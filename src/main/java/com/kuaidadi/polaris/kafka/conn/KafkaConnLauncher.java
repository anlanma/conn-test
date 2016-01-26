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

    private static Properties prop;

    public static void main(String[] args) {
        KafkaConnLauncher launcher = new KafkaConnLauncher();
        launcher.init();
        launcher.start();
    }
    public void init(){
        InputStream in = null;
        try {
            in = KafkaConnLauncher.class.getClassLoader().getResourceAsStream(confName);
            Properties p = new Properties();
            p.load(in);
            this.prop = p;
        } catch (FileNotFoundException e) {
            logger.error(e);
        } catch (IOException e) {
            logger.error(e);
        }

    }
    public void start(){
        Timer timer = new Timer();
        String[] addrs = prop.getProperty(ADDRS).split(";");
        Map<String,Integer> map = getValidAddrs(addrs);
        if(map == null){
            logger.error("Invalid addrs");
            return;
        }
        timer.schedule(new MyTask(map),1000,1000);
    }
    public Map<String,Integer> getValidAddrs(String[] addrs){
        if(addrs == null || addrs.length == 0){
            return null;
        }
        Map<String,Integer> map = new HashMap<String, Integer>();
        //È¥³ý¿Õ´®
        int i = 0;
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
            if(ipValid(ip) && portValid(port)){
                map.put(ip,Integer.valueOf(port));
            }
        }
        return map;
    }
    public boolean ipValid(String ip){
        return true;
    }
    public boolean portValid(String port){
        return true;
    }


    class MyTask extends TimerTask{
        //key:IP,value:port
        private Map<String,Integer> map;

        public MyTask(Map<String,Integer> map){
            this.map = map;
        }

        @Override
        public void run() {
            Iterator it = map.keySet().iterator();
            while (it.hasNext()){
                String host = (String) it.next();
                Integer port = map.get(host);
                Socket socket = null;
                Long befor = System.currentTimeMillis();
                try {
                    socket = new Socket(host,port);
                    logger.info("Connect "+host+":"+port+" successfully,time used:"+ (System.currentTimeMillis() - befor) + " ms.");
                } catch (IOException e) {
                    logger.error("Connect "+host+":"+port+" fail,time used:"+ (System.currentTimeMillis() - befor)  + " ms.",e);
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
