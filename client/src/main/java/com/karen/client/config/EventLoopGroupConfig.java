package com.karen.client.config;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @ClassName: EventLoopGroupConfig
 * @Author: WenHao
 * @Description: 使用ChannelGroup管理Channel, 维护terminalPhone->ChannelId->Channel 一对一映射关系
 * @Date Created in 2021/1/11 17:24
 * @Modified By:
 */
@Configuration
public class EventLoopGroupConfig {

    @Value("${netty.threads.boss}")
    private int bossThreadsNum;

    @Value("${netty.threads.worker}")
    private int workerThreadsNum;

    @Value("${netty.threads.business}")
    private int businessThreadsNum;

    /**
     * 负责TCP连接建立操作 绝对不能阻塞
     * @return
     */
    @Bean(name = "bossGroup")
    public NioEventLoopGroup bossGroup() {
        return new NioEventLoopGroup(bossThreadsNum);
    }

    /**
     * 负责Socket读写操作 绝对不能阻塞
     * @return
     */
    @Bean(name = "workerGroup")
    public NioEventLoopGroup workerGroup() {
        return new NioEventLoopGroup(workerThreadsNum);
    }

    /**
     * Handler中出现IO操作(如数据库操作，网络操作)使用这个
     * @return
     */
    @Bean(name = "businessGroup")
    public EventExecutorGroup businessGroup() {
        return new DefaultEventExecutorGroup(businessThreadsNum);
    }

}
