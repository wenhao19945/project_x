package com.karen.client.client;

import com.karen.client.config.ChannelManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * @ClassName: NettyClient
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/2/2 16:35
 * @Modified By: Netty客户端
 */
@Slf4j
@Component
public class NettyClient {

    @Value("${netty.url}")
    private String host;
    @Value("${netty.port}")
    private int port;
    @Value("${netty.channel}")
    private String channel;

    @Autowired
    @Qualifier("bossGroup")
    private NioEventLoopGroup bossGroup;

    @Autowired
    @Qualifier("workerGroup")
    private NioEventLoopGroup workerGroup;

    @Autowired
    @Qualifier("businessGroup")
    private EventExecutorGroup businessGroup;

    @Autowired
    private NettyChannelInitializer nettyChannelInitializer;

    @Autowired
    private ChannelManager channelManager;

    /**
     * NettyClient start
     * @Author wenhao
     * @Date 2021/2/2 18:57
     * @return boolean
     * @Description //TODO 启动TCP连接
     */
    @PostConstruct
    public void start() throws Exception{
        try {
            Bootstrap b = new Bootstrap();
            b.group(bossGroup)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(nettyChannelInitializer);
            log.info(">>>>> Connecting to the server ----> tcp://"+host+":"+port);
            ChannelFuture cf = b.connect().sync();
            log.info("----- Connection successful! ChannelKey: {}", channelManager.add(channel, cf.channel()));

            log.info("----- Connection close");//异步等待关闭连接channel
            cf.channel().closeFuture().sync();
        }catch (Exception e){
            log.info("----- Connection failed");
            e.printStackTrace();
        }finally {
            // 释放线程池资源
            bossGroup.shutdownGracefully().sync();
            //start();
        }
    }

    /**
     * NettyClient destroy
     * @Author wenhao
     * @Date 2021/2/2 18:57
     * @return void
     * @Description //TODO 销毁
     */
    @PreDestroy
    public void destroy() {
        bossGroup.shutdownGracefully().syncUninterruptibly();
        workerGroup.shutdownGracefully().syncUninterruptibly();
        businessGroup.shutdownGracefully().syncUninterruptibly();
        log.info("----- Netty destroy");
    }

}
