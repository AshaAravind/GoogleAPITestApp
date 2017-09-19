package com.gtest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GTestUtil {

	private static final String PATH_CONFIG_PROPERTIES = "src/main/resources/config.properties";
	private static final Logger LOG = LoggerFactory.getLogger(TokenTestServlet.class);

	public static String getConfigValue(String propertyName) {
		String value = "";
		try (InputStream stream = new FileInputStream(new File(PATH_CONFIG_PROPERTIES))){
			Properties properties = new Properties();
			properties.load(stream);
			value = properties.getProperty(propertyName);
		} catch (FileNotFoundException e) {
			LOG.error("Config file not found", e);
		} catch (IOException e) {
			LOG.error("Error while fetching the values from the config file", e);
		} 
		return value;
	}

}
