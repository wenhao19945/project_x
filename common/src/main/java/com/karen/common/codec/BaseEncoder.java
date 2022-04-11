package com.karen.common.codec;

import com.karen.common.model.BaseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseEncoder 编码器
 * @Author wenhao
 * @Date 2021/2/4 19:05
 * @return
 * @Description //TODO
 */
public class BaseEncoder extends MessageToByteEncoder<BaseMessage> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * 加码/转成字节流
   * @author WenHao
   * @date 2022/1/21 16:41
   */
  @Override
  protected void encode(ChannelHandlerContext ctx, BaseMessage msg, ByteBuf out) {
    try{
      msg.toByteBufMsg(out);
      log.info(">>>>> ip:{},len:{},hex:{}", ctx.channel().remoteAddress(), out.readableBytes(), ByteBufUtil.hexDump(out));
    }catch (Exception e){
      e.printStackTrace();
    }
  }

}
