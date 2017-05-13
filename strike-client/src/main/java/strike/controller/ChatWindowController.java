package strike.controller;

import com.google.common.eventbus.Subscribe;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import strike.StrikeClient;
import strike.handler.ProtocolHandlerFactory;
import strike.model.Chatter;
import strike.model.event.*;
import strike.service.ClientState;
import strike.service.ConnectionService;
import strike.service.JSONMessageBuilder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ChatWindowController {

    // TODO find out a better way to do this dependency injection
    private StrikeClient strikeClient;

    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
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

    @FXML
    private Text idRoomnameLabel;

    @FXML
    private Text idUsernameLabel;

    private Set<Chatter> clientsInRoom = new HashSet<>();

    private JSONMessageBuilder messageBuilder = JSONMessageBuilder.getInstance();

    @FXML
    private void initialize() {

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

        ConnectionService.getInstance().getEventBus().register(this);
        ProtocolHandlerFactory.newSendHandler(messageBuilder.helpMessage()).handle();
    }

    @FXML
    public void didSendMessage(ActionEvent actionEvent) {
        String message = idMessageTextField.getText();

        // Add ours to the window.
        Text text = new Text(String.format("You: %s\n", message));
        idChatWindowContents.getChildren().add(text);

        // Send it to everyone.
        //this.strikeClient.getClient().SendMessage(message);

        //TODO need more efficient way to send chat message??
        ProtocolHandlerFactory.newSendHandler(messageBuilder.chatMessage(message)).handle();

        // Clean the input field.
        idMessageTextField.clear();
    }

    // Helper methods for updating the client list UI.

    private void appendSysMessage(String sysMessage) {
        appendSysMessage(sysMessage, "-fx-font-weight: bold");
    }

    private void appendSysMessage(String sysMessage, String style) {
        Text text = new Text(sysMessage);
        text.setStyle(style);
        idChatWindowContents.getChildren().add(text);
    }

    private void addClient(Chatter client) {
        clientsInRoom.add(client);
        updateClientListUI();
    }

    private void removeClient(Chatter client) {
        clientsInRoom.remove(client);
        updateClientListUI();
    }

    private void updateClients(Set<Chatter> clients) {
        clientsInRoom = clients;
        updateClientListUI();
    }

    private void updateClientListUI() {

        idClientsContents.getChildren().removeAll(idClientsContents.getChildren());

        Text titleText = new Text("USERS\n");
        titleText.setStyle("-fx-font-weight: bold");

        titleText.setFill(Color.web("#555555"));
        titleText.setFont(Font.font("Helvetica", FontWeight.BOLD, 14));

        idClientsContents.getChildren().add(titleText);

        for (Chatter client : clientsInRoom) {

            Text onlineSymbol = new Text("● ");
            onlineSymbol.setFill(Color.rgb(103, 207, 96));
            onlineSymbol.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 16));
            idClientsContents.getChildren().add(onlineSymbol);

            String clientStr = client.getId();
            if (client.isRoomOwner())
                clientStr = client.getId() + " ★";

            Text text = new Text(String.format("%s\n", clientStr));
            text.setFill(Color.web("#666666"));
            text.setFont(Font.font("Helvetica", FontWeight.SEMI_BOLD, 14));
            idClientsContents.getChildren().add(text);
        }
    }

    @Subscribe
    public void _messageReceive(MessageReceiveEvent event) {
        // When this client receives a message.
        Platform.runLater(() -> {
            Text text = new Text(String.format("%s: %s\n", event.getSender(), event.getMessage()));
            idChatWindowContents.getChildren().add(text);
            idScrollPane.setVvalue(1.0); // Move the scrollpane down to the bottom.
        });
    }

    @Subscribe
    public void _roomChange(RoomChangeEvent event) {
        Platform.runLater(() -> {
            String from = event.getFrom();
            String to = event.getTo();
            String message;

            if (from.equals("")) {
                message = String.format(String.format("You have joined the room '%s'.\n", to));
            } else {
                message = String.format("You have changed from room: '%s' to '%s'.\n", from, to);
            }

            // Add a message.
            Text text = new Text(message);
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the roomname label at the top of screen.
            idRoomnameLabel.setText(String.format("#%s", to.toUpperCase()));

            // Also update the username. (Not ideal, best place to put it for now.)
            idUsernameLabel.setText(String.format("@%s", ClientState.getInstance().getIdentity()));

            // Request a list of who is in the chat room.
            ProtocolHandlerFactory.newSendHandler(messageBuilder.whoMessage()).handle();
        });
    }

    @Subscribe
    public void _userJoined(UserJoinRoomEvent event) {
        // When a new user joins or leaves the room this client is in.
        Platform.runLater(() -> {
            // Update the main chat window.
            Text text = new Text(String.format("%s has joined the room!\n", event.getChatter().getId()));
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the clients list.
            addClient(event.getChatter());
        });
    }

    @Subscribe
    public void _userLeft(UserLeftRoomEvent event) {
        Platform.runLater(() -> {
            // Update the main chat window.
            Text text = new Text(String.format("%s has left the room!\n", event.getChatter().getId()));
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the clients list.
            removeClient(event.getChatter());
        });
    }

    @Subscribe
    public void _receiveRoomContents(RoomContentsEvent event) {
        Platform.runLater(() -> {
            updateClients(event.getClients());
        });
    }

    @Subscribe
    public void _receiveRoomList(RoomListEvent event) {
        Platform.runLater(() -> {
            ArrayList<String> roomList = new ArrayList<>();
            roomList.addAll(event.getRooms());
            roomList.sort(String::compareTo);
            String finalString = String.join(", ", roomList) + "\n";
            Text text = new Text(finalString);
            text.setFill(Color.GRAY);
            idChatWindowContents.getChildren().add(text);
        });
    }

    @Subscribe
    public void _deleteRoomCallback(RoomDeleteEvent event) {
        Platform.runLater(() -> {
           if (event.isApproved()) {
               appendSysMessage(String.format("%s is deleted.\n", event.getRoomId()));
           } else {
               String gangnamStyle = "-fx-font-weight: bold; -fx-text-fill: #006464;";
               appendSysMessage(String.format("Ask owner of %s to delete the room.\n", event.getRoomId()), gangnamStyle);
           }
        });
    }

    @Subscribe
    public void _createRoomCallback(RoomCreateEvent event) {
        Platform.runLater(() -> {
            if (event.isApproved()) {
                appendSysMessage(String.format("%s is created.\n", event.getRoomId()));
            } else {
                appendSysMessage(String.format("Room %s may already exist.\n", event.getRoomId()));
            }
        });
    }

    @Subscribe
    public void _ownerLeaveRoomCallback(OwnerLeaveRoomEvent event) {
        Platform.runLater(() -> {
            appendSysMessage(String.format("You are room owner of %s. Delete the room to leave.\n",
                    ClientState.getInstance().getRoomId()));
        });
    }

    @Subscribe
    public void _commandInvalidCallback(CommandInvalidEvent event) {
        Platform.runLater(() -> {
            appendSysMessage(String.format("%s\n", event.getMessage()));
        });
    }

    @Subscribe
    public void _userQuit(UserQuitEvent event) {
        Platform.runLater(() -> {
            // Update the main chat window.
            Text text = new Text(String.format("%s has quit the server!\n", event.getChatter().getId()));
            text.setStyle("-fx-font-weight: bold");
            idChatWindowContents.getChildren().add(text);

            // Update the clients list.
            removeClient(event.getChatter());

            // TODO just exit the app for now
            exitApplication(null);
        });
    }

    @Subscribe
    public void _socketClosed(SocketClosedEvent event) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connection Exception");
            alert.setHeaderText("Connection exception has occurred.\nYour session might has been time out." +
                    " Please login again.");
            alert.showAndWait();
            exitApplication(null);
        });
    }

    @FXML
    public void exitApplication(ActionEvent event) {
        // close all stages and call for shutdown the entire application
        logger.info("ChatWindow initiate exit application...");
        // Platform.exit();
        this.strikeClient.getPrimaryStage().close();
    }

    private static final Logger logger = LogManager.getLogger(ChatWindowController.class);
}
