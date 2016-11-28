package sample;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by TinTin on 22-Nov-16.
 *
 * @Author TranTrongTin
 */
public class LoginController implements Initializable {
    @FXML
    private TextField usernameTxtField;

    @FXML
    private PasswordField passwordTxtField;

    @FXML
    private Button loginBtn;

    @FXML
    private Label connectionStatus;

    private Client client;
    private StringProperty username = new SimpleStringProperty(),
            password = new SimpleStringProperty();

    @FXML
    private void login(ActionEvent event) throws IOException {
        String response = client.loginValidate(username.get(), password.get());

        //successful validation authentication
        if (client.validResponse(response)) {
            client.setUsername(username.get());
            //open chat GUI
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
            Parent simulation_parent = loader.load();
            MainViewController controller = loader.getController();
            controller.setClient(client);
            controller.init();

            //create scene for mainView chat and add CSS style
            Scene main_scene = new Scene(simulation_parent);

            //set stage properties
            Stage main_stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            main_stage.setScene(main_scene);
            main_stage.show();
        }
        else {
            connectionStatus.setTextFill(Color.RED);
            connectionStatus.setText(response.substring(3));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        username.bindBidirectional(usernameTxtField.textProperty());
        password.bindBidirectional(passwordTxtField.textProperty());
        try {
            client = new Client();
            String message = client.getServerMessage();
            if (client.validResponse(message)){
                connectionStatus.setTextFill(Color.GREEN);
                connectionStatus.setText("Successfully connected to the server");
            }
            else{
                connectionStatus.setTextFill(Color.RED);
                connectionStatus.setText("Failed to connect");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
