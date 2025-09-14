package com.coldscholor;

import com.coldscholor.common.URL;
import com.coldscholor.protocol.HttpServer;
import com.coldscholor.register.LocalRegister;
import com.coldscholor.register.MapRemoteRegister;

/**
 * @author 寒士obj
 * @date 2025/09/13 13:36
 **/
public class Provider {
    public static void main(String[] args) {
        // 本地注册
        LocalRegister.register(HelloService.class.getName(), "1.0", HelloServiceImpl.class);
        LocalRegister.register(HelloService.class.getName(), "2.0", HelloServiceImpl2.class);

        // 注册中心注册，服务注册
        URL url = new URL("127.0.0.1", 8080);
        MapRemoteRegister.register(HelloService.class.getName(), url);

        // Netty/Tomcat来接收网络请求
        HttpServer server = new HttpServer();
        server.start(url.getHostName(), url.getPort());
    }
}
