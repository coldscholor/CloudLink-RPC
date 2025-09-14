package com.coldscholor.common;

/**
 * @author 寒士obj
 * @date 2025/09/13 13:54
 **/

import java.io.Serializable;

/**
 * 将需要调用的方法封装成对象传递到服务提供者
 */
public class Invocation implements Serializable {
    // 接口名
    private String interfaceName;
    // 方法名
    private String methodName;
    // 参数类型
    private Class[] parameterTypes;
    // 参数
    private Object[] arguments;

    public Invocation() {
    }

    public Invocation(String interfaceName, String methodName, Class[] parameterTypes, Object[] arguments) {
        this.interfaceName = interfaceName;
        this.methodName = methodName;
        this.parameterTypes = parameterTypes;
        this.arguments = arguments;
    }

    /**
     * 获取
     * @return interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * 设置
     * @param interfaceName
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * 获取
     * @return methodName
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * 设置
     * @param methodName
     */
    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    /**
     * 获取
     * @return parameterTypes
     */
    public Class[] getParameterTypes() {
        return parameterTypes;
    }

    /**
     * 设置
     * @param parameterTypes
     */
    public void setParameterTypes(Class[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    /**
     * 获取
     * @return arguments
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * 设置
     * @param arguments
     */
    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public String toString() {
        return "Invocation{interfaceName = " + interfaceName + ", methodName = " + methodName + ", parameterTypes = " + parameterTypes + ", arguments = " + arguments + "}";
    }
    // 版本号，用户区分接口的实现类
    // private String version;


}
