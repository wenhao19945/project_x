package com.karen.server.server;

import com.karen.common.codec.BaseDecoder;
import com.karen.common.codec.BaseEncoder;
import com.karen.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import java.net.InetSocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


/**
 * netty管道初始化.
 * @author WenHao
 * @date 2022/1/20 19:02
 * @return
 */
@Component
public class NettyTcpServer {

    Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${netty.port}")
    private int port;

    /**
     * netty服务
     */
    private final ServerBootstrap serverBootstrap;

    /**
     * Selector线程池
     */
    private final EventLoopGroup eventLoopGroupSelector;

    /**
     * boss线程池
     */
    private final EventLoopGroup eventLoopGroupBoss;

    /**
     * 事件处理线程池
     */
    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    /**
     * 构造线程池
     * @author WenHao
     * @date 2022/1/20 19:06
     * @return
     */
    public NettyTcpServer(){

        this.serverBootstrap = new ServerBootstrap();

        this.eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyNIOBoss_%d", this.threadIndex.incrementAndGet()));
            }
        });

        this.eventLoopGroupSelector = new NioEventLoopGroup(3, new ThreadFactory() {
            private AtomicInteger threadIndex = new AtomicInteger(0);
            private int threadTotal = 3;

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
            }
        });

    }

    /**
     * 启动Server
     * TODO 仅 liunx 支持 EpollServerSocketChannel.class
     * @author WenHao
     * @date 2022/1/20 19:07
     */
    public void start() {

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
            8,
            new ThreadFactory() {

                private AtomicInteger threadIndex = new AtomicInteger(0);

                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "NettyServerEventThread_" + this.threadIndex.incrementAndGet());
                }
            });

        this.serverBootstrap.group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(NioServerSocketChannel.class)
                //服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //立即写出
                .childOption(ChannelOption.TCP_NODELAY, true)
                //在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(new InetSocketAddress(this.port))
                .childHandler(
                    new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                .addLast(defaultEventExecutorGroup,
                                    new BaseEncoder(),
                                    new BaseDecoder(),
                                    new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS),
                                    //new LengthFieldBasedFrameDecoder(2<<8, 2, 4, 0, 0),
                                    new ServerHandler()
                                );
                        }
                    }
                );

        //内存泄漏检测 开发推荐PARANOID 线上SIMPLE
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.SIMPLE);

        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            if (sync.isSuccess()) {
                log.info("----- netty-server server started! ,port={}", port);
            }
        } catch (InterruptedException e1) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() InterruptedException", e1);
        }

    }

    public void shutdown() {

        try {

            // 异步shutdownGracefully().syncUninterruptibly()
            this.eventLoopGroupBoss.shutdownGracefully();

            this.eventLoopGroupSelector.shutdownGracefully();

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }

        } catch (Exception e) {
            log.error("NettyRemotingServer shutdown exception, ", e);
        }

    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        this.shutdown();
        log.info("------ Netty destroy!");
    }


}
