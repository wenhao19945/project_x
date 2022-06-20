package com.karen.common.model;

import com.karen.common.util.CrcUtils;
import com.karen.common.config.Config;
import com.karen.common.util.ByteUtils;
import io.netty.buffer.ByteBuf;
import java.io.Serializable;

/**
 * 帧数据.
 * 2+2+4+8+2+2+4+n+4  至少30个字节
 * @author WenHao
 * @date 2020/1/19 18:24
 */
public class BaseMessage implements Serializable {

  public static final long serialVersionUID = 1L;

  /**
   * 起始标识 2(byte)
   */
  private short head;
  /**
   * 起始标识 2(byte)
   */
  private short cmd;
  /**
   * topic 标题title  4(byte)
   */
  private int topic;
  /**
   * 消息标识   8(byte)
   */
  private long id;
  /**
   * 序号（第几帧）2(byte)
   */
  private short no;
  /**
   * 有多少帧 2(byte)
   */
  private short fNum;
  /**
   * 数据长度 4(byte)  DATA的总长度
   */
  private int dataLen;
  /**
   * 需要传递的数据 任意长度
   */
  private String data;
  /**
   * 校验 4(byte)
   */
  private int chk;

  public BaseMessage(){
    this.head = Config.HEAD;
  }

  public BaseMessage(short cmd, int topic, long id, short no, short fNum, int dataLen,
      String data) {
    this.cmd = cmd;
    this.topic = topic;
    this.id = id;
    this.no = no;
    this.fNum = fNum;
    this.dataLen = dataLen;
    this.data = data;
  }

  /**
   * toByteBufMsg 生成报文 ByteBuf
   * @Author wenhao
   * @Date 2020/1/29 9:42
   * @return io.netty.buffer.ByteBuf
   * @Description //TODO 在Encoder escape()方法处回收
   */
  public ByteBuf toByteBufMsg(ByteBuf byteBuf) throws Exception{
    try{
      byteBuf.writeBytes(ByteUtils.shortTo2Byte(Config.HEAD));
      byteBuf.writeBytes(ByteUtils.shortTo2Byte(this.cmd));
      byteBuf.writeBytes(ByteUtils.intToByteArray(this.topic));
      byteBuf.writeBytes(ByteUtils.longToByteArray(this.id));
      byteBuf.writeBytes(ByteUtils.shortTo2Byte(this.no));
      byteBuf.writeBytes(ByteUtils.shortTo2Byte(this.fNum));
      if(null != this.data && this.data.length() > 0){
        byte[] dataBytes = this.data.getBytes(Config.CHARSET);
        byteBuf.writeInt(dataBytes.length);
        byteBuf.writeBytes(dataBytes);
      }else{
        byteBuf.writeBytes(ByteUtils.intToByteArray(0));
      }
      byte[] bytes = new byte[byteBuf.readableBytes()];
      byteBuf.readBytes(bytes);
      // 获取crc校验码
      int crc = CrcUtils.crc32(bytes);
      byteBuf.writeBytes(bytes);
      byteBuf.writeBytes(ByteUtils.intToByteArray(crc));
      return byteBuf;
    }catch (NullPointerException e) {
      throw new RuntimeException("BaseMessage field cannot be null!");
    }
  }

  public short getHead() {
    return head;
  }

  public void setHead(short head) {
    this.head = head;
  }

  public short getCmd() {
    return cmd;
  }

  public void setCmd(short cmd) {
    this.cmd = cmd;
  }

  public int getTopic() {
    return topic;
  }

  public void setTopic(int topic) {
    this.topic = topic;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public short getNo() {
    return no;
  }

  public void setNo(short no) {
    this.no = no;
  }

  public short getfNum() {
    return fNum;
  }

  public void setfNum(short fNum) {
    this.fNum = fNum;
  }

  public int getDataLen() {
    return dataLen;
  }

  public void setDataLen(int dataLen) {
    this.dataLen = dataLen;
  }

  public String getData() {
    return data;
  }

  public void setData(String data) {
    this.data = data;
  }

  public int getChk() {
    return chk;
  }

  public void setChk(int chk) {
    this.chk = chk;
  }

  @Override
  public String toString() {
    return "BaseMessage{" +
        "head=" + head +
        ", cmd=" + cmd +
        ", topic=" + topic +
        ", id=" + id +
        ", no=" + no +
        ", fNum=" + fNum +
        ", dataLen=" + dataLen +
        ", data='" + data + '\'' +
        ", chk=" + chk +
        '}';
  }

}
