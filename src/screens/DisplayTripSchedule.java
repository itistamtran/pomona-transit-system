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
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DisplayTripSchedule {
    private VBox root;

    private final TableView<Map<String, String>> table = new TableView<>();
    private final TableColumn<Map<String, String>, String> colTrip     = new TableColumn<>("TripNumber");
    private final TableColumn<Map<String, String>, String> colStartLoc = new TableColumn<>("StartLocation");
    private final TableColumn<Map<String, String>, String> colDestLoc  = new TableColumn<>("Destination");
    private final TableColumn<Map<String, String>, String> colDate     = new TableColumn<>("Date");
    private final TableColumn<Map<String, String>, String> colStart    = new TableColumn<>("StartTime");
    private final TableColumn<Map<String, String>, String> colArrive   = new TableColumn<>("ArrivalTime");
    private final TableColumn<Map<String, String>, String> colDriver   = new TableColumn<>("Driver");
    private final TableColumn<Map<String, String>, String> colBus      = new TableColumn<>("BusID");

    public DisplayTripSchedule(Stage stage) {
        Label title = new Label("Display Trip Schedule");
        title.getStyleClass().add("screen-title");

        Text lblPrompt = new Text("Enter start location, destination, and date to search for a trip:");
        TextField tfStart = new TextField(); tfStart.setPromptText("Start location");
        TextField tfDest  = new TextField(); tfDest.setPromptText("Destination");
        TextField tfDate  = new TextField(); tfDate.setPromptText("Date (YYYY-MM-DD)");

        HBox inputs = new HBox(10, tfStart, tfDest, tfDate);
        inputs.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfStart, Priority.ALWAYS);
        HBox.setHgrow(tfDest,  Priority.ALWAYS);
        HBox.setHgrow(tfDate,  Priority.ALWAYS);

        Button btnSearch = new Button("Search");
        btnSearch.getStyleClass().add("primary-btn");
        Button btnReset  = new Button("Reset");
        Button btnBack   = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        // Table setup
        colTrip.setCellValueFactory     (cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        colStartLoc.setCellValueFactory (cd -> new ReadOnlyStringWrapper(cd.getValue().get("startLoc")));
        colDestLoc.setCellValueFactory  (cd -> new ReadOnlyStringWrapper(cd.getValue().get("destLoc")));
        colDate.setCellValueFactory     (cd -> new ReadOnlyStringWrapper(cd.getValue().get("date")));
        colStart.setCellValueFactory    (cd -> new ReadOnlyStringWrapper(cd.getValue().get("start")));
        colArrive.setCellValueFactory   (cd -> new ReadOnlyStringWrapper(cd.getValue().get("arrive")));
        colDriver.setCellValueFactory   (cd -> new ReadOnlyStringWrapper(cd.getValue().get("driver")));
        colBus.setCellValueFactory      (cd -> new ReadOnlyStringWrapper(cd.getValue().get("bus")));

        table.getColumns().setAll(colTrip, colStartLoc, colDestLoc, colDate, colStart, colArrive, colDriver, colBus);
        table.setPlaceholder(new Label("No trip offerings to display"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(360);

        HBox buttons = new HBox(10, btnSearch, btnReset, btnBack); 
        buttons.setAlignment(Pos.CENTER);

        root = new VBox(15, title, lblPrompt, inputs, buttons, table, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnSearch.setOnAction(e ->
                search(tfStart.getText().trim(), tfDest.getText().trim(), tfDate.getText().trim(), status));
        btnReset.setOnAction(e -> { 
            tfStart.clear();
            tfDest.clear();
            tfDate.clear();
            status.clear();
            loadAllTrips();
        });
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        // Initial: show ALL trips
        loadAllTrips();
    }

    /** Load ALL trip offerings (any route, any date). */
    private void loadAllTrips() {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

        final String sql = """
            SELECT
                T.TripNumber,
                T.StartLocationName,
                T.DestinationName,
                O.Date,
                O.ScheduledStartTime,
                O.ScheduledArrivalTime,
                O.DriverName,
                O.BusID
            FROM Trip T
            JOIN TripOffering O ON T.TripNumber = O.TripNumber
            ORDER BY O.Date, O.ScheduledStartTime
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("trip",     Integer.toString(rs.getInt("TripNumber")));
                row.put("startLoc", rs.getString("StartLocationName"));
                row.put("destLoc",  rs.getString("DestinationName"));
                row.put("date",     rs.getDate("Date").toString());
                row.put("start",    rs.getTime("ScheduledStartTime").toString());
                row.put("arrive",   rs.getTime("ScheduledArrivalTime").toString());
                row.put("driver",   rs.getString("DriverName"));
                row.put("bus",      Integer.toString(rs.getInt("BusID")));
                items.add(row);
            }
            table.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            table.setItems(FXCollections.observableArrayList());
        }
    }

    /** Filter by Start, Destination, and Date; if any are missing, keep current table and show a message. */
    private void search(String start, String dest, String date, TextArea status) {
        status.clear();

        if (start.isEmpty() || dest.isEmpty() || date.isEmpty()) {
            status.setText("Please enter Start location, Destination, and Date (YYYY-MM-DD) to filter.");
            return;
        }

        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

        final String sql = """
            SELECT
                T.TripNumber,
                T.StartLocationName,
                T.DestinationName,
                O.Date,
                O.ScheduledStartTime,
                O.ScheduledArrivalTime,
                O.DriverName,
                O.BusID
            FROM Trip T
            JOIN TripOffering O ON T.TripNumber = O.TripNumber
            WHERE T.StartLocationName = ?
              AND T.DestinationName   = ?
              AND O.Date              = ?
            ORDER BY O.ScheduledStartTime
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, dest);
            ps.setString(3, date);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("trip",     Integer.toString(rs.getInt("TripNumber")));
                    row.put("startLoc", rs.getString("StartLocationName"));
                    row.put("destLoc",  rs.getString("DestinationName"));
                    row.put("date",     rs.getDate("Date").toString());
                    row.put("start",    rs.getTime("ScheduledStartTime").toString());
                    row.put("arrive",   rs.getTime("ScheduledArrivalTime").toString());
                    row.put("driver",   rs.getString("DriverName"));
                    row.put("bus",      Integer.toString(rs.getInt("BusID")));
                    items.add(row);
                }
            }

            table.setItems(items);
            if (items.isEmpty()) {
                status.setText("No trip offerings found for: " + start + " → " + dest + " on " + date + ".");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error while fetching schedule: " + ex.getMessage());
        }
    }

    public VBox getView() {
        return root;
    }
}
