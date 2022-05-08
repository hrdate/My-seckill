# My-seckill
 简单秒杀和支付demo，~~不考虑集群/微服务~~（已升级重构用Spring Cloud）

~~前后端不分离，只是单纯的在界面请求接口，使用thymeleaf模板引擎 + jquery-validation  + ajax + js + bootsrap~~（重构后，还没成功用Vue.js重写前端项目，目前还只是用Spring Boot项目静态资源展示）

后端技术：Spring Boot + Spring Cloud Alibaba +  Mybatis-Plus + MySQL + Redis + RabbitMQ

测压工具：apache-jmeter-5.4.3

其中选用网关Gateway，注册中心Nacos，远程调用Open Feign，支付组件Payment-Spring Boot

项目描述：

- [x] 使用网关Gateway和注册中心Nacos进行转发路由到具体模块
- [x]  Gateway中配合Redis使用限流RequestRateLimiter(自定义令牌桶规则是主机名)
- [ ] 页面静态化 + 部分对象静态化处理降低网络拥塞 ~~（也可以考虑使用CDN）~~
- [x] 预热秒杀商品基本信息，防止缓存击穿
- [x] Mybatis-Plus操作数据库MySQL的InnoDB引擎执行SQL语句，且同时把用户id + 商品id设置为唯一索引
- [x] 使用RabbitMQ异步操作，流量削峰，设置队列和交换机持久化
- [x] 采用确认和回退机制，把提交失败的信息存入数据库错误表中~~后期可以考虑用定时任务处理~~
- [x] Redis + LUA脚本保证原子性~~（但由于把LUA脚本放在在Java端所以每次都会把该脚本发送给Redis故导致吞吐量降低）~~
- [x] 当秒杀商品为0时，用内存中的变量标识，反之无效的再次访问Redis
- [x] 秒杀接口隐藏，定点修改界面JS
- [x] hutool工具验证码
- [x] 自定义注解拦截器，计数器方法和令牌桶算法，两种接口限流
- [ ] 微信支付Native方式，回调确认支付成功，~~支持查单~~

秒杀下单的关键代码如下图：

<img src="README.assets/image-20220304194039306.png" alt="image-20220304194039306" style="zoom: 80%;" />

使用jmeter测试当前情况下秒杀商品，设置线程组为1000线程数，循环10次

![test-result](README.assets/test-result.jpg)



数据库分表情况

![image-20220227200705610](README.assets/image-20220227200705610.png)



页面效果展示

登录界面

![image-20220227200206739](README.assets/image-20220227200206739.png)

商品列表界面

![image-20220227200448012](README.assets/image-20220227200448012.png)

商品详情界面

![image-20220227200455825](README.assets/image-20220227200455825.png)

成功秒杀后进入商品订单界面

<img src="README.assets/image-20220227200539573.png" alt="image-20220227200539573" style="zoom: 67%;" />



![image-20220227200554413](README.assets/image-20220227200554413.png)

