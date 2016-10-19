package strike.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import javafx.scene.layout.Pane;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import strike.StrikeClient;
import strike.common.model.Protocol;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.util.*;

public class Login {

    private class ServerInfo {
        public String address;
        public Integer port;
        public String serverID;

        public ServerInfo(String address, Integer port, String serverID) {
            this.address = address;
            this.port = port;
            this.serverID = serverID;
        }
    }

    private StrikeClient strikeClient;

    private Configuration systemProperties;
    private SSLSocketFactory sslsocketfactory;
    private List<String> messages;
    private JSONParser parser;
    private SSLSocket socket = null;

    HashMap<String, ServerInfo> serverInformationMap = new HashMap<>();

    ObservableList<String> listOfServers = FXCollections.observableArrayList();

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
    private ComboBox<String> idServer;

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

        // Try to create the SSL socket we are going to use for all communication.
        sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();

        try {
            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // This server needs to be running or else we cannot get the server list!
            socket = (SSLSocket) sslsocketfactory.createSocket("115.146.90.37", 4440);

            // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            // To run locally, use this socket instead!
            // socket = (SSLSocket) sslsocketfactory.createSocket("localhost", 4444);

            socket.startHandshake();
        }
        catch(IOException e) {
            logger.log(Level.FATAL, "Unable to create SSL socket.");
            System.exit(0);
        }


        JSONObject message = createServerListMessage();
        JSONObject response = perform(socket, message); // (serverlist, not auth).
        populateToggleGroup(response);

        try {
            socket.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void populateToggleGroup(JSONObject serverList) {

        JSONArray servers = (JSONArray)serverList.get("servers");

        Iterator i = servers.iterator();

        while(i.hasNext()) {
            JSONObject server = (JSONObject)i.next();

            String serverID = (String)server.get("serverid");
            Number port = (Number)server.get("port");
            String address = (String)server.get("address");

            serverInformationMap.put(serverID, new ServerInfo(address, port.intValue(), serverID));
        }

        ArrayList<String> sorted = new ArrayList<>();
        listOfServers.addAll(serverInformationMap.keySet());

        Collections.sort(listOfServers, (String o1, String o2) -> {
                return o1.compareTo(o2);
        });

        idServer.setItems(listOfServers);
    }

    @FXML
    public void login(ActionEvent actionEvent) {

        String username = idUsername.getText();
        System.out.println(username);

        String password =  idPassword.getText();
        System.out.println(password);

        String accType = ((RadioButton) idAccountType.getSelectedToggle()).getText();
        System.out.println(accType);

        String selectedServerID = idServer.getSelectionModel().getSelectedItem();
        ServerInfo info = serverInformationMap.get(selectedServerID);
        System.err.println(info.toString());

        try {
            socket = (SSLSocket) sslsocketfactory.createSocket(info.address, info.port);
            socket.startHandshake();
        }
        catch(IOException e) {
            logger.log(Level.FATAL, "Unable to create SSL socket for authentication.");
            System.exit(0);
        }

        // try to perform authorisation:
        // Need to update the port to the server port chosen from the server list.
        JSONObject authMessage = createAuthMessage(username, password, false);
        JSONObject response = perform(socket, authMessage);
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

                // Pass the client to the screen name window.
                ScreenNameController controller = loader.getController();
                controller.setStrikeClient(this.strikeClient);
                controller.setAuthenticatedSocket(this.socket);

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
    public JSONObject perform(SSLSocket socket, JSONObject authMessage) {
        JSONObject response = null;

        try {
            for (String s : socket.getSupportedProtocols()) {
                System.out.println(s);
            }
            System.out.println();

            // Write the message out.
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));

            writer.write(authMessage+ "\n");
            writer.flush();
            System.out.println("Sending : " + authMessage);

            // Wait for the response.
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

            String resp = reader.readLine();

            try {
                JSONObject jj = (JSONObject) parser.parse(resp);
                response = jj;
                System.out.println("Response : " + jj.toJSONString());
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //writer.close();
            //reader.close();

        } catch (IOException ioe) {
            System.err.println("IO Exception somewhere.");
            System.err.println(ioe.getMessage());
            System.out.println(ioe.getCause());
        }

        return response;
    }

    private JSONObject createAuthMessage(String username, String password, boolean remember) {
        // {"type" : "authenticate", "username" : "ray@example.com", "password":"cheese", "rememberme":"true"}
        JSONObject jj = new JSONObject();
        jj.put(Protocol.type.toString(), Protocol.authenticate.toString());
        jj.put(Protocol.username.toString(), username); // define in shiro.ini
        jj.put(Protocol.password.toString(), password); // define in shiro.ini
        jj.put(Protocol.rememberme.toString(), String.format("%s", remember)); // true or false or not provide

        return jj;
    }

    private JSONObject createServerListMessage() {
        JSONObject request = new JSONObject();
        request.put(Protocol.type.toString(), Protocol.listserver.toString());
        return request;
    }

    private static final Logger logger = LogManager.getLogger(Login.class);
}
