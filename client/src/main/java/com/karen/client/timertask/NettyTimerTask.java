package com.karen.client.timertask;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import java.util.concurrent.*;

/**
 * @ClassName: NettyTimerTask 初始化一个全局定时任务调度器
 * @Author: wenhao
 * @Description:
 * @Date Created in 2020/2/5 11:31
 * @Modified By:
 */
@Slf4j
@Component
public class NettyTimerTask {

    public static void main(String[] args) {
        ThreadFactory springThreadFactory = new CustomizableThreadFactory("springThread-pool-");

        ExecutorService exec = new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10),springThreadFactory);
        exec.submit(() -> {
            log.info("--111111---");
        });
    }

    /**
     * 1.ScheduledExecutorService 是面向任务的，当任务数非常大时，使用堆(PriorityQueue)维护任务的新增、删除会造成性能的下降，
     * 而 HashedWheelTimer 是面向 bucket 的，设置合理的 ticksPerWheel，tickDuration ，可以不受任务量的限制。所以在任务量非常大时， HashedWheelTimer 可以表现出它的优势。
     * 2.相反，如果任务量少， HashedWheelTimer 内部的 Worker 线程依旧会不停的拨动指针，虽然不是特别消耗性能，但至少不能说： HashedWheelTimer 一定比 ScheduledExecutorService 优秀。
     * 3.HashedWheelTimer 由于开辟了一个 bucket 数组，占用的内存也会稍大。
     * 4.在任务量非常大时，使用 HashedWheelTimer 可以获得性能的提升。例如服务治理框架中的心跳定时任务，当服务实例非常多时，
     * 每一个客户端都需要定时发送心跳，每一个服务端都需要定时检测连接状态，这是一个非常适合使用 HashedWheelTimer 的场景。
     */

    /**
     * ticksPerWheel，tickDuration 这两个参数尤为重要，ticksPerWheel 控制了时间轮中 bucket 的数量，决定了冲突发生的概率，
     * tickDuration 决定了指针拨动的频率，一方面会影响定时的精度，一方面决定 CPU 的消耗量。当任务数量非常大时，考虑增大
     * ticksPerWheel；当时间精度要求不高时，可以适当加大 tickDuration
     * 有16个格子的轮子，每一秒走一个一个格子，到了延迟的时间就会触发格子里的定时任务
     */
    public static final Timer TIMER = new HashedWheelTimer(new CustomizableThreadFactory("HashedWheel-p-"),1, TimeUnit.SECONDS, 16);

    public Timer getTimer(){
        return TIMER;
    }

    public static final ScheduledExecutorService SCHEDULED = new ScheduledThreadPoolExecutor(5, new CustomizableThreadFactory("Scheduled-p-"));

}
