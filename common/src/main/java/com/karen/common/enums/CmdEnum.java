package com.karen.common.enums;

/**
 * CmdEnum.
 * @author WenHao
 * @date 2022/1/21 16:52
 */
public enum CmdEnum {

  /**
   * 连接建立成功时服务端自动下发，无需应答.
   */
  SUCCESS(100),
  /**
   * 登录验证.
   */
  LOGIN(110),
  /**
   * 登录应答.
   */
  LOGIN_RES(120),
  /**
   * 心跳.
   */
  HEARTBEAT(200),
  /**
   * 数据.
   */
  DATA(300),
  /**
   * 数据应答.
   */
  ANSWER(310),
  /**
   * 订阅.
   */
  SUBSCRIBE(400);


  CmdEnum(int cmd){
    this.cmd = (short)cmd;
  }

  private short cmd;

  public short getCmd() {
    return cmd;
  }

  public void setCmd(short cmd) {
    this.cmd = cmd;
  }

}
