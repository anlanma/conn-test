package com.kuaidadi.polaris.kafka.conn;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by tanglin on 16/2/9.
 */
public class ConfigUpdateMonitor {
    private static Logger logger = Logger.getLogger(ConfigUpdateMonitor.class);

    private static long lastUpdTime;

    private static String path;

    public static void start(String p){
        path = p;
        Timer timer = new Timer();
        timer.schedule(new MonitorTask(),1000,1000);
    }

    private static void reload(){
        Properties p = null;
        try {
            p = ConfReader.readConf(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        KafkaConnLauncher.updateAddr(p);
    }


    static class MonitorTask extends TimerTask{

        @Override
        public void run() {
            try {
                logger.info("monitor task");
                String absolutePath = ConfReader.getAbsPath(path);
                if (StringUtils.isNotEmpty(path)) {
                    File file = new File(absolutePath);
                    if (!file.exists()) {
                        return;
                    }
                    long updTime = file.lastModified();
                    if (lastUpdTime < updTime) {
                        lastUpdTime = updTime;
                        reload();
                    }
                }
            }catch (Exception e){
                logger.error(e.getMessage(),e);
            }

        }
    }
}
