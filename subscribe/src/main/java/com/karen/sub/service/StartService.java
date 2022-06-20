package com.karen.sub.service;

import com.karen.sub.client.NettyClient;
import com.karen.sub.config.NettyConfig;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author WenHao
 * @ClassName StartService
 * @date 2021/1/18 17:16
 * @Description
 */
@Component
public class StartService implements ApplicationRunner {

  Logger log = LoggerFactory.getLogger(this.getClass());

  @Autowired
  NettyConfig config;

  @Override
  public void run(ApplicationArguments args) throws Exception {
    log.info("run----------------");
    NettyClient client = new NettyClient(config.host, config.port);
    ChannelFuture future = client.start();
    //异步等待关闭连接channel
    //future.channel().closeFuture().sync();
  }

}
