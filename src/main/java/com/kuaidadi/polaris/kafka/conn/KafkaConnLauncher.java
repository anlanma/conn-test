package com.kuaidadi.polaris.kafka.conn;

import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.*;

/**
 * Created by tanglin on 2016/1/18.
 */
public class KafkaConnLauncher {
    private static Logger logger = Logger.getLogger("launcher");

    private static String confName = "conf.properties";

    private static final String ADDRS = "addr.list";

    private static volatile Map ipAndPort;


    public static void main(String[] args) {
        init();
        start();
    }

    /**
     * 加载配置文件获
     */
    private static void init(){
        Properties p = null;
        try {
            p = ConfReader.readConf(confName);
        } catch (IOException e) {
            logger.error("Load config fail:"+confName,e);
            System.exit(-1);
        }

        if(!updateAddr(p)){
            logger.error("No valid addrs,properties:"+p);
            System.exit(-1);
        }

    }
    public static boolean updateAddr(Properties p){
        logger.info("Update conf success:"+p);
        String[] addrs = p.getProperty(ADDRS).split(";");
        Map<String,Integer> tmp = getValidAddrs(addrs);
        if(tmp == null || tmp.isEmpty()){
            return false;
        }
        ipAndPort = tmp;
        return true;
    }

    /**
     * 获取ip、port list，启动定时任务
     */
    public static void start(){
        Timer timer = new Timer();
        timer.schedule(new ConnectTask(ipAndPort),1000,1000);
        ConfigUpdateMonitor.start(confName);
    }


    private static Map<String,Integer> getValidAddrs(String[] addrs){
        if(addrs == null || addrs.length == 0){
            return null;
        }
        Map<String,Integer> map = new HashMap<String, Integer>();
        //去除空串
        for(String addr : addrs){
            if(logger.isDebugEnabled()){
                logger.debug("Addr:"+addr);
            }
            String[] parts = addr.split(":");
            if(parts.length != 2){
                logger.error("Invalid ip and port:"+addr);
            }
            String ip = parts[0];
            String port = parts[1];
            if(isIpValid(ip) && isPortValid(port)){
                map.put(ip,Integer.valueOf(port));
            }
        }
        return map;
    }
    private static boolean isIpValid(String ip){
        return true;
    }
    private static boolean isPortValid(String port){
        return true;
    }


    static class ConnectTask extends TimerTask{
        //key:IP,value:port
        private Map<String,Integer> map;

        public ConnectTask(Map<String, Integer> map){
            this.map = map;
        }

        @Override
        public void run() {
            Iterator it = map.keySet().iterator();
            while (it.hasNext()){
                String host = (String) it.next();
                Integer port = map.get(host);
                Socket socket = null;
                Long before = System.currentTimeMillis();
                try {
                    socket = new Socket(host,port);
                    logger.info("Connect "+host+":"+port+" successfully,time used:"+ (System.currentTimeMillis() - before) + " ms.");
                } catch (IOException e) {
                    logger.error("Connect "+host+":"+port+" fail,time used:"+ (System.currentTimeMillis() - before)  + " ms.",e);
                }finally {
                    if(socket != null){
                        try {
                            socket.close();
                        } catch (IOException e) {
                            logger.error("Close socket error.",e);
                        }
                    }
                }
            }

        }
    }
}
