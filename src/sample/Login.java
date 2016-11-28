package sample;/**
 * Created by TinTin on 22-Nov-16.
 *
 * @Author TranTrongTin
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Login extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
    // Load the FXML file.
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent parent = loader.load();
        LoginController controller = loader.getController();

        // Build the scene graph.
        Scene scene = new Scene(parent);
//        controller.setUpAlbum();

        // Display our window, using the scene graph.
        stage.setTitle("ImageViewDemo");
        stage.setScene(scene);
        stage.show();
    }
}
