package com.karen.client.client;

import com.karen.common.codec.BaseDecoder;
import com.karen.common.codec.BaseEncoder;
import com.karen.client.handler.NettyClientHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.EventExecutorGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName: NettyChannelInitializer
 * @Author: wenhao
 * @Description: netty管道初始化
 * @Date Created in 2020/1/11 17:24
 * @Modified By:
 */
@Component
public class NettyChannelInitializer extends ChannelInitializer<SocketChannel> {

  @Autowired
  @Qualifier("businessGroup")
  private EventExecutorGroup businessGroup;

  @Autowired
  @Qualifier("bossGroup")
  private NioEventLoopGroup bossGroup;

  @Autowired
  @Qualifier("workerGroup")
  private NioEventLoopGroup workerGroup;

  @Autowired
  private NettyClientHandler serverHandler;

  @Override
  protected void initChannel(SocketChannel ch) throws Exception {
    ChannelPipeline pipeline = ch.pipeline();
    /**
     * 第一个参数  表示读操作空闲时间
     * 第二个参数  表示写操作空闲时间
     * 第三个参数  表示读写操作空闲时间
     * 第四个参数  单位/秒
     */
    pipeline.addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
    //添加解码器
    pipeline.addLast(new BaseDecoder());
    //添加编码器
    pipeline.addLast(new BaseEncoder());
    //因为locationMsgHandler中涉及到数据库操作，所以放入businessGroup
    pipeline.addLast(businessGroup, serverHandler);
  }

}
