package strike;

import au.edu.unimelb.tcp.client.Client;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import strike.controller.Login;

import java.io.IOException;

public class StrikeClient extends Application {

    private Stage primaryStage;
    private BorderPane rootLayout;
    private Client client;

    @Override
    public void start(Stage primaryStage) throws Exception {

        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Strike Chat Client");

        initRootLayout();

        boolean userNotLoggedIn = false; // TODO dummy login state
        if (!userNotLoggedIn) {
            showLogin();
        }

        String[] args = new String[6];
        args[0] = "-p";
        args[1] = "4444";
        args[2] = "-i";
        args[3] = "user";
        args[4] = "-h";
        args[5] = "localhost";

        //client = new Client(getParameters().getRaw().toArray(new String[0]));
        client = new Client(args);
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