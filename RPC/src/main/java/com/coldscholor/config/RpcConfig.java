package com.coldscholor.config;

/**
 * RPC框架配置管理类
 * 管理线程池、连接池、熔断器等配置参数
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class RpcConfig {
    
    // ========== 线程池配置 ==========
    
    /** 核心线程数 */
    private static final int CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors();
    
    /** 最大线程数 */
    private static final int MAX_POOL_SIZE = CORE_POOL_SIZE * 2;
    
    /** 线程空闲时间(秒) */
    private static final long KEEP_ALIVE_TIME = 60L;
    
    /** 任务队列大小 */
    private static final int QUEUE_CAPACITY = 1000;
    
    // ========== 连接池配置 ==========
    
    /** 最大连接数 */
    private static final int MAX_CONNECTIONS = 200;
    
    /** 每个路由的最大连接数 */
    private static final int MAX_CONNECTIONS_PER_ROUTE = 50;
    
    /** 连接超时时间(毫秒) */
    private static final int CONNECTION_TIMEOUT = 5000;
    
    /** 读取超时时间(毫秒) */
    private static final int READ_TIMEOUT = 10000;
    
    /** 连接请求超时时间(毫秒) */
    private static final int CONNECTION_REQUEST_TIMEOUT = 3000;
    
    // ========== 熔断器配置 ==========
    
    /** 熔断器失败率阈值 */
    private static final float FAILURE_RATE_THRESHOLD = 50.0f;
    
    /** 熔断器等待时间(秒) */
    private static final int WAIT_DURATION_IN_OPEN_STATE = 30;
    
    /** 熔断器半开状态下的调用次数 */
    private static final int PERMITTED_CALLS_IN_HALF_OPEN_STATE = 5;
    
    /** 熔断器滑动窗口大小 */
    private static final int SLIDING_WINDOW_SIZE = 10;
    
    /** 熔断器最小调用次数 */
    private static final int MINIMUM_NUMBER_OF_CALLS = 5;
    
    // ========== RPC调用配置 ==========
    
    /** 默认重试次数 */
    private static final int DEFAULT_RETRY_COUNT = 3;
    
    /** 重试间隔时间(毫秒) */
    private static final long RETRY_INTERVAL = 1000L;
    
    /** 异步调用超时时间(秒) */
    private static final int ASYNC_TIMEOUT = 30;
    
    // ========== Getter方法 ==========
    
    public static int getCorePoolSize() {
        return CORE_POOL_SIZE;
    }
    
    public static int getMaxPoolSize() {
        return MAX_POOL_SIZE;
    }
    
    public static long getKeepAliveTime() {
        return KEEP_ALIVE_TIME;
    }
    
    public static int getQueueCapacity() {
        return QUEUE_CAPACITY;
    }
    
    public static int getMaxConnections() {
        return MAX_CONNECTIONS;
    }
    
    public static int getMaxConnectionsPerRoute() {
        return MAX_CONNECTIONS_PER_ROUTE;
    }
    
    public static int getConnectionTimeout() {
        return CONNECTION_TIMEOUT;
    }
    
    public static int getReadTimeout() {
        return READ_TIMEOUT;
    }
    
    public static int getConnectionRequestTimeout() {
        return CONNECTION_REQUEST_TIMEOUT;
    }
    
    public static float getFailureRateThreshold() {
        return FAILURE_RATE_THRESHOLD;
    }
    
    public static int getWaitDurationInOpenState() {
        return WAIT_DURATION_IN_OPEN_STATE;
    }
    
    public static int getPermittedCallsInHalfOpenState() {
        return PERMITTED_CALLS_IN_HALF_OPEN_STATE;
    }
    
    public static int getSlidingWindowSize() {
        return SLIDING_WINDOW_SIZE;
    }
    
    public static int getMinimumNumberOfCalls() {
        return MINIMUM_NUMBER_OF_CALLS;
    }
    
    public static int getDefaultRetryCount() {
        return DEFAULT_RETRY_COUNT;
    }
    
    public static long getRetryInterval() {
        return RETRY_INTERVAL;
    }
    
    public static int getAsyncTimeout() {
        return ASYNC_TIMEOUT;
    }
}