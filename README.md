# My-seckill
 简单秒杀demo，不考虑集群/微服务

前后端不分离，只是单纯的在界面请求接口，使用thymeleaf模板引擎 + jquery-validation  + ajax + js + bootsrap

后端技术：Spring Boot + Mybatis-Plus + MySQL + Redis + RabbitMQ

测压工具：apache-jmeter-5.4.3

项目描述：

- [ ] 页面静态化 + 部分对象静态化处理降低网络拥塞 ~~（也可以考虑使用CDN）~~
- [ ] 预热秒杀商品基本信息，防止缓存击穿
- [ ] Mybatis-Plus操作数据库MySQL的InnoDB引擎执行SQL语句，且同时把用户id + 商品id设置为唯一索引
- [ ] 使用RabbitMQ异步操作，流量削峰
- [ ] Redis + LUA脚本保证原子性~~（但由于把LUA脚本放在在Java端所以每次都会把该脚本发送给Redis故导致吞吐量降低）~~
- [ ] 秒杀接口隐藏
- [ ] hutool工具验证码
- [ ] 计数器方法接口限流

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

