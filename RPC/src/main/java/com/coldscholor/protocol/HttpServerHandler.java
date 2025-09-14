package com.coldscholor.protocol;

import com.coldscholor.common.Invocation;
import com.coldscholor.register.LocalRegister;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author 寒士obj
 * @date 2025/09/13 13:49
 **/
public class HttpServerHandler {
    public void handle(HttpServletRequest  request, HttpServletResponse  response){
        try {
            // 处理请求 --> 调用某个接口的某个方法、方法参数
            ObjectInputStream ois = new ObjectInputStream(request.getInputStream());
            // 反序列化
            Invocation invocation = (Invocation) ois.readObject();
            String interfaceName = invocation.getInterfaceName();
            // String version = invocation.getVersion();

            // 通过接口名称，从本地注册中心（map）中获取接口实现类
            // 如果版本号为空，则默认调用1.0版本

            Class aClass = aClass = LocalRegister.get(interfaceName, "1.0");;
          /*  if(version == null){
                aClass = LocalRegister.get(interfaceName, "1.0");
            }
            aClass = LocalRegister.get(interfaceName, version);*/

            // 通过反射创建实例并执行方法
            Object object = aClass.newInstance();
            Method method = aClass.getMethod(invocation.getMethodName(), invocation.getParameterTypes());

            // 执行方法获取结果
            String result = (String) method.invoke(object, invocation.getArguments());

            // // 序列化结果并返回给客户端
            IOUtils.write(result, response.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
