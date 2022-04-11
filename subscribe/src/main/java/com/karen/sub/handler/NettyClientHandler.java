package com.karen.sub.handler;

import com.alibaba.fastjson.JSONObject;
import com.karen.common.config.Config;
import com.karen.common.enums.CmdEnum;
import com.karen.common.model.BaseMessage;
import com.karen.common.model.LoginBean;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @ClassName: NettyClientHandler
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/2/2 16:47
 * @Modified By:
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class NettyClientHandler extends SimpleChannelInboundHandler<BaseMessage> {

  /**
   * 添加通道 handlerAdded先执行，channelActive后执行，handlerRemoved在失去连接时执行
   */
  @Override
  public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    super.handlerAdded(ctx);
  }

  /**
   * 通道建立成功了，可以回复登录报文
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("----- Client channelActive ");
    log.info(">>>>> 登录报文发送");
    LoginBean loginBean = new LoginBean();
    loginBean.setUsername("admin");
    loginBean.setPassword("admin12345");
    String data = JSONObject.toJSONString(loginBean);
    byte[] dataBytes = data.getBytes(Config.CHARSET);
    BaseMessage msg = new BaseMessage(CmdEnum.LOGIN.getCmd(), 0, System.nanoTime(), (short)1, (short)1, dataBytes.length, data);
    ctx.writeAndFlush(msg);
  }

  /**
   * 收到消息调用channelRead SimpleChannelInboundHandler 重写了channelRead 方法 加入了channelRead0 执行完之后自动释放
   */
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {

    log.info("server msg:" + msg.getCmd());
    if(CmdEnum.LOGIN_RES.getCmd() == msg.getCmd() && msg.getData().equals("success")){
      String data = "sub";
      byte[] dataBytes = data.getBytes(Config.CHARSET);
      BaseMessage send = new BaseMessage(CmdEnum.SUBSCRIBE.getCmd(), 0, System.nanoTime(), (short)1, (short)1, dataBytes.length, data);
      ctx.writeAndFlush(send);
    }

    if(msg.getCmd() == CmdEnum.DATA.getCmd()){
      log.info("///// {} data msg: {}", ctx.channel().remoteAddress(), msg.getData());
      String data = "answer";
      byte[] dataBytes = data.getBytes(Config.CHARSET);
      BaseMessage send = new BaseMessage(CmdEnum.ANSWER.getCmd(), 0, System.nanoTime(), (short)1, (short)1, dataBytes.length, data);
      ctx.writeAndFlush(send);
    }

    ctx.flush();

  }

  /**
   * channelRead表示接收消息，可以看到msg转换成了ByteBuf，然后打印，也就是把Client传过来的消息打印了一下，
   * 你会发现每次打印完后，channelReadComplete也会调用，如果你试着传一个超长的字符串过来，超过1024个字母长度，
   * 你会发现channelRead会调用多次，而channelReadComplete只调用一次。所以这就比较清晰了吧，因为ByteBuf是有长度限制的，
   * 所以超长了，就会多次读取，也就是调用多次channelRead，而channelReadComplete则是每条消息只会调用一次，无论你多长，
   * 分多少次读取，只在该条消息最后一次读取完成的时候调用，所以这段代码把关闭Channel的操作放在channelReadComplete里，
   * 放到channelRead里可能消息太长了，结果第一次读完就关掉连接了，后面的消息全丢了。
   */
  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    super.channelReadComplete(ctx);
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state().equals(IdleState.READER_IDLE)) {
        /**
         *  长期没收到服务器推送数据
         *  可以选择重新连接
         */
      } else if (event.state().equals(IdleState.WRITER_IDLE)) {
        /**
         *  长期未向服务器发送数据
         *  可以发送心跳包
         */
        BaseMessage message = new BaseMessage(CmdEnum.HEARTBEAT.getCmd(), 0, 0, (short) 0,
            (short) 0, (short) 0, "heartbeat");
        ctx.writeAndFlush(message);
      } else if (event.state().equals(IdleState.ALL_IDLE)) {
        System.out.println("ALL");
      }
    }
  }

  /**
   * 抛出异常通常会执行到这里，并断开连接
   */
  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.info("----- exceptionCaught ");
    cause.printStackTrace();
    ctx.close();
  }

  /**
   * 通道关闭了
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    //这里可以断线重连
    log.info("----- Channel inactive...");
  }

  /**
   * 通道被移除
   */
  @Override
  public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    super.handlerRemoved(ctx);
  }
}
