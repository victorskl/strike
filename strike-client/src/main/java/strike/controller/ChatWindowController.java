package strike.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import strike.StrikeClient;

public class ChatWindowController {

    private StrikeClient strikeClient;

    public void setStrikeClient(StrikeClient strikeClient) {
        this.strikeClient = strikeClient;
    }

    @FXML
    private TextArea idChatWindowContents;

    @FXML
    private TextField idMessageTextField;

    @FXML
    private void initialize() {
        /**
         * Initializes the controller class. This method is automatically called
         * after the fxml file has been loaded.
         */
        System.out.println("ChatWindow Controller Init...");
        idChatWindowContents.appendText("\n");
    }

    @FXML
    public void didSendMessage(ActionEvent actionEvent) {
        String message = idMessageTextField.getText();
        idChatWindowContents.appendText(String.format("%s\n", message));

        idMessageTextField.clear();
    }
}
