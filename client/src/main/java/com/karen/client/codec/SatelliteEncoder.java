package com.karen.client.codec;

import com.karen.client.model.SatelliteMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SatelliteEncoder 编码器
 * @Author wenhao
 * @Date 2020/2/4 19:05
 * @return
 * @Description //TODO
 */
@Slf4j
@Component
public class SatelliteEncoder extends MessageToByteEncoder<SatelliteMessage> {

  /**
   * SatelliteEncoder encode 加码/转成字节流
   * @Author wenhao
   * @Date 2020/2/4 19:47
   * @param ctx  通道
   * @param msg  入参
   * @param out  出参 传递到MessageToByteEncoder发送
   * @return void
   * @Description //TODO
   */
  @Override
  protected void encode(ChannelHandlerContext ctx, SatelliteMessage msg, ByteBuf out) {
    try{
      ByteBuf escape = msg.toByteBufMsg();
      log.info(">>>>> ip:{},hex:{}", ctx.channel().remoteAddress(), ByteBufUtil.hexDump(escape));
      out.writeBytes(escape);
      ReferenceCountUtil.safeRelease(escape);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

}
