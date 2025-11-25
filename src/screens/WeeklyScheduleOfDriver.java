package screens;

import database.Database;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class WeeklyScheduleOfDriver {
    private VBox root;

    private final TableView<Map<String, String>> table = new TableView<>();
    private final TableColumn<Map<String, String>, String> colDate       = new TableColumn<>("Date");
    private final TableColumn<Map<String, String>, String> colTrip       = new TableColumn<>("Trip");
    private final TableColumn<Map<String, String>, String> colStart      = new TableColumn<>("StartTime");
    private final TableColumn<Map<String, String>, String> colArrival    = new TableColumn<>("ArrivalTime");
    private final TableColumn<Map<String, String>, String> colBus        = new TableColumn<>("BusID");

    public WeeklyScheduleOfDriver(Stage stage) {
        Label title = new Label("Weekly Schedule of a Driver");
        title.getStyleClass().add("screen-title");

        Text lblPrompt = new Text("Enter a driver's name and start date of the week to display schedule:");
        TextField tfDriver = new TextField();
        tfDriver.setPromptText("Driver name");

        TextField tfDate = new TextField();
        tfDate.setPromptText("Start date (YYYY-MM-DD)");

        Button btnShow = new Button("Show weekly schedule");
        btnShow.getStyleClass().add("primary-btn"); 
        Button btnReset = new Button("Reset");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        // Table setup
        colDate.setCellValueFactory   (cd -> new ReadOnlyStringWrapper(cd.getValue().get("date")));
        colTrip.setCellValueFactory   (cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        colStart.setCellValueFactory  (cd -> new ReadOnlyStringWrapper(cd.getValue().get("start")));
        colArrival.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("arrive")));
        colBus.setCellValueFactory    (cd -> new ReadOnlyStringWrapper(cd.getValue().get("bus")));

        table.getColumns().setAll(colDate, colTrip, colStart, colArrival, colBus);
        table.setPlaceholder(new Label("No schedule to display"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(320);

        // Layout
        HBox inputsRow = new HBox(10, tfDriver, tfDate);
        HBox.setHgrow(tfDriver, Priority.ALWAYS);
        HBox.setHgrow(tfDate, Priority.ALWAYS);

        HBox buttonsRow = new HBox(10, btnShow, btnReset, btnBack);
        buttonsRow.setAlignment(Pos.CENTER);

        root = new VBox(12, title, lblPrompt, inputsRow, buttonsRow, table, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnShow.setOnAction(e -> displayWeekly(tfDriver.getText().trim(), tfDate.getText().trim(), status));
        btnReset.setOnAction(e -> {
            tfDriver.clear();
            tfDate.clear();
            status.clear();
            table.getItems().clear();
        });
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));
    }

    private void displayWeekly(String driverName, String startDate, TextArea status) {
        status.clear();
        if (driverName.isEmpty() || startDate.isEmpty()) {
            status.setText("Please enter both Driver name and Start date (YYYY-MM-DD).");
            table.setItems(FXCollections.observableArrayList());
            return;
        }

        final String sql = """
            SELECT TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, BusID
            FROM TripOffering
            WHERE DriverName = ?
              AND Date BETWEEN ? AND DATE_ADD(?, INTERVAL 6 DAY)
            ORDER BY Date, ScheduledStartTime
            """;

        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, driverName);
            ps.setString(2, startDate);
            ps.setString(3, startDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("date",   rs.getDate("Date").toString());
                    row.put("trip",   Integer.toString(rs.getInt("TripNumber")));
                    row.put("start",  rs.getTime("ScheduledStartTime").toString());
                    row.put("arrive", rs.getTime("ScheduledArrivalTime").toString());
                    row.put("bus",    Integer.toString(rs.getInt("BusID")));
                    items.add(row);
                }
            }

            table.setItems(items);
            if (items.isEmpty()) {
                status.setText("No trips found for " + driverName + " starting " + startDate + ".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error fetching weekly schedule: " + ex.getMessage());
        }
    }

    public VBox getView() {
        return root;
    }
}
