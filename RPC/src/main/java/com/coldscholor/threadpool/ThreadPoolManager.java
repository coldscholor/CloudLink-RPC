package com.coldscholor.threadpool;

import com.coldscholor.config.RpcConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 线程池管理器
 * 提供统一的线程池管理和任务执行功能
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class ThreadPoolManager {
    
    /** 单例实例 */
    private static volatile ThreadPoolManager instance;
    
    /** 服务端请求处理线程池 */
    private final ThreadPoolExecutor serverExecutor;
    
    /** 客户端调用线程池 */
    private final ThreadPoolExecutor clientExecutor;
    
    /** 异步回调线程池 */
    private final ThreadPoolExecutor callbackExecutor;
    
    /** 关闭标志 */
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    
    /**
     * 私有构造函数
     */
    private ThreadPoolManager() {
        // 服务端请求处理线程池
        this.serverExecutor = createThreadPool(
            "rpc-server",
            RpcConfig.getCorePoolSize(),
            RpcConfig.getMaxPoolSize(),
            RpcConfig.getKeepAliveTime(),
            RpcConfig.getQueueCapacity()
        );
        
        // 客户端调用线程池
        this.clientExecutor = createThreadPool(
            "rpc-client",
            RpcConfig.getCorePoolSize() / 2,
            RpcConfig.getMaxPoolSize(),
            RpcConfig.getKeepAliveTime(),
            RpcConfig.getQueueCapacity()
        );
        
        // 异步回调线程池
        this.callbackExecutor = createThreadPool(
            "rpc-callback",
            2,
            RpcConfig.getCorePoolSize(),
            RpcConfig.getKeepAliveTime(),
            RpcConfig.getQueueCapacity() / 2
        );
        
        // 注册JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    /**
     * 获取单例实例
     */
    public static ThreadPoolManager getInstance() {
        if (instance == null) {
            synchronized (ThreadPoolManager.class) {
                if (instance == null) {
                    instance = new ThreadPoolManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 创建线程池
     */
    private ThreadPoolExecutor createThreadPool(String namePrefix, int corePoolSize, 
                                               int maximumPoolSize, long keepAliveTime, 
                                               int queueCapacity) {
        ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat(namePrefix + "-%d")
            .setDaemon(false)
            .build();
            
        BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<>(queueCapacity);
        
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            corePoolSize,
            maximumPoolSize,
            keepAliveTime,
            TimeUnit.SECONDS,
            workQueue,
            threadFactory,
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略：调用者运行
        );
        
        // 允许核心线程超时
        executor.allowCoreThreadTimeOut(true);
        
        return executor;
    }
    
    /**
     * 提交服务端任务
     */
    public void submitServerTask(Runnable task) {
        if (!shutdown.get()) {
            serverExecutor.submit(task);
        }
    }
    

    

    
    /**
     * 获取回调线程池执行器
     */
    public ThreadPoolExecutor getCallbackExecutor() {
        return callbackExecutor;
    }
    
    /**
     * 获取客户端线程池执行器
     */
    public ThreadPoolExecutor getClientExecutor() {
        return clientExecutor;
    }
    

    
    /**
     * 优雅关闭线程池
     */
    public void shutdown() {
        if (shutdown.compareAndSet(false, true)) {
            System.out.println("Shutting down ThreadPoolManager...");
            
            shutdownExecutor("Server", serverExecutor);
            shutdownExecutor("Client", clientExecutor);
            shutdownExecutor("Callback", callbackExecutor);
            
            System.out.println("ThreadPoolManager shutdown completed.");
        }
    }
    
    /**
     * 关闭单个线程池
     */
    private void shutdownExecutor(String name, ThreadPoolExecutor executor) {
        try {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                System.out.println(name + " executor did not terminate gracefully, forcing shutdown...");
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println(name + " executor did not terminate after forced shutdown");
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupted while shutting down " + name + " executor");
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * 检查是否已关闭
     */
    public boolean isShutdown() {
        return shutdown.get();
    }
}