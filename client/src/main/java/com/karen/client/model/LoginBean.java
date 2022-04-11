package com.karen.client.model;

import com.karen.client.util.DataTransferUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import lombok.Data;

/**
 * @ClassName: LoginBean
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/1/13 16:49
 * @Modified By:
 */
@Data
public class LoginBean {
    /**
     * 组织名称编码
     */
    private byte id1;
    /**
     * 登录时间
     */
    private String time7;
    /**
     * 终端类型
     */
    private byte type1;
    /**
     * 用户名
     */
    private String username10;
    /**
     * 密码
     */
    private String password20;
    /**
     * 企业平台当前连接支持车载终端上限数
     */
    private int num4;

    public ByteBuf getByte(){
        ByteBuf res = ByteBufAllocator.DEFAULT.heapBuffer();
        res.writeByte(this.id1);
        res.writeBytes(DataTransferUtil.str2ByteArray(this.time7,7));
        res.writeByte(this.type1);
        res.writeBytes(DataTransferUtil.str2ByteArray(this.username10,10));
        res.writeBytes(DataTransferUtil.str2ByteArray(this.password20,20));
        res.writeBytes(DataTransferUtil.intToByteArray(this.num4));
        return res;
    }

}
