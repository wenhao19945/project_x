package com.karen.sub.client;

import com.karen.common.codec.BaseDecoder;
import com.karen.common.codec.BaseEncoder;
import com.karen.sub.handler.NettyClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PreDestroy;

/**
 * @ClassName: NettyClient
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/2/2 16:35
 * @Modified By: Netty客户端
 */
@Slf4j
public class NettyClient {

  private String host;
  private int port;
  private NioEventLoopGroup bossGroup;
  private EventExecutorGroup businessGroup;
  private ChannelFuture channelFuture;
  private Bootstrap bootstrap = new Bootstrap();
  protected volatile SslContext sslContext;

  public NettyClient() {

  }

  public NettyClient(String host, int post) {
    this.host = host;
    this.port = post;
    this.bossGroup = new NioEventLoopGroup(1);
    this.businessGroup = new DefaultEventExecutorGroup(10);
  }

  /**
   * NettyClient start
   *
   * @return boolean
   * @Author wenhao
   * @Date 2021/2/2 18:57
   * @Description //TODO 启动TCP连接
   */
  public ChannelFuture start() {
    try {
      this.bootstrap.group(this.bossGroup).channel(NioSocketChannel.class)
          //启用或关闭Nagle算法。如果要求高实时性，有数据发送时就马上发送，就将该选项设置为true关闭Nagle算法；如果要减少发送次数减少网络交互，就设置为false等累积一定大小后再发送。默认为false。
          .option(ChannelOption.TCP_NODELAY, true)
          //在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
          .option(ChannelOption.SO_KEEPALIVE, false)
          //连接超时时间
          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
          //发送缓冲区大小
          .option(ChannelOption.SO_SNDBUF, 65535)
          //接收缓冲区大小
          .option(ChannelOption.SO_RCVBUF, 65535)
          //水位设置，设置某个连接上可以暂存的最大最小Buffer  1M - 4M
          .option(ChannelOption.WRITE_BUFFER_WATER_MARK, new WriteBufferWaterMark(1048576, 4194304))
          .handler(
              new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                  // ch.pipeline().addFirst("sslHandler", sslContext.newHandler(ch.alloc()));
                  ch.pipeline().addLast(businessGroup,
                      new BaseEncoder(),
                      new BaseDecoder(),
                      new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS),
                      //new LengthFieldBasedFrameDecoder(2<<8, 2, 4, 0, 0),
                      new NettyClientHandler()
                  );
                }
              }
          );
      log.info(">>>>> Connecting to the server ----> tcp://" + this.host + ":" + this.port);
      //发起连接，非阻塞
      this.channelFuture = this.bootstrap.connect(new InetSocketAddress(this.host, this.port));
      if(this.channelFuture.channel().isOpen()){
        log.info("----- Connection successful!");
      }
    } catch (Exception e) {
      log.info("----- Connection failed");
      e.printStackTrace();
      destroy();
    }
    return channelFuture;
  }

  /**
   * NettyClient destroy
   *
   * @return void
   * @Author wenhao
   * @Date 2021/2/2 18:57
   * @Description //TODO 销毁
   */
  @PreDestroy
  public void destroy() {
    log.info("----- Netty destroy");
    if (null != this.bossGroup) {
      this.bossGroup.shutdownGracefully().syncUninterruptibly();
    }
    if (null != this.bossGroup) {
      this.bossGroup.shutdownGracefully().syncUninterruptibly();
    }
    if (null != this.businessGroup) {
      this.businessGroup.shutdownGracefully().syncUninterruptibly();
    }
  }

}
