package screens;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class EditTripSchedule {
    private VBox root;

    public EditTripSchedule(Stage stage) {
        Label title = new Label("Edit Trip Schedule");

        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.getItems().addAll(
                "Delete trip offering",
                "Add trip offering",
                "Change driver",
                "Change bus"
        );
        choice.setValue("Delete trip offering");

        Button btnGo = new Button("Go");
        Button btnBack = new Button("Back");

        VBox placeholder = new VBox();
        placeholder.setAlignment(Pos.CENTER);

        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        btnGo.setOnAction(e -> {
            String selected = choice.getValue();
            switch (selected) {
                case "Delete trip offering" -> showDeleteForm(placeholder);
                case "Add trip offering" -> showAddForm(placeholder);
                case "Change driver" -> showChangeDriverForm(placeholder);
                case "Change bus" -> showChangeBusForm(placeholder);
            }
        });

        root = new VBox(12, title, choice, btnGo, btnBack, placeholder);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void showDeleteForm(VBox container) {
        container.getChildren().clear();

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");
        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField();
        tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        Button btnDelete = new Button("Delete");

        btnDelete.setOnAction(e -> {
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                String date = tfDate.getText().trim();
                String start = tfStart.getText().trim();

                String sql = "DELETE FROM TripOffering WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?";

                try (Connection conn = Database.connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, trip);
                    ps.setString(2, date);
                    ps.setString(3, start);

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        status.setText("Trip offering deleted.");
                    } else {
                        status.setText("No matching trip offering found.");
                    }
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber must be an integer.");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Error deleting trip offering, check console.");
            }
        });

        container.getChildren().addAll(new Label("Delete Trip Offering"),
                tfTrip, tfDate, tfStart, btnDelete, status);
    }

    private void showAddForm(VBox container) {
        container.getChildren().clear();

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");
        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField();
        tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfArr = new TextField();
        tfArr.setPromptText("Scheduled Arrival Time (HH:MM:SS)");
        TextField tfDriver = new TextField();
        tfDriver.setPromptText("Driver name");
        TextField tfBus = new TextField();
        tfBus.setPromptText("BusID");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        Button btnAdd = new Button("Add");

        btnAdd.setOnAction(e -> {
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                int bus = Integer.parseInt(tfBus.getText().trim());
                String sql = """
                    INSERT INTO TripOffering
                    (TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, DriverName, BusID)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

                try (Connection conn = Database.connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, trip);
                    ps.setString(2, tfDate.getText().trim());
                    ps.setString(3, tfStart.getText().trim());
                    ps.setString(4, tfArr.getText().trim());
                    ps.setString(5, tfDriver.getText().trim());
                    ps.setInt(6, bus);

                    ps.executeUpdate();
                    status.setText("Trip offering added.");
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber and BusID must be integers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Error adding trip offering, check console.");
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.add(tfTrip, 0, 0);
        grid.add(tfDate, 1, 0);
        grid.add(tfStart, 0, 1);
        grid.add(tfArr, 1, 1);
        grid.add(tfDriver, 0, 2);
        grid.add(tfBus, 1, 2);

        container.getChildren().addAll(new Label("Add Trip Offering"), grid, btnAdd, status);
    }

    private void showChangeDriverForm(VBox container) {
        container.getChildren().clear();

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");
        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField();
        tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfDriver = new TextField();
        tfDriver.setPromptText("New Driver Name");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        Button btnUpdate = new Button("Update");

        btnUpdate.setOnAction(e -> {
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                String sql = """
                    UPDATE TripOffering
                    SET DriverName = ?
                    WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?
                    """;

                try (Connection conn = Database.connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setString(1, tfDriver.getText().trim());
                    ps.setInt(2, trip);
                    ps.setString(3, tfDate.getText().trim());
                    ps.setString(4, tfStart.getText().trim());

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        status.setText("Driver updated.");
                    } else {
                        status.setText("No matching trip offering found.");
                    }
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber must be an integer.");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Error updating driver, check console.");
            }
        });

        container.getChildren().addAll(new Label("Change Driver"), tfTrip, tfDate, tfStart, tfDriver, btnUpdate, status);
    }

    private void showChangeBusForm(VBox container) {
        container.getChildren().clear();

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");
        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField();
        tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfBus = new TextField();
        tfBus.setPromptText("New BusID");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        Button btnUpdate = new Button("Update");

        btnUpdate.setOnAction(e -> {
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                int bus = Integer.parseInt(tfBus.getText().trim());

                String sql = """
                    UPDATE TripOffering
                    SET BusID = ?
                    WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?
                    """;

                try (Connection conn = Database.connect();
                     PreparedStatement ps = conn.prepareStatement(sql)) {

                    ps.setInt(1, bus);
                    ps.setInt(2, trip);
                    ps.setString(3, tfDate.getText().trim());
                    ps.setString(4, tfStart.getText().trim());

                    int rows = ps.executeUpdate();
                    if (rows > 0) {
                        status.setText("Bus updated.");
                    } else {
                        status.setText("No matching trip offering found.");
                    }
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber and BusID must be integers.");
            } catch (Exception ex) {
                ex.printStackTrace();
                status.setText("Error updating bus, check console.");
            }
        });

        container.getChildren().addAll(new Label("Change Bus"), tfTrip, tfDate, tfStart, tfBus, btnUpdate, status);
    }

    public VBox getView() {
        return root;
    }
}
