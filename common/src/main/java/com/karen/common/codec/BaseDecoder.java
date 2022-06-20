package com.karen.common.codec;

import com.karen.common.model.BaseMessage;
import com.karen.common.util.CrcUtils;
import com.karen.common.config.Config;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseDecoder解码器.
 * @author WenHao
 * @date 2020/1/20 16:23
 */
public class BaseDecoder extends MessageToMessageDecoder<ByteBuf> {

  Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * 统一解码
   * @author WenHao
   * @date 2020/1/21 16:40
   */
  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {

    if (!byteBuf.isReadable()){
      return;
    }
    int hexLen = byteBuf.readableBytes();
    if(hexLen < Config.MIN_LENGTH){
      return;
    }
    log.info("<<<<< ip:{},len:{},hex:{}", channelHandlerContext.channel().remoteAddress(), hexLen, ByteBufUtil.hexDump(byteBuf));
    // crc校验
    byte[] data = new byte[hexLen - 4];
    byteBuf.readBytes(data);
    int crc = byteBuf.readInt();
    int dataCrc = CrcUtils.crc32(data);
    if(crc != dataCrc){
      log.info("------- crc false :" + crc + "!=" + dataCrc);
      return;
    }
    // 再写入
    byteBuf.writeBytes(data);
    short head = byteBuf.readShort();
    if(Config.HEAD != head){
      return;
    }
    BaseMessage message = new BaseMessage();
    message.setHead(head);
    message.setCmd(byteBuf.readShort());
    message.setTopic(byteBuf.readInt());
    message.setId(byteBuf.readLong());
    message.setNo(byteBuf.readShort());
    message.setfNum(byteBuf.readShort());
    message.setDataLen(byteBuf.readInt());
    if(message.getDataLen() > 0){
      byte[] bytes = new byte[message.getDataLen()];
      byteBuf.readBytes(bytes);
      message.setData(new String(bytes, Config.CHARSET));
    }
    message.setChk(crc);
    list.add(message);

  }

}
