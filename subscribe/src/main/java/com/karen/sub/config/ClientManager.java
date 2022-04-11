package com.karen.sub.config;

import com.karen.sub.client.NettyClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * @author WenHao
 * @ClassName ClientManager
 * @date 2022/2/18 17:31
 * @Description
 */
@Component
public class ClientManager {

  public Map<String, NettyClient> map = new ConcurrentHashMap<>(16);

}
