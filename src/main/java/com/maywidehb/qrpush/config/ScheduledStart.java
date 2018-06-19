package com.maywidehb.qrpush.config;

import org.springframework.stereotype.Component;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * web启动类,有需要启动运行的类在此添加
 * @author DaY
 *
 */
@Component
public class ScheduledStart implements ServletContextListener  {

	final static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();  
	/**
	 * 00:00:00保证日志每天产生变化,备份
	 */
    public static void TimerLogging() {
        Calendar calendar = Calendar. getInstance();
        calendar.setTime( new Date());
        calendar.set(Calendar. HOUR_OF_DAY, 0);
        calendar.set(Calendar. MINUTE, 1);
        calendar.set(Calendar. SECOND, 1);
        calendar.set(Calendar. MILLISECOND, 0);
        calendar.add(Calendar. DAY_OF_MONTH, 1);
        Date date = calendar.getTime();
        long initialDelay = date.getTime()-System.currentTimeMillis();
        long delay = 24*60*60*1000;
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System. out.println("时间定时器运行时间:"+new Date());
                try{
                    Logs.QRPR.info(" ");
                    Logs.QRCODE.info(" ");
                    Logs.QRREQUEST.info(" ");
                    System. out.println("时间定时器运行成功,时间:"+new Date());
                }catch (Exception e) {
                	e.printStackTrace();
				}
            }
        }, initialDelay, delay, TimeUnit.MILLISECONDS);
        System. out.println("时间定时器开始时间:"+date);
        System. out.println("时间定时器下次运行时间:"+new Date(delay+initialDelay+System.currentTimeMillis()));
    }
    
    public static void main(String[] args) {
    	TimerLogging();
    }

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
        System. out.println("时间定时线程销毁");
        scheduledExecutorService.shutdownNow();
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		TimerLogging();
	}
    
}