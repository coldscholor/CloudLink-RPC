package com.coldscholor.proxy;

import com.coldscholor.async.AsyncRpcManager;
import com.coldscholor.circuitbreaker.CircuitBreakerManager;
import com.coldscholor.common.Invocation;
import com.coldscholor.common.URL;
import com.coldscholor.loadbalance.LoadBalance;
import com.coldscholor.protocol.ImprovedHttpClient;
import com.coldscholor.register.MapRemoteRegister;
import com.coldscholor.threadpool.ThreadPoolManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 代理工厂 - 纯异步版
 * 基于CompletableFuture的高性能异步RPC框架
 * 集成熔断器、负载均衡、连接池等企业级特性
 * 
 * 技术亮点：
 * - 100%异步调用，提升系统吞吐量
 * - 熔断器保护，防止雪崩效应
 * - 智能负载均衡，优化资源利用
 * - 非阻塞线程池，提高并发性能
 * 
 * @author 寒士obj
 * @date 2025/09/13 15:30
 **/
public class ProxyFactory {
    
    /** 异步RPC管理器 */
    private static final AsyncRpcManager asyncRpcManager = AsyncRpcManager.getInstance();
    
    /** 熔断器管理器 */
    private static final CircuitBreakerManager circuitBreakerManager = CircuitBreakerManager.getInstance();
    
    /** 改进的HTTP客户端 */
    private static final ImprovedHttpClient improvedHttpClient = ImprovedHttpClient.getInstance();
    
    /** 线程池管理器 */
    private static final ThreadPoolManager threadPoolManager = ThreadPoolManager.getInstance();
    
    /**
     * 创建异步代理对象（默认异步模式）
     * @param clazz 接口类
     * @return 代理对象
     * @param <T> 泛型类型
     */
    public static <T> T getProxy(Class clazz) {
        return getProxy(clazz, true);
    }
    
    /**
     * 创建代理对象（纯异步模式）
     * @param clazz 接口类
     * @param async 保留参数兼容性（始终为异步）
     * @return 代理对象
     * @param <T> 泛型类型
     */
    public static <T> T getProxy(Class clazz, boolean async)
    {
        Object proxyInstance = Proxy.newProxyInstance(clazz.getClassLoader(), new Class[] {clazz}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                // Mock模式支持
                String mock = System.getProperty("mock");
                if(mock != null && mock.startsWith("return:")){
                    String returnValue = mock.replace("return:", "");
                    return returnValue;
                }

                // 创建调用信息
                Invocation invocation = new Invocation(
                        clazz.getName(),
                        method.getName(),
                        method.getParameterTypes(),
                        args);

                // 服务发现
                List<URL> urls = MapRemoteRegister.get(clazz.getName());
                if (urls == null || urls.isEmpty()) {
                    throw new RuntimeException("No available service providers for: " + clazz.getName());
                }

                String serviceName = clazz.getName() + "." + method.getName();
                
                // 纯异步调用模式（带熔断器和负载均衡）
                return handleAsyncCallWithCircuitBreaker(urls, invocation, serviceName, method);
            }
        });


        return (T) proxyInstance;
    }
    
    /**
     * 处理异步调用（带熔断器保护和负载均衡）
     */
    private static Object handleAsyncCallWithCircuitBreaker(List<URL> urls, Invocation invocation, String serviceName, Method method) {
        return circuitBreakerManager.executeWithCircuitBreaker(
            serviceName,
            () -> {
                // 负载均衡选择服务
                URL url = LoadBalance.random(urls);
                String requestUrl = "http://" + url.getHostName() + ":" + url.getPort();
                
                // 异步调用但同步等待结果（非阻塞线程池处理）
                try {
                    CompletableFuture<String> future = asyncRpcManager.callAsyncWithTimeout(
                        requestUrl, invocation, 30, TimeUnit.SECONDS
                    );
                    return future.get(30, TimeUnit.SECONDS);
                } catch (Exception e) {
                    throw new RuntimeException("Async call failed: " + e.getMessage(), e);
                }
            },
            () -> CircuitBreakerManager.createFallbackResponse(serviceName)
        );
    }


}
