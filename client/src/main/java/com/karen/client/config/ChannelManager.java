package com.karen.client.config;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @ClassName: ChannelManager
 * @Author: WenHao
 * @Description: 使用ChannelGroup管理Channel, 维护terminalPhone->ChannelId->Channel 一对一映射关系
 * @Date Created in 2020/1/11 17:24
 * @Modified By:
 */
@Slf4j
@Component
public class ChannelManager {

    private static final AttributeKey<String> MAC = AttributeKey.newInstance("mac");

    private ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Map<String, ChannelId> channelIdMap = new ConcurrentHashMap<>();

    private ChannelFutureListener remover = future -> {
        String phone = future.channel().attr(MAC).get();
        if (channelIdMap.get(phone) == future.channel().id()) {
            channelIdMap.remove(phone);
        }
    };

    public boolean add(String mac, Channel channel) {
        boolean added = channelGroup.add(channel);
        if (added) {
            //替换
            if (channelIdMap.containsKey(mac)) {
                Channel old = get(mac);
                old.closeFuture().removeListener(remover);
                old.close();
            }
            channel.attr(MAC).set(mac);
            channel.closeFuture().addListener(remover);
            channelIdMap.put(mac, channel.id());
        }
        return added;
    }

    public boolean remove(String mac) {
        return channelGroup.remove(channelIdMap.remove(mac));
    }

    public Channel get(String mac) {
        return channelGroup.find(channelIdMap.get(mac));
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }
}
