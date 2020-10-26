import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PAPP extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("View.fxml"));
        Parent root = (Parent)loader.load();

        Scene scene = new Scene(root);

        primaryStage.setTitle("PAPP");
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = (Controller)loader.getController();
        controller.setStage(primaryStage);
    }
}
