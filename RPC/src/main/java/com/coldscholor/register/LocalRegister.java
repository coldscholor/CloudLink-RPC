package com.coldscholor.register;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 寒士obj
 * @date 2025/09/13 14:40
 **/
// 本地注册
public class LocalRegister {
    private static final Map<String, Class> map = new ConcurrentHashMap<>();

    /**
     *  把接口和实现类注册到本地注册中心
     * @param interfaceName
     * @param implClass
     */
    public static void register(String interfaceName, String version, Class implClass) {
        map.put(interfaceName + version, implClass);
    }

    /**
     * 根据接口名获取实现类
     * @param interfaceName
     * @return
     */
    public static Class get(String interfaceName, String version) {
        return map.get(interfaceName + version);
    }
}
