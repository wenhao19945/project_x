package com.karen.sub.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * NettyConfig.
 * @author WenHao
 * @date 2021/1/18 17:15
 */
@Slf4j
@Configuration
public class NettyConfig {

    @Value("${netty.url}")
    public String host;
    @Value("${netty.port}")
    public int port;

}
