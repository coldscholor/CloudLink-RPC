package com.coldscholor.protocol;

import com.coldscholor.common.Invocation;
import com.coldscholor.http.HttpConnectionPoolManager;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * 改进的HTTP客户端
 * 使用Apache HttpClient和连接池提供高性能的HTTP通信
 * 
 * @author 寒士obj
 * @date 2025/01/15
 */
public class ImprovedHttpClient {
    
    /** 单例实例 */
    private static volatile ImprovedHttpClient instance;
    
    /** HTTP连接池管理器 */
    private final HttpConnectionPoolManager connectionPoolManager;
    
    /** HTTP客户端 */
    private final CloseableHttpClient httpClient;
    
    /**
     * 私有构造函数
     */
    private ImprovedHttpClient() {
        this.connectionPoolManager = HttpConnectionPoolManager.getInstance();
        this.httpClient = connectionPoolManager.getHttpClient();
    }
    
    /**
     * 获取单例实例
     */
    public static ImprovedHttpClient getInstance() {
        if (instance == null) {
            synchronized (ImprovedHttpClient.class) {
                if (instance == null) {
                    instance = new ImprovedHttpClient();
                }
            }
        }
        return instance;
    }

    /**
     * 发送HTTP请求（通过URL）
     * 
     * @param url 完整的URL
     * @param invocation 调用信息
     * @return 响应结果
     * @throws IOException IO异常
     */
    public String sendRequest(String url, Invocation invocation) throws IOException {
        // 确保URL以/结尾
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        
        // 创建POST请求
        HttpPost httpPost = new HttpPost(url);
        
        try {
            // 序列化请求对象
            byte[] requestData = serializeInvocation(invocation);
            
            // 设置请求实体
            HttpEntity requestEntity = new ByteArrayEntity(requestData);
            httpPost.setEntity(requestEntity);
            
            // 设置请求头
            httpPost.setHeader("Content-Type", "application/octet-stream");
            httpPost.setHeader("User-Agent", "RPC-Client/1.0");
            
            // 执行请求
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // 检查响应状态
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    throw new IOException("HTTP request failed with status code: " + statusCode);
                }
                
                // 获取响应实体
                HttpEntity responseEntity = response.getEntity();
                if (responseEntity == null) {
                    throw new IOException("Empty response entity");
                }
                
                // 读取响应内容
                String result = EntityUtils.toString(responseEntity, "UTF-8");
                
                // 确保实体被完全消费
                EntityUtils.consume(responseEntity);
                
                return result;
            }
            
        } catch (IOException e) {
            // 取消请求
            httpPost.abort();
            throw e;
        } finally {
            // HttpPost会自动释放连接，无需手动释放
        }
    }
    
    /**
     * 序列化Invocation对象
     */
    private byte[] serializeInvocation(Invocation invocation) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            
            oos.writeObject(invocation);
            oos.flush();
            
            return baos.toByteArray();
        }
    }

    
    /**
     * HTTP回调接口
     */
    public interface HttpCallback {
        /**
         * 请求成功回调
         */
        void onSuccess(String result);
        
        /**
         * 请求失败回调
         */
        void onFailure(Exception e);
    }
}