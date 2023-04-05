package com.iflytek.gulimall.order;


import com.iflytek.gulimall.order.config.DataSourceProxyAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.session.config.annotation.web.http.EnableSpringHttpSession;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * RabbitListener可用于方法和类上
 * RabbitHandler只能用于方法上,当消息为不同的实体类,提供不同的重载方法
 *
 */


/**
 * 本地事务失效问题
 * 同一个对象内事务互相默认失效，原因 绕过了代理对象，事务使用代理对象来控制的
 * 解决：使用代理对象来调用事务方法
 */

/**
 *  * Seata控制分布式事务
 *  *  1）、每一个微服务必须创建undo_Log
 *  *  2）、安装事务协调器：seate-server
 *  *  3）、整合
 *  *      1、导入依赖
 *  *      2、解压并启动seata-server：
 *  *          registry.conf:注册中心配置    修改 registry ： nacos
 *  *      3、所有想要用到分布式事务的微服务使用seata DataSourceProxy 代理自己的数据源
 *  *      4、每个微服务，都必须导入   registry.conf   file.conf
 *  *          vgroup_mapping.{application.name}-fescar-server-group = "default"
 *  *      5、启动测试分布式事务
 *  *      6、给分布式大事务的入口标注@GlobalTransactional
 *  *      7、每一个远程的小事务用@Trabsactional
 *  */


@MapperScan("com.iflytek.gulimall.order.dao")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.iflytek.gulimall.common.feign"})
@EnableRedisHttpSession
@EnableRabbit
@Import({DataSourceProxyAutoConfiguration.class})
@Slf4j
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class},
        scanBasePackages = {"com.iflytek.gulimall.order","com.iflytek.gulimall.common"})
public class GulimallOrderApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(GulimallOrderApplication.class, args);
        Environment env = application.getEnvironment();
        log.info("\n----------------------------------------------------------\n\t" +
                        "应用 '{}' 运行成功! 访问连接:\n\t" +
                        "数据库监控: \t\thttp://{}:{}{}/druid\n" +
                        "----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                StringUtils.isEmpty(env.getProperty("server.servlet.context-path"))?"":env.getProperty("server.servlet.context-path"));
    }
    @Bean
    public MessageConverter createMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
