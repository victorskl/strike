package strike.controller;

import au.edu.unimelb.tcp.client.Client;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import strike.StrikeClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ChatWindowController implements Client.IMessageReceiveHandler, Client.IRoomChangeHandler, Client.IClientListUpdateHandler {

    private StrikeClient strikeClient;

    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;

        // Update the text when we get a new message from a client.
        strikeClient.getClient().onMessageReceive("message", this);

        // Update the GUI when we have changed rooms.
        strikeClient.getClient().onRoomChange("change", this);

        strikeClient.getClient().onClientListUpdate("joinleave", this);
    }

    @FXML
    // Main input field.
    private TextField idMessageTextField;

    // Main chat window
    @FXML
    private ScrollPane idScrollPane;
    @FXML
    private TextFlow idChatWindowContents;

    // Chatroom clients window
    @FXML
    private ScrollPane idClientsScrollPane;
    @FXML
    private TextFlow idClientsContents;

    private Set<String> clientsInRoom = new HashSet<>();

    @FXML
    private void initialize() {
        /**
         * Initializes the controller class. This method is automatically called
         * after the fxml file has been loaded.
         */
        logger.info("ChatWindow Controller Init...");

        idChatWindowContents.heightProperty().addListener((observable, oldVal, newVal) -> {
            idScrollPane.setVvalue(newVal.doubleValue());
        });

        idChatWindowContents.setPadding(new Insets(10, 10, 10, 10));
        idClientsContents.setPadding(new Insets(15, 15, 15, 15));

        /*
        idClientsContents.heightProperty().addListener((observable, oldVal, newVal) -> {
            idClientsScrollPane.setVvalue(newVal.doubleValue());
        });
        */
    }

    @FXML
    public void didSendMessage(ActionEvent actionEvent) {
        String message = idMessageTextField.getText();

        // Add ours to the window.
        Text text = new Text(String.format("You: %s\n", message));
        idChatWindowContents.getChildren().add(text);

        // Send it to everyone.
        this.strikeClient.getClient().SendMessage(message);

        // Clean the input field.
        idMessageTextField.clear();
    }

    // Helper methods for updating the client list UI.

    public void addClient(String client) {
        clientsInRoom.add(client);
        setClients(clientsInRoom);
    }

    public void removeClient(String client) {
        clientsInRoom.remove(client);

        setClients(clientsInRoom);
    }

    public void setClients(Set<String> clients) {

        // Sort the clients alphabetically.
        clientsInRoom = clients;
        ArrayList<String> clientsList = new ArrayList<>();
        clientsList.addAll(clients);
        Collections.sort(clientsList, String.CASE_INSENSITIVE_ORDER);

        updateClientListUI(clientsList);
    }

    public void updateClientListUI(ArrayList<String> clients) {

        idClientsContents.getChildren().removeAll(idClientsContents.getChildren());

        Text titleText = new Text("USERS\n");
        titleText.setStyle("-fx-font-weight: bold");

        titleText.setFill(Color.web("#555555"));
        titleText.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));

        idClientsContents.getChildren().add(titleText);

        for(String client : clients) {

            Text onlineSymbol = new Text("● ");
            onlineSymbol.setFill(Color.rgb(103,207,96));
            onlineSymbol.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 16));
            idClientsContents.getChildren().add(onlineSymbol);

            Text text = new Text(String.format("%s\n", client));
            text.setFill(Color.web("#666666"));
            text.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 14));
            idClientsContents.getChildren().add(text);
        }
    }


    // Handle events
    @Override
    // When this client receives a message.
    public void messageReceive(String sender, String message) {
        // Need to call runLater, as it will schedule this on the main JavaFX Application Thread
        // Can only update UI elements from the main thread.
        Platform.runLater(() -> {
            Text text = new Text(String.format("%s: %s\n", sender, message));
            idChatWindowContents.getChildren().add(text);
            idScrollPane.setVvalue(1.0); // Move the scrollpane down to the bottom.
        });
    }

    @Override
    // When this client changes rooms.
    public void roomChange(String from, String to) {
        // Need to call runLater, as it will schedule this on the main JavaFX Application Thread
        // Can only update UI elements from the main thread.
        Platform.runLater(() -> {

            String message;

            if(from.equals("")) {
               message = String.format(String.format("You have joined the room '%s'.\n", to));
            }
            else {
                message = String.format("You have changed from room: '%s' to '%s'.\n", from, to);
            }

            // Add a message.
            Text text = new Text(message);
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Request a list of who is in the chat room.
            this.strikeClient.getClient().requestClientListUpdate();
        });
    }


    // When a new user joins or leaves the room this client is in.
    @Override
    public void userJoined(String userid) {
        Platform.runLater(() -> {
            // Update the main chat window.
            Text text = new Text(String.format("%s has joined the room!\n", userid));
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the clients list.
            addClient(userid);
        });
    }

    @Override
    public void userLeft(String userid) {
        Platform.runLater(() -> {
            // Update the main chat window.
            Text text = new Text(String.format("%s has left the room!\n", userid));
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the clients list.
            removeClient(userid);
        });
    }

    @Override
    public void receiveInitialClientList(Set<String> clients) {
        Platform.runLater(() -> {
            setClients(clients);
        });
    }

    private static final Logger logger = LogManager.getLogger(ChatWindowController.class);
}
