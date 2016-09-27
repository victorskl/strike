package strike.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import strike.StrikeClient;

public class Login {

    private StrikeClient strikeClient;

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
        System.out.println("Login Controller Init...");
    }

    @FXML
    public void login(ActionEvent actionEvent) {
        String username = idUsername.getText();
        System.out.println(username);

        String password =  idPassword.getText();
        System.out.println(password);

        String accType = ((RadioButton) idAccountType.getSelectedToggle()).getText();
        System.out.println(accType);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(strikeClient.getPrimaryStage());
        alert.setTitle("Login");
        alert.setHeaderText("Your Login Info");
        alert.setContentText("Username: " + username + ", Password: " + password + ", Account Type: " + accType);
        alert.showAndWait();
    }
}
