package com.karen.client.model;

import com.karen.client.util.DataTransferUtil;
import lombok.Data;

/**
 * @ClassName: LoginRet
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/1/28 20:00
 * @Modified By:
 */
@Data
public class LoginRet {
    /**
     * 登录结果1成功 2失败
     */
    private byte code;
    /**
     * 心跳间隔（秒）
     */
    private byte heartbeat;
    /**
     * 是否认证方案
     */
    private byte auth;
    /**
     * 接入网关地址
     */
    private int ads;
    /**
     * 接入网关端口
     */
    private short port;

    /**
     * getByte
     * @Author wenhao
     * @Date 2021/1/29 10:36
     * @return byte[]
     * @Description //TODO bean to byte[]
     */
    public byte[] getByte(){
        byte[] bytes = new byte[9];
        bytes[0] = code;
        bytes[1] = heartbeat;
        bytes[2] = auth;
        byte[] adsByte = DataTransferUtil.intToByteArray(ads);
        bytes[3] = adsByte[0];
        bytes[4] = adsByte[1];
        bytes[5] = adsByte[2];
        bytes[6] = adsByte[3];
        byte[] portByte = DataTransferUtil.shortToByteArray(port);
        bytes[7] = portByte[0];
        bytes[8] = portByte[1];
        return bytes;
    }

}
