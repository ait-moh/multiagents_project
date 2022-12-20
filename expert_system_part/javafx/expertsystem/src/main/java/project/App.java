package project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation((this.getClass().getResource("/fxml/main_pane.fxml")));
        Parent mainPane = loader.<Parent>load();

        Scene scene = new Scene(mainPane);
        scene.getStylesheets().add(this.getClass().getResource("/css/style.css").toExternalForm());
        stage.setTitle("Expert System");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

}
