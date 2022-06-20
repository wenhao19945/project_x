package com.karen.client.codec;

import com.karen.client.model.SatelliteMessage;
import com.karen.client.model.LoginRet;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * 解码器.
 * @Author:chensy
 * @Date:15:44 2020/2/4
 * @Description
 * @Modified By:
 */
@Slf4j
public class SatelliteDecoder extends MessageToMessageDecoder<ByteBuf> {

  /**
   * SatelliteDecoder decode 协议解析
   * @Author wenhao
   * @Date 2020/2/4 17:49
   * @param channelHandlerContext  通道
   * @param byteBuf  入参
   * @param list  出参，传递到NettyClientHandler
   * @return void
   * @Description //TODO
   */
  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

    SatelliteMessage message = new SatelliteMessage();

    if (!byteBuf.isReadable()){
      log.info("byteBuf have no data");
    } else {
      log.info("<<<<< ip:{},hex:{}", channelHandlerContext.channel().remoteAddress(), ByteBufUtil.hexDump(byteBuf));
      /*byte[] raw = new byte[byteBuf.readableBytes()];
      byteBuf.readBytes(raw);*/
      int len = 21;
      if(byteBuf.readableBytes() < len){
        log.info("byteBuf 长度不满足规范");
        list.add(message);
      }else{

        message.setHead(byteBuf.readShort());
        message.setVersion(byteBuf.readShort());
        message.setLength(byteBuf.readShort());
        message.setFrameNum(byteBuf.readByte());
        message.setCmd(byteBuf.readByte());
        message.setReceiveType(byteBuf.readByte());
        message.setSendType(byteBuf.readByte());
        message.setReceiverAddress(byteBuf.readInt());
        message.setSenderAddress(byteBuf.readInt());

        /**
         * 登录应答
         */
        if(0xFFFFFFF1 == message.getCmd()){
          LoginRet loginRet = new LoginRet();
          loginRet.setCode(byteBuf.readByte());
          loginRet.setHeartbeat(byteBuf.readByte());
          loginRet.setAuth(byteBuf.readByte());
          loginRet.setAds(byteBuf.readInt());
          loginRet.setPort(byteBuf.readShort());
          message.setData(loginRet);
          message.setChk(byteBuf.readShort());
        }

        list.add(message);

      }

    }

  }

}
