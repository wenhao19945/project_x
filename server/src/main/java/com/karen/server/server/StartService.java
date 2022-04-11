package com.karen.server.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author WenHao
 * @ClassName StartService
 * @date 2022/1/20 17:48
 * @Description
 */
@Component
public class StartService implements ApplicationRunner {

  Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  NettyTcpServer server;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("run----------------");
    server.start();
  }

}
