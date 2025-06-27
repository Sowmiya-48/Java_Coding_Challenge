package com.hexaware.oms.util;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class DBPropertyUtil {

    public static Properties loadProperties(String fileName) throws IOException {
        Properties props = new Properties();
        FileReader reader = new FileReader(fileName);
        props.load(reader);
        return props;
    }
}
