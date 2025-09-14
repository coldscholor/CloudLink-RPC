package com.coldscholor.http;

import com.coldscholor.config.RpcConfig;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * HTTP连接池管理器
 * 使用Apache HttpClient提供高性能的HTTP连接池
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class HttpConnectionPoolManager {
    
    /** 单例实例 */
    private static volatile HttpConnectionPoolManager instance;
    
    /** 连接池管理器 */
    private final PoolingHttpClientConnectionManager connectionManager;
    
    /** HTTP客户端 */
    private final CloseableHttpClient httpClient;
    
    /** 请求配置 */
    private final RequestConfig requestConfig;
    
    /**
     * 私有构造函数
     */
    private HttpConnectionPoolManager() {
        // 创建连接池管理器
        this.connectionManager = new PoolingHttpClientConnectionManager();
        
        // 配置连接池参数
        connectionManager.setMaxTotal(RpcConfig.getMaxConnections());
        connectionManager.setDefaultMaxPerRoute(RpcConfig.getMaxConnectionsPerRoute());
        
        // 创建请求配置
        this.requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(RpcConfig.getConnectionRequestTimeout())
            .setConnectTimeout(RpcConfig.getConnectionTimeout())
            .setSocketTimeout(RpcConfig.getReadTimeout())
            .build();
        
        // 创建HTTP客户端
        this.httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setDefaultRequestConfig(requestConfig)
            .build();
        
        // 注册JVM关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));
    }
    
    /**
     * 获取单例实例
     */
    public static HttpConnectionPoolManager getInstance() {
        if (instance == null) {
            synchronized (HttpConnectionPoolManager.class) {
                if (instance == null) {
                    instance = new HttpConnectionPoolManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * 获取HTTP客户端
     */
    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }
    
    /**
     * 获取请求配置
     */
    public RequestConfig getRequestConfig() {
        return requestConfig;
    }
    

    /**
     * 关闭连接池
     */
    public void shutdown() {
        try {
            System.out.println("Shutting down HTTP connection pool...");
            
            if (httpClient != null) {
                httpClient.close();
            }
            
            if (connectionManager != null) {
                connectionManager.close();
            }
            
            System.out.println("HTTP connection pool shutdown completed.");
        } catch (Exception e) {
            System.err.println("Error shutting down HTTP connection pool: " + e.getMessage());
        }
    }
    

}