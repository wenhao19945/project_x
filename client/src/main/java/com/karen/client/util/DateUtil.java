package com.karen.client.util;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * DateUtil
 * @Author wenhao
 * @Date 2020/2/4 17:40
 * @Description //TODO
 */
public class DateUtil {

  public static final String DEF_FMT = "yyyy-MM-dd HH:mm:ss";

  public static final String YMD = "yyyyMMddHHmmss";

  public static String getDateStr(){
    SimpleDateFormat sdf = new SimpleDateFormat(YMD);
    return sdf.format(new Date());
  }

  public static String DateToStr(Date date, String fmt){
    SimpleDateFormat sdf = new SimpleDateFormat(fmt);
    return sdf.format(date);
  }

  public static void main(String[] args) throws Exception{

   SimpleDateFormat formatter = new SimpleDateFormat(DEF_FMT);

    final Timer timer = new HashedWheelTimer(1, TimeUnit.SECONDS, 2);
    System.out.println(formatter.format(new Date()));
    TimerTask task1 = new TimerTask() {
      @Override
      public void run(Timeout timeout) throws Exception {
        System.out.println("task 1 will run per 5 seconds "+formatter.format(new Date()));
        timer.newTimeout(this, 5, TimeUnit.SECONDS);//结束时候再次注册
      }
    };
    timer.newTimeout(task1, 5, TimeUnit.SECONDS);


    TimerTask task2 = new TimerTask() {
      @Override
      public void run(Timeout timeout) throws Exception {
        System.out.println("task 2 will run per 10 seconds"+formatter.format(new Date()));
        timer.newTimeout(this, 10, TimeUnit.SECONDS);//结束时候再注册
      }
    };
    timer.newTimeout(task2, 10, TimeUnit.SECONDS);


    //该任务仅仅运行一次
    timer.newTimeout(new TimerTask() {
      @Override
      public void run(Timeout timeout) throws Exception {
        System.out.println("task 3 run only once ! "+formatter.format(new Date()));
      }
    }, 15, TimeUnit.SECONDS);

  }

}