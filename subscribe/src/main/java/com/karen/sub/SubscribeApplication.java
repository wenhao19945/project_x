package com.karen.sub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @ClassName: Application
 * @Author: wenhao
 * @Description: 启动类
 * @Date Created in 2020/12/24 14:17
 * @Modified By:
 */
@SpringBootApplication
@EnableScheduling
public class SubscribeApplication {

  public static void main(String[] args) {
    SpringApplication.run(SubscribeApplication.class, args);
  }

}
