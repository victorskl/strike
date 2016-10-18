package strike.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.layout.Pane;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.StrikeClient;
//import strike.model.Protocol;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Login {

    private StrikeClient strikeClient;

    private Configuration systemProperties;
    private SSLSocketFactory sslsocketfactory;
    private List<String> messages;
    private JSONParser parser;
    private SSLSocket socket = null;

    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
    }

    @FXML
    private TextField idUsername;

    @FXML
    private PasswordField idPassword;

    @FXML
    private ToggleGroup idAccountType;

    @FXML
    private void initialize() {
        /**
         * Initializes the controller class. This method is automatically called
         * after the fxml file has been loaded.
         */
        logger.info("Login Controller Init...");

        try {
            File systemPropertiesFile = new File("./config/system.properties");
            Configurations configs = new Configurations();
            systemProperties = configs.properties(systemPropertiesFile);
            parser = new JSONParser();
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }

        messages = new ArrayList<>();

        // MUST BE BEFORE SSLSocketFactory!!!
        System.setProperty("javax.net.ssl.keyStore", systemProperties.getString("keystore"));
        System.setProperty("javax.net.ssl.keyStorePassword", "strikepass");
        System.setProperty("javax.net.ssl.trustStore", systemProperties.getString("keystore"));
        //System.setProperty("javax.net.debug","all");

        sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

    }

    @FXML
    public void login(ActionEvent actionEvent) {

        String username = idUsername.getText();
        System.out.println(username);

        String password =  idPassword.getText();
        System.out.println(password);

        String accType = ((RadioButton) idAccountType.getSelectedToggle()).getText();
        System.out.println(accType);

        /*
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(strikeClient.getPrimaryStage());
        alert.setTitle("Login");
        alert.setHeaderText("Your Login Info");
        alert.setContentText("Username: " + username + ", Password: " + password + ", Account Type: " + accType);
        alert.showAndWait();
        */



        // try to perform authorisation:
        // Need to update the port to the server port chosen from the server list.
        createAuthMessage(username, password, false);
        JSONObject response = performAuth(4444);
        System.out.println(response.toJSONString());


        if(response.get("success").equals("true")) {
            // Show the screen name prompt.
            try {

                // Get the screenname.
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(this.strikeClient.getClass().getResource("view/ScreenName.fxml"));
                Pane screenNameWindow = loader.load();

                // Show the scene containing the root layout.
                Scene scene = new Scene(screenNameWindow);
                strikeClient.getPrimaryStage().setScene(scene);
                strikeClient.getPrimaryStage().show();

                // Pass the client to the screen name widnow.
                ScreenNameController controller = loader.getController();
                controller.setStrikeClient(this.strikeClient);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else { // Show an alert about couldn't authenticate.
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Unable to authenticate!");
            alert.setHeaderText("Unable to authenticate your credentials. Please try again.");
            alert.showAndWait();
        }

    }


    // Returns the response from the authorisation.
    public JSONObject performAuth(int port) {
        JSONObject response = null;
        System.out.println("Im here.");

        try {
            socket = (SSLSocket) sslsocketfactory.createSocket("localhost", port);

            for (String s : socket.getSupportedProtocols()) {
                System.out.println(s);
            }
            System.out.println();

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            for (String message : messages) {
                writer.write(message + "\n");
                writer.flush();
                System.out.println("Sending : " + message);
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            for (int i = 0; i< messages.size(); i++) {
                String resp = reader.readLine();

                try {
                    JSONObject jj = (JSONObject) parser.parse(resp);
                    response = jj;
                    System.out.println("Response : " + jj.toJSONString());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            writer.close();
            reader.close();
            socket.close();

        } catch (IOException ioe) {
            System.out.println(ioe.getCause());
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return response;
    }

    private void createAuthMessage(String username, String password, boolean remember) {

        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}

        /*
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), username); // define in shiro.ini
        jj.put(Protocol.password.toString(), password); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), remember); // true or false or not provide
        messages.add(jj.toJSONString());
        */

        JSONObject jj = new JSONObject();
        jj.put("type", "authenticate");
        jj.put("username", username); // define in shiro.ini
        jj.put("password", password); // define in shiro.ini
        jj.put("rememberme", String.format("%s", remember)); // true or false or not provide
        messages.add(jj.toJSONString());
    }


    private static final Logger logger = LogManager.getLogger(Login.class);
}
