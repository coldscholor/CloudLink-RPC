package com.coldscholor.register;

import com.coldscholor.common.URL;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 寒士obj
 * @date 2025/09/13 15:45
 **/
// 注册中心
public class MapRemoteRegister {

    // 注册表，键值对为接口名称和服务列表
    private static Map<String, List<URL>> REGISTRY = new ConcurrentHashMap<>();

    /**
     * 把接口和实现类注册到远程注册中心（Naocs、Etcd、ZooKeeper）
     *
     * @param interfaceName
     * @param  url
     */
    public static void register(String interfaceName, URL url) {
        List<URL> urls = REGISTRY.get(interfaceName);
        if (urls == null) {
            urls = new ArrayList<>();
        }
        urls.add(url);

        REGISTRY.put(interfaceName, urls);

        // 保存到临时文件中
        saveFile();
    }

    /**
     * 根据接口名获取实现类
     *
     * @param interfaceName
     * @return
     */
    public static List<URL> get(String interfaceName) {
        // 从本地缓存中获取
        REGISTRY = loadFile();
        return REGISTRY.get(interfaceName);
    }

    /**
     * 因为同时要开启两个JVM进程，但是不能共享缓存，所以保存到临时文件中
     */
    private static void saveFile(){
        try {
            FileOutputStream fos = new FileOutputStream("/temp.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(REGISTRY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Map<String, List<URL>> loadFile(){
        try {
            FileInputStream fis = new FileInputStream("/temp.txt");
            ObjectInputStream ois = new ObjectInputStream(fis);
            return (Map<String, List<URL>>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ConcurrentHashMap<>();
    }
}
