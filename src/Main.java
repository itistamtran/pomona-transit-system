import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import screens.HomePage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        HomePage home = new HomePage(stage);
        Scene scene = new Scene(home.getView(), 850, 500);

        // Load styles.css from src/
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );

        stage.setTitle("Pomona Transit System Management");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
