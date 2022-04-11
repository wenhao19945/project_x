package com.karen.client.model;

import com.karen.client.util.DataTransferUtil;
import com.karen.client.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

/**
 * 帧数据.
 * @Author:WenHao
 * @Date:14:34 2020/12/25
 * @Description
 * @Modified By:
 */
@Data
public class SatelliteMessage {
  /**
   * 起始标识 2(byte)
   */
  private int head = 0xfaf5;
  /**
   * 版本号 2(byte)
   */
  private int version;
  /**
   * 报文长度 2(byte)  “发送序列号” 到“DATA”的总长度 (掐头去尾，包含DATA)
   */
  private int length;
  /**
   * 起始标识 1(byte)
   */
  private int frameNum;
  /**
   * 命令字 1(byte)
   */
  private int cmd;
  /**
   * 接收方类型 1(byte)
   */
  private int receiveType;
  /**
   * 发送方类型 1(byte)
   */
  private int sendType;
  /**
   * 接收方地址 4(byte)
   */
  private long receiverAddress;
  /**
   * 发送方地址 4(byte)
   */
  private long senderAddress;
  /**
   * 需要传递的实体类
   */
  private Object data;
  /**
   * 主要数据字节数组 n(byte)
   */
  private byte[] dataByte;
  /**
   * 校验 2(byte)
   */
  private int chk;

  /**
   * toByteBufMsg 生成报文 ByteBuf
   * @Author wenhao
   * @Date 2021/1/29 9:42
   * @return io.netty.buffer.ByteBuf
   * @Description //TODO 在Encoder escape()方法处回收
   */
  public ByteBuf toByteBufMsg() {
    ByteBuf bb = ByteBufAllocator.DEFAULT.heapBuffer();
    bb.writeShort(head);
    bb.writeShort(version);
    bb.writeShort(length);
    bb.writeByte(frameNum);
    bb.writeByte(cmd);
    bb.writeByte(receiveType);
    bb.writeByte(sendType);
    bb.writeInt((int)receiverAddress);
    bb.writeInt((int)senderAddress);
    bb.writeBytes(dataByte);
    bb.writeShort(chk);
    return bb;
  }

  public static SatelliteMessage getPing(){
    SatelliteMessage resp = new SatelliteMessage();
    resp.setHead(0xfaf5);
    resp.setVersion(0x1000);
    resp.setLength(0x13);
    resp.setFrameNum(0x1);
    resp.setCmd(0xFFFFFFF4);
    resp.setReceiveType(0);
    resp.setSendType(0);
    resp.setReceiverAddress((long)1001);
    resp.setSenderAddress((long)1002);
    resp.setChk(0x6f6b);
    byte[] bytes = DataTransferUtil.str2ByteArray(DateUtil.getDateStr(),7);
    resp.setDataByte(bytes);
    return resp;
  }

}
