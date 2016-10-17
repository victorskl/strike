package strike;

import au.edu.unimelb.tcp.client.Client;
import au.edu.unimelb.tcp.client.ComLineValues;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kohsuke.args4j.CmdLineParser;
import strike.controller.Login;

import java.io.IOException;

public class StrikeClient extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Client client;

    private Configuration systemProperties;

    private static final Logger logger = LogManager.getLogger(StrikeClient.class);

    @Override
    public void start(Stage primaryStage) throws Exception {

        String[] args = new String[6];
        args[0] = "-p";
        args[1] = "4444";
        args[2] = "-i";
        args[3] = "user";
        args[4] = "-h";
        args[5] = "localhost";

        // String[] args = getParameters().getRaw().toArray(new String[0]);

        ComLineValues values = new ComLineValues();
        CmdLineParser cmdLineParser = new CmdLineParser(values);

        logger.info("Parsing args...");
        cmdLineParser.parseArgument(args);

        logger.info("option: -c " + values.getSystemPropertiesFile().toString());
        logger.info("Reading system properties file: " + values.getSystemPropertiesFile().toString());
        try {
            Configurations configs = new Configurations();
            systemProperties = configs.properties(values.getSystemPropertiesFile());
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        logger.info("Setting up SSL system environment...");
        System.setProperty("javax.net.ssl.trustStore", systemProperties.getString("keystore"));
        //System.setProperty("javax.net.debug","all"); // uncomment to debug SSL, and comment it back there after

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Strike Chat Client");

        initRootLayout();

        boolean userNotLoggedIn = false; // TODO dummy login state
        if (!userNotLoggedIn) {
            showLogin();
        }

        client = new Client(values);
    }

    private void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/RootLayout.fxml"));
            rootLayout = loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("view/Login.fxml"));
            Pane loginPane = loader.load();

            // Set person overview into the center of root layout.
            rootLayout.setCenter(loginPane);

            // Give the controller access to the main app.
            Login controller = loader.getController();
            controller.setStrikeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public Client getClient() {return client;}

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if(client.isRunning()) {
            client.SendMessage("#quit");
        }
    }
}