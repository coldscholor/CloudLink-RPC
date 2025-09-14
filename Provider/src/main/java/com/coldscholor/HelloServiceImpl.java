package com.coldscholor;

/**
 * @author 寒士obj
 * @date 2025/09/13 13:29
 **/
public class HelloServiceImpl implements HelloService{
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}
