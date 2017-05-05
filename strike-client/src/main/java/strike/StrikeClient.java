package strike;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import strike.controller.Login;
import strike.service.ConnectionService;

import java.io.IOException;

public class StrikeClient extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Strike Chat Client");
        logger.info("Starting client...");
        updateLogger();
        initRootLayout();

        boolean userNotLoggedIn = false; // TODO dummy login state
        if (!userNotLoggedIn) {
            showLogin();
        }
    }

    private void updateLogger() {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        org.apache.logging.log4j.core.config.Configuration config = ctx.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig("strike");

        logger.debug(String.format("Client is running in %s mode", loggerConfig.getLevel().toString()));
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

    @Override
    public void stop() {
        try {
            logger.info("Initiating shutdown sequence...");
            ConnectionService.getInstance().stop();
            super.stop();
            logger.info("Bye!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private static final Logger logger = LogManager.getLogger(StrikeClient.class);
}