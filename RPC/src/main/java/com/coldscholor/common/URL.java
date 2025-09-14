package com.coldscholor.common;

import java.io.Serializable;

/**
 * URL类 - 服务地址封装
 * 包含主机名、端口和权重信息，用于负载均衡
 * 
 * @author 寒士obj
 * @date 2025/09/13 15:44
 **/
public class URL implements Serializable {

    private String hostName;
    private int port;
    private int weight = 1; // 默认权重为1

    public URL() {
    }

    public URL(String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
    }
    
    public URL(String hostName, int port, int weight) {
        this.hostName = hostName;
        this.port = port;
        this.weight = weight;
    }

    /**
     * 获取
     * @return hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * 设置
     * @param hostName
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * 获取
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * 设置
     * @param port
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * 获取权重
     * @return weight
     */
    public int getWeight() {
        return weight;
    }
    
    /**
     * 设置权重
     * @param weight
     */
    public void setWeight(int weight) {
        this.weight = Math.max(1, weight); // 权重最小为1
    }

    public String toString() {
        return "URL{hostName = " + hostName + ", port = " + port + ", weight = " + weight + "}";
    }
}
