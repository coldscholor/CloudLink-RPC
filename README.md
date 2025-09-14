# RPC框架项目

一个基于HTTP协议的轻量级分布式RPC框架，支持服务注册发现、负载均衡、熔断保护等企业级特性。

## 项目架构

### 模块结构

```
RPC-project/
├── Provider-Common/     # 公共接口定义模块
├── Provider/           # 服务提供者模块
├── Consumer/           # 服务消费者模块
├── RPC/               # 核心框架模块
└── README.md          # 项目说明文档
```

### 核心组件

- **服务注册与发现**: 支持本地注册和远程注册中心
- **代理工厂**: 基于JDK动态代理实现透明远程调用
- **负载均衡**: 提供随机、轮询、加权随机三种策略
- **熔断器**: 基于Resilience4j实现故障隔离和服务降级
- **异步调用**: 高性能异步请求处理机制
- **连接池管理**: HTTP连接复用提升性能
- **线程池管理**: 分离服务端、客户端、回调线程池

## 技术特性

### 🚀 高性能
- 异步调用机制，提升并发处理能力
- HTTP连接池复用，减少连接开销
- 多线程池隔离，优化资源利用

### 🛡️ 高可用
- 熔断器保护，防止服务雪崩
- 多种负载均衡策略，实现故障转移
- 服务健康检查和自动恢复

### 🔧 易扩展
- 模块化设计，组件可插拔
- 策略模式实现算法可替换
- 配置化管理，参数可调优

### 📦 轻量级
- 基于HTTP协议，无需复杂序列化
- 最小化依赖，快速集成
- 简洁API设计，易于使用

## 快速开始

### 1. 定义服务接口

```java
// Provider-Common模块
public interface HelloService {
    String sayHello(String name);
}
```

### 2. 实现服务提供者

```java
// Provider模块
public class HelloServiceImpl implements HelloService {
    @Override
    public String sayHello(String name) {
        return "Hello " + name;
    }
}

public class Provider {
    public static void main(String[] args) {
        // 本地注册服务实现
        LocalRegister.register(HelloService.class.getName(), "1.0", HelloServiceImpl.class);
        
        // 远程注册中心注册
        URL url = new URL("127.0.0.1", 8080);
        MapRemoteRegister.register(HelloService.class.getName(), url);
        
        // 启动HTTP服务器
        HttpServer server = new HttpServer();
        server.start(url.getHostName(), url.getPort());
    }
}
```

### 3. 创建服务消费者

```java
// Consumer模块
public class Consumer {
    public static void main(String[] args) {
        // 创建服务代理
        HelloService service = ProxyFactory.getProxy(HelloService.class);
        
        // 调用远程服务
        String result = service.sayHello("World");
        System.out.println(result); // 输出: Hello World
    }
}
```

## 运行项目

### 环境要求
- JDK 8+
- Maven 3.6+

### 编译项目
```bash
mvn clean compile
```

### 启动服务提供者
```bash
java -cp "RPC\lib\*;RPC\target\classes;Provider-Common\target\classes;Provider\target\classes" com.coldscholor.Provider
```

### 运行服务消费者
```bash
java -cp "RPC\lib\*;RPC\target\classes;Provider-Common\target\classes;Consumer\target\classes" com.coldscholor.Consumer
```

## 核心配置

### 线程池配置
- 核心线程数: CPU核心数
- 最大线程数: CPU核心数 × 2
- 队列容量: 1000
- 线程空闲时间: 60秒

### 连接池配置
- 最大连接数: 200
- 每路由最大连接数: 50
- 连接超时: 5秒
- 读取超时: 10秒

### 熔断器配置
- 失败率阈值: 50%
- 熔断等待时间: 30秒
- 半开状态允许调用数: 5
- 滑动窗口大小: 10

## 调用流程

### 客户端调用流程
1. **代理拦截**: ProxyFactory创建动态代理对象
2. **服务发现**: 从注册中心获取服务提供者列表
3. **负载均衡**: 根据策略选择目标服务实例
4. **熔断检查**: 检查服务健康状态
5. **异步调用**: 通过HTTP客户端发送请求
6. **同步等待**: 等待异步结果返回

### 服务端处理流程
1. **接收请求**: HTTP服务器接收客户端请求
2. **请求解析**: 解析调用参数和方法信息
3. **服务查找**: 从本地注册表查找实现类
4. **反射调用**: 通过反射执行目标方法
5. **结果返回**: 序列化结果并返回给客户端

## 依赖说明

### 核心依赖
- **Resilience4j**: 熔断器实现
- **Apache HttpClient**: HTTP客户端
- **Tomcat Embed**: 嵌入式HTTP服务器
- **Jackson**: JSON序列化
- **Guava**: 工具类库

### 测试依赖
- **JUnit 5**: 单元测试框架

## 扩展指南

### 自定义负载均衡策略
```java
public class CustomLoadBalance {
    public static URL customStrategy(List<URL> urls) {
        // 实现自定义负载均衡逻辑
        return urls.get(0);
    }
}
```

### 自定义序列化协议
```java
public interface Serializer {
    byte[] serialize(Object obj);
    <T> T deserialize(byte[] data, Class<T> clazz);
}
```

## 性能优化建议

1. **连接池调优**: 根据并发量调整连接池大小
2. **线程池调优**: 根据业务特点调整线程池参数
3. **熔断器调优**: 根据服务稳定性调整熔断阈值
4. **序列化优化**: 选择高效的序列化协议
5. **网络优化**: 启用HTTP Keep-Alive和压缩

## 监控与运维

### 关键指标
- 请求QPS和响应时间
- 服务可用率和错误率
- 线程池使用情况
- 连接池使用情况
- 熔断器状态变化

### 日志配置
```properties
# 启用RPC框架日志
logging.level.com.coldscholor=DEBUG
```

## 常见问题

### Q: 如何处理服务超时？
A: 框架默认异步调用超时时间为30秒，可通过RpcConfig.getAsyncTimeout()调整。

### Q: 如何实现服务版本控制？
A: 在服务注册时指定版本号，支持同一接口的多版本实现。

### Q: 如何处理序列化异常？
A: 框架目前假设返回String类型，可扩展支持复杂对象序列化。

## 贡献指南

1. Fork项目到个人仓库
2. 创建功能分支: `git checkout -b feature/new-feature`
3. 提交更改: `git commit -am 'Add new feature'`
4. 推送分支: `git push origin feature/new-feature`
5. 创建Pull Request

## 许可证

本项目采用MIT许可证，详见LICENSE文件。

## 联系方式

- 作者: 寒士obj
- 邮箱: [your-email@example.com]
- 项目地址: [https://github.com/your-username/RPC-project]

---

**注意**: 这是一个学习性质的RPC框架实现，生产环境使用请谨慎评估并进行充分测试。
