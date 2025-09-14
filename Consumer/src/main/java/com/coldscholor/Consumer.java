package com.coldscholor;

import com.coldscholor.proxy.ProxyFactory;

/**
 * RPC消费者
 * 
 * @author 寒士obj
 * @date 2025/09/13 13:30
 **/
public class Consumer {
    public static void main(String[] args) {
        HelloService service = ProxyFactory.getProxy(HelloService.class);
        String result = service.sayHello("coldscholor");
        System.out.println(result);
    }
}
