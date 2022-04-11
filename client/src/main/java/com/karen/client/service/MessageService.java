package com.karen.client.service;

import com.alibaba.fastjson.JSON;
import com.karen.client.config.ChannelManager;
import com.karen.client.model.LoginBean;
import com.karen.client.model.SatelliteMessage;
import com.karen.client.util.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @ClassName: MessageService
 * @Author: wenhao
 * @Description:
 * @Date Created in 2021/2/4 17:03
 * @Modified By:
 */
@Slf4j
@Service
public class MessageService {

    @Autowired
    private ChannelManager channelManager;

    @Value("${netty.channel}")
    private String channel;

    /**
     * Channel ctx = channelManager.get(channel);
     */

    public void pushLoginMsgByChannel() {
        Channel ctx = channelManager.get(channel);
        pushLoginMsg(ctx);
    }

    public void pushMsg(SatelliteMessage resp,Channel ctx){
        ctx.writeAndFlush(resp);
        log.info(">>>>> PushMsg ---> data: {}", JSON.toJSONString(resp));
    }

    public void pushMsgDef(SatelliteMessage resp){
        Channel ctx = channelManager.get(channel);
        ctx.writeAndFlush(resp);
        log.info(">>>>> PushMsgDef ---> data: {}", JSON.toJSONString(resp));
    }

    /**
     * MessageService loginMsg 发送登录报文
     * @Author wenhao
     * @Date 2021/2/4 17:06
     * @return void
     * @Description //TODO
     */
    public void pushLoginMsg(Channel ctx) {
        SatelliteMessage resp = getlogBean();
        ctx.writeAndFlush(resp);
        log.info(">>>>> Push msg: {}", JSON.toJSONString(resp));
    }

    public SatelliteMessage getlogBean(){

        LoginBean loginBean = new LoginBean();
        loginBean.setId1((byte)0);
        loginBean.setTime7(DateUtil.getDateStr());
        loginBean.setType1((byte)0);
        loginBean.setUsername10("szbus");
        loginBean.setPassword20("szbus2020");
        loginBean.setNum4(8);

        ByteBuf res = loginBean.getByte();
        byte[] bytes = new byte[res.readableBytes()];
        res.readBytes(bytes);

        SatelliteMessage resp = getSatelliteMessageDef(bytes);

        //释放ByteBuf
        ReferenceCountUtil.safeRelease(res);
        return resp;
    }

    /**
     * getSatelliteMessageDef
     * @Author wenhao
     * @Date 2021/2/4 17:37
     * @param bytes  数据体字节数组
     * @return SatelliteMessage
     * @Description //TODO
     */
    public SatelliteMessage getSatelliteMessageDef(byte[] bytes){
        SatelliteMessage resp = new SatelliteMessage();
        resp.setHead(0xfaf5);
        resp.setVersion(0x1000);
        resp.setLength(0x15);
        resp.setFrameNum(0x1);
        resp.setCmd(0xFFFFFFF0);
        resp.setReceiveType(0000);
        resp.setSendType(0000);
        resp.setReceiverAddress((long)1111);
        resp.setSenderAddress((long)2222);
        resp.setDataByte(bytes);
        resp.setChk(0x5321);
        return resp;
    }

}
