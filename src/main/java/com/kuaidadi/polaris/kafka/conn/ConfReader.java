package com.kuaidadi.polaris.kafka.conn;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by tanglin on 16/2/12.
 */
public class ConfReader {
    private static Logger logger = Logger.getLogger(ConfReader.class);


    public static Properties readConf(String confName) throws IOException {
        InputStream in = ConfReader.class.getClassLoader().getResourceAsStream(confName);
        Properties p = new Properties();
        p.load(in);
        return p;
    }

    public static String getAbsPath(String confName){
        return ConfReader.class.getClassLoader().getResource(confName).getPath();
    }

}
