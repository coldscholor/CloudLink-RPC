package com.coldscholor.circuitbreaker;

import com.coldscholor.config.RpcConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 熔断器管理器
 * 使用Resilience4j实现熔断器模式，提供服务降级和故障隔离
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class CircuitBreakerManager {
    
    /** 单例实例 */
    private static volatile CircuitBreakerManager instance;
    
    /** 熔断器注册表 */
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    /** 熔断器缓存 */
    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakerCache;
    
    /**
     * 私有构造函数
     */
    private CircuitBreakerManager() {
        // 创建默认熔断器配置
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(RpcConfig.getFailureRateThreshold())
            .waitDurationInOpenState(Duration.ofSeconds(RpcConfig.getWaitDurationInOpenState()))
            .permittedNumberOfCallsInHalfOpenState(RpcConfig.getPermittedCallsInHalfOpenState())
            .slidingWindowSize(RpcConfig.getSlidingWindowSize())
            .minimumNumberOfCalls(RpcConfig.getMinimumNumberOfCalls())
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            .build();
        
        // 创建熔断器注册表
        this.circuitBreakerRegistry = CircuitBreakerRegistry.of(defaultConfig);
        
        // 初始化缓存
        this.circuitBreakerCache = new ConcurrentHashMap<>();
    }
    
    /**
     * 获取单例实例
     */
    public static CircuitBreakerManager getInstance() {
        if (instance == null) {
            synchronized (CircuitBreakerManager.class) {
                if (instance == null) {
                    instance = new CircuitBreakerManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取或创建熔断器
     * 
     * @param serviceName 服务名称
     * @return 熔断器实例
     */
    public CircuitBreaker getOrCreateCircuitBreaker(String serviceName) {
        return circuitBreakerCache.computeIfAbsent(serviceName, name -> 
            circuitBreakerRegistry.circuitBreaker(name)
        );
    }
    
    /**
     * 执行带熔断器保护的操作
     * 
     * @param serviceName 服务名称
     * @param supplier 要执行的操作
     * @param fallback 降级操作
     * @return 执行结果
     */
    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = getOrCreateCircuitBreaker(serviceName);
        
        // 装饰supplier
        Supplier<T> decoratedSupplier = CircuitBreaker.decorateSupplier(circuitBreaker, supplier);
        
        try {
            return decoratedSupplier.get();
        } catch (Exception e) {
            // 执行降级逻辑
            if (fallback != null) {
                return fallback.get();
            }
            
            throw new RuntimeException("Service call failed and no fallback provided", e);
        }
    }
    
    /**
     * 执行带熔断器保护的操作（无降级）
     * 
     * @param serviceName 服务名称
     * @param supplier 要执行的操作
     * @return 执行结果
     */
    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> supplier) {
        return executeWithCircuitBreaker(serviceName, supplier, null);
    }
    

    
    /**
     * 创建默认降级响应
     * 
     * @param serviceName 服务名称
     * @return 降级响应
     */
    public static String createFallbackResponse(String serviceName) {
        return String.format("Service '%s' is currently unavailable. Please try again later.", serviceName);
    }
}