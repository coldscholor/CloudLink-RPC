package com.coldscholor.loadbalance;

import com.coldscholor.common.URL;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 负载均衡算法实现类
 * 支持三种负载均衡策略：随机、轮询、加权随机
 * 
 * @author 寒士obj
 * @date 2025/09/13 15:52
 **/
public class LoadBalance {
    
    // 轮询算法的计数器
    private static final AtomicInteger roundRobinCounter = new AtomicInteger(0);
    
    /**
     * 负载均衡策略枚举
     */
    public enum Strategy {
        RANDOM,              // 随机
        ROUND_ROBIN,         // 轮询
        WEIGHTED_RANDOM      // 加权随机
    }
    
    /**
     * 随机算法
     * @param urls 服务URL列表
     * @return 选中的URL
     */
    public static URL random(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URL list cannot be null or empty");
        }
        int index = ThreadLocalRandom.current().nextInt(urls.size());
        return urls.get(index);
    }
    
    /**
     * 轮询算法
     * @param urls 服务URL列表
     * @return 选中的URL
     */
    public static URL roundRobin(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URL list cannot be null or empty");
        }
        int index = roundRobinCounter.getAndIncrement() % urls.size();
        return urls.get(index);
    }
    
    /**
     * 加权随机算法
     * @param urls 服务URL列表
     * @return 选中的URL
     */
    public static URL weightedRandom(List<URL> urls) {
        if (urls == null || urls.isEmpty()) {
            throw new IllegalArgumentException("URL list cannot be null or empty");
        }
        
        // 计算总权重
        int totalWeight = urls.stream().mapToInt(url -> url.getWeight()).sum();
        if (totalWeight <= 0) {
            return random(urls); // 如果没有权重，降级为随机算法
        }
        
        // 生成随机数
        int randomWeight = ThreadLocalRandom.current().nextInt(totalWeight);
        
        // 根据权重选择URL
        int currentWeight = 0;
        for (URL url : urls) {
            currentWeight += url.getWeight();
            if (randomWeight < currentWeight) {
                return url;
            }
        }
        
        return urls.get(urls.size() - 1); // 兜底返回最后一个
    }
    

    
    /**
     * 根据策略选择URL
     * @param urls 服务URL列表
     * @param strategy 负载均衡策略
     * @return 选中的URL
     */
    public static URL select(List<URL> urls, Strategy strategy) {
        switch (strategy) {
            case RANDOM:
                return random(urls);
            case ROUND_ROBIN:
                return roundRobin(urls);
            case WEIGHTED_RANDOM:
                return weightedRandom(urls);
            default:
                return random(urls);
        }
    }
    
    /**
     * 清理统计信息
     */
    public static void clearStats() {
        roundRobinCounter.set(0);
    }
}
