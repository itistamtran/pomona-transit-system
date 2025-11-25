package screens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class HomePage {
    private VBox root;

    public HomePage(Stage stage) {

        Text title = new Text("Pomona Transit System Management");
        title.setFont(Font.font("Arial", 30));
        title.setStyle("-fx-font-weight: bold;");

        Text subtitle = new Text("Please select one of the following options:");
        subtitle.setFont(Font.font("Arial", 16));

        Button displayTrip = new Button("Display trip schedule");
        Button editTrip = new Button("Edit Trip Schedule");
        Button displayStops = new Button("Display stops");
        Button displayWeekly = new Button("Display weekly schedule of a driver");

        Button addDriver = new Button("Add a driver");
        Button addBus = new Button("Add a bus");
        Button deleteBus = new Button("Delete a bus");
        Button addActualStopInfo = new Button("Add actual trip stop info");

        Button[] buttons = {
                displayTrip, editTrip, displayStops, displayWeekly,
                addDriver, addBus, deleteBus, addActualStopInfo
        };

        for (Button b : buttons) {
            b.setPrefWidth(280);
        }

        displayTrip.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        editTrip.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        displayStops.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        displayWeekly.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        addDriver.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        addBus.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        addActualStopInfo.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");
        deleteBus.setStyle("-fx-background-color: #1e4d2b; -fx-text-fill: white;");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(30);
        grid.setVgap(20);

        grid.add(displayTrip, 0, 0);
        grid.add(addDriver, 1, 0);

        grid.add(editTrip, 0, 1);
        grid.add(addBus, 1, 1);

        grid.add(displayStops, 0, 2);
        grid.add(deleteBus, 1, 2);

        grid.add(displayWeekly, 0, 3);
        grid.add(addActualStopInfo, 1, 3);

        VBox container = new VBox(20, title, subtitle, grid);
        container.setPadding(new Insets(30));
        container.setAlignment(Pos.TOP_CENTER);

        this.root = container;

        displayTrip.setOnAction(e -> stage.getScene().setRoot(new DisplayTripSchedule(stage).getView()));
        addDriver.setOnAction(e -> stage.getScene().setRoot(new AddDriver(stage).getView()));
        addBus.setOnAction(e -> stage.getScene().setRoot(new AddBus(stage).getView()));
        deleteBus.setOnAction(e -> stage.getScene().setRoot(new DeleteBus(stage).getView()));
        displayStops.setOnAction(e -> stage.getScene().setRoot(new DisplayStops(stage).getView()));
        displayWeekly.setOnAction(e -> stage.getScene().setRoot(new WeeklyScheduleOfDriver(stage).getView()));
        addActualStopInfo.setOnAction(e -> stage.getScene().setRoot(new AddActualTripStopInfo(stage).getView()));
        editTrip.setOnAction(e -> stage.getScene().setRoot(new EditTripSchedule(stage).getView()));
    }

    public VBox getView() {
        return root;
    }
}
