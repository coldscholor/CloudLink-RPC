package com.coldscholor.async;

import com.coldscholor.circuitbreaker.CircuitBreakerManager;
import com.coldscholor.common.Invocation;
import com.coldscholor.protocol.ImprovedHttpClient;
import com.coldscholor.threadpool.ThreadPoolManager;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/**
 * 异步RPC调用管理器
 * 支持CompletableFuture和回调模式的异步调用
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class AsyncRpcManager {
    
    /** 单例实例 */
    private static volatile AsyncRpcManager instance;
    
    /** HTTP客户端 */
    private final ImprovedHttpClient httpClient;
    
    /** 线程池管理器 */
    private final ThreadPoolManager threadPoolManager;
    
    /** 熔断器管理器 */
    private final CircuitBreakerManager circuitBreakerManager;
    
    /** 请求ID生成器 */
    private final AtomicLong requestIdGenerator;
    
    /** 待处理的异步请求 */
    private final ConcurrentHashMap<Long, AsyncRequest> pendingRequests;
    
    /**
     * 私有构造函数
     */
    private AsyncRpcManager() {
        this.httpClient = ImprovedHttpClient.getInstance();
        this.threadPoolManager = ThreadPoolManager.getInstance();
        this.circuitBreakerManager = CircuitBreakerManager.getInstance();
        this.requestIdGenerator = new AtomicLong(0);
        this.pendingRequests = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     */
    public static AsyncRpcManager getInstance() {
        if (instance == null) {
            synchronized (AsyncRpcManager.class) {
                if (instance == null) {
                    instance = new AsyncRpcManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 异步调用RPC服务（CompletableFuture模式）
     * 
     * @param url 服务URL
     * @param invocation 调用信息
     * @return CompletableFuture包装的结果
     */
    public CompletableFuture<String> callAsync(String url, Invocation invocation) {
        return callAsync(url, invocation, null, null);
    }
    
    /**
     * 异步调用RPC服务（回调模式）
     * 
     * @param url 服务URL
     * @param invocation 调用信息
     * @param onSuccess 成功回调
     * @param onError 错误回调
     * @return CompletableFuture包装的结果
     */
    public CompletableFuture<String> callAsync(String url, Invocation invocation, 
                                               Consumer<String> onSuccess, Consumer<Throwable> onError) {
        
        long requestId = requestIdGenerator.incrementAndGet();
        String serviceName = invocation.getInterfaceName() + "." + invocation.getMethodName();
        
        // 创建异步请求对象
        AsyncRequest asyncRequest = new AsyncRequest(requestId, url, invocation, onSuccess, onError);
        pendingRequests.put(requestId, asyncRequest);
        
        // 创建CompletableFuture - 这里是异步的关键！
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                // 使用熔断器保护的异步调用
                return circuitBreakerManager.executeWithCircuitBreaker(
                    serviceName,
                    () -> {
                        try {
                            return httpClient.sendRequest(url, invocation);
                        } catch (Exception e) {
                            throw new RuntimeException("HTTP request failed", e);
                        }
                    },
                    () -> CircuitBreakerManager.createFallbackResponse(serviceName)
                );
            } catch (Exception e) {
                throw new RuntimeException("Async RPC call failed", e);
            } finally {
                // 清理待处理请求
                pendingRequests.remove(requestId);
            }
        }, threadPoolManager.getClientExecutor());
        
        // 添加回调处理
        if (onSuccess != null || onError != null) {
            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    if (onError != null) {
                        threadPoolManager.getCallbackExecutor().execute(() -> onError.accept(throwable));
                    }
                } else {
                    if (onSuccess != null) {
                        threadPoolManager.getCallbackExecutor().execute(() -> onSuccess.accept(result));
                    }
                }
            });
        }
        
        return future;
    }
    
    /**
     * 异步调用RPC服务（带超时）
     * 
     * @param url 服务URL
     * @param invocation 调用信息
     * @param timeout 超时时间
     * @param timeUnit 时间单位
     * @return CompletableFuture包装的结果
     */
    public CompletableFuture<String> callAsyncWithTimeout(String url, Invocation invocation, 
                                                          long timeout, TimeUnit timeUnit) {
        CompletableFuture<String> future = callAsync(url, invocation);
        
        // 创建超时处理
        CompletableFuture<String> timeoutFuture = new CompletableFuture<>();
        
        // 设置超时任务
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
             if (!future.isDone()) {
                 timeoutFuture.completeExceptionally(
                     new RuntimeException("Async RPC call timeout after " + timeout + " " + timeUnit)
                 );
             }
             scheduler.shutdown();
         }, timeout, timeUnit);
        
        // 返回第一个完成的Future
        return CompletableFuture.anyOf(future, timeoutFuture)
            .thenCompose(result -> {
                if (result instanceof String) {
                    return CompletableFuture.completedFuture((String) result);
                } else {
                    CompletableFuture<String> failedFuture = new CompletableFuture<>();
                    failedFuture.completeExceptionally(
                        new RuntimeException("Unexpected result type: " + result.getClass())
                    );
                    return failedFuture;
                }
            });
    }
    


    

    
    /**
     * 异步请求内部类
     */
    private static class AsyncRequest {
        private final long requestId;
        private final String url;
        private final Invocation invocation;
        private final Consumer<String> onSuccess;
        private final Consumer<Throwable> onError;
        private final long createTime;
        
        public AsyncRequest(long requestId, String url, Invocation invocation, 
                           Consumer<String> onSuccess, Consumer<Throwable> onError) {
            this.requestId = requestId;
            this.url = url;
            this.invocation = invocation;
            this.onSuccess = onSuccess;
            this.onError = onError;
            this.createTime = System.currentTimeMillis();
        }
        
        // Getters
        public long getRequestId() { return requestId; }
        public String getUrl() { return url; }
        public Invocation getInvocation() { return invocation; }
        public Consumer<String> getOnSuccess() { return onSuccess; }
        public Consumer<Throwable> getOnError() { return onError; }
        public long getCreateTime() { return createTime; }
    }
    
    /**
     * 批量请求内部类
     */
    public static class BatchRequest {
        private final String url;
        private final Invocation invocation;
        
        public BatchRequest(String url, Invocation invocation) {
            this.url = url;
            this.invocation = invocation;
        }
        
        public String getUrl() { return url; }
        public Invocation getInvocation() { return invocation; }
    }
}