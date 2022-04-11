package com.karen.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.karen.common.enums.CmdEnum;
import com.karen.common.model.BaseMessage;
import com.karen.common.model.LoginBean;
import com.karen.server.server.MsgService;
import com.karen.server.util.SpringContextUtils;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * netty服务端消息处理类.
 * @Author:chensy
 * @Date:15:45 2020/12/25
 * @Description
 * @Modified By:
 */
@Component
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<BaseMessage>{

  Logger log = LoggerFactory.getLogger(this.getClass());

  @Override
  public void channelRead0(ChannelHandlerContext ctx, BaseMessage msg) throws Exception {

    log.info("<<<<< Client said msg:" + msg.toString());
    if(msg.getCmd() == CmdEnum.LOGIN.getCmd()){
      LoginBean loginBean = JSONObject.parseObject(msg.getData(), LoginBean.class);
      String userName = "admin";
      if(userName.equals(loginBean.getUsername())){
        log.info("----- {} login success!", ctx.channel().remoteAddress());
        BaseMessage send = new BaseMessage();
        send.setCmd(CmdEnum.LOGIN_RES.getCmd());
        send.setTopic(0);
        send.setId(System.nanoTime());
        send.setNo((short)0);
        send.setfNum((short)0);
        send.setData("success");
        ctx.writeAndFlush(send);
      }else{
        log.info("----- {} login fails!", ctx.channel().remoteAddress());
        ctx.close();
      }
    }
    if(msg.getCmd() == CmdEnum.DATA.getCmd()){
      log.info("----- {} data msg: {}", ctx.channel().remoteAddress(), msg.getData());
      MsgService service = SpringContextUtils.getBean(MsgService.class);
      service.write(JSONObject.toJSONString(msg));
    }
    if(msg.getCmd() == CmdEnum.SUBSCRIBE.getCmd()){
      log.info("----- {} subscribe msg", ctx.channel().remoteAddress());
      MsgService service = SpringContextUtils.getBean(MsgService.class);
      BaseMessage send = service.read();
      ctx.writeAndFlush(send);
    }
    if(msg.getCmd() == CmdEnum.ANSWER.getCmd()){
      log.info("----- {} answer msg", ctx.channel().remoteAddress());
      MsgService service = SpringContextUtils.getBean(MsgService.class);
      BaseMessage send = service.read();
      if(null != send){
        ctx.writeAndFlush(send);
      }
    }
    ctx.flush();
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.error("----- Connection exception.....");
    cause.printStackTrace();
    ctx.channel().close();
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    super.userEventTriggered(ctx, evt);
    if (evt instanceof IdleStateEvent) {
      IdleStateEvent event = (IdleStateEvent) evt;
      if (event.state().equals(IdleState.READER_IDLE)) {
        /**
         *  长期没收到客户端数据，断开连接
         */
        log.error("----- 长期没收到客户端数据，断开连接.....");
        ctx.close();
      }
      if (event.state().equals(IdleState.WRITER_IDLE)) {
        /**
         *  长期未向客户端发送数据
         *  可以发送心跳包
         */
        BaseMessage message = new BaseMessage(CmdEnum.HEARTBEAT.getCmd(), 0, 0, (short) 0,
            (short) 0, (short) 0, null);
        ctx.writeAndFlush(message);
      }
    }
  }

  @Override
  public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    ctx.flush();
  }

  /**
   * 客户端连接触发.
   */
  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    log.info("----- Client online : {}", ctx.channel().remoteAddress());
    BaseMessage message = new BaseMessage();
    message.setCmd(CmdEnum.SUCCESS.getCmd());
    message.setTopic(0);
    message.setId(System.nanoTime());
    message.setNo((short)0);
    message.setfNum((short)0);
    message.setData("connect success");
    log.info("----- active msg:" + message.toString());
    ctx.write(message);
    ctx.flush();
  }


  /**
   * 客户端断开触发..
   */
  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    log.info("----- Channel inActive......");
  }


}