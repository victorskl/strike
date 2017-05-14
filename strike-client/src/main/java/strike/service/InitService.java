package strike.service;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class InitService {
    private static InitService instance = null;

    public static synchronized InitService getInstance() {
        if (instance == null) instance = new InitService();
        return instance;
    }

    private Configuration systemProperties;

    private InitService() {
        File systemPropertiesFile = new File("./config/client.properties");
        logger.info("Reading system properties file: " + systemPropertiesFile.getName());
        try {
            Configurations configs = new Configurations();
            systemProperties = configs.properties(systemPropertiesFile);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        logger.info("Setting up SSL system environment...");
        System.setProperty("javax.net.ssl.trustStore", systemProperties.getString("keystore"));
        //System.setProperty("javax.net.debug","all"); // uncomment to debug SSL, and comment it back there after

        // MUST BE BEFORE SSLSocketFactory!!!
        System.setProperty("javax.net.ssl.keyStore", systemProperties.getString("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", "strikepass");
        System.setProperty("javax.net.ssl.trustStore", systemProperties.getString("keystore"));
        //System.setProperty("javax.net.debug","all");
    }

    public Configuration getSystemProperties() {
        return this.systemProperties;
    }

    private static final Logger logger = LogManager.getLogger(InitService.class);
}
