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

public class DisplayStops {
    private VBox root;

    private final TableView<Map<String, String>> tableStops = new TableView<>();
    private final TableColumn<Map<String, String>, String> colTripNum =
            new TableColumn<>("TripNumber");
    private final TableColumn<Map<String, String>, String> colStopNum =
            new TableColumn<>("StopNumber");
    private final TableColumn<Map<String, String>, String> colSeq =
            new TableColumn<>("Sequence");
    private final TableColumn<Map<String, String>, String> colDrivingTime =
            new TableColumn<>("DrivingTime");
    private final TableColumn<Map<String, String>, String> colStopAddress =
            new TableColumn<>("StopAddress");

    public DisplayStops(Stage stage) {
        Label title = new Label("Display Stops for a Trip");
        title.getStyleClass().add("screen-title");

        Text lblPrompt = new Text("Enter a TripNumber to display all stops along the way:");
        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");

        Button btnShow = new Button("Show stops");
        btnShow.getStyleClass().add("primary-btn");     
        Button btnReset = new Button("Reset");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        // Table setup
        colTripNum.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        colStopNum.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("stop")));
        colSeq.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("seq")));
        colDrivingTime.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("drive")));
        colStopAddress.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("addr")));

        tableStops.getColumns().setAll(colTripNum, colStopNum, colSeq, colDrivingTime, colStopAddress);
        tableStops.setPlaceholder(new Label("No stops to display"));
        tableStops.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableStops.setPrefHeight(320);

        // Layout
        HBox formRow = new HBox(10, tfTrip);
        HBox.setHgrow(tfTrip, Priority.ALWAYS);

        HBox buttonsRow = new HBox(10, btnShow, btnReset, btnBack);
        buttonsRow.setAlignment(Pos.CENTER);

        root = new VBox(12, title, lblPrompt, formRow, buttonsRow, tableStops, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnShow.setOnAction(e -> {
            String t = tfTrip.getText().trim();
            if (t.isEmpty()) {
                status.clear();
                loadStops(null); // all trips
            } else {
                try {
                    int trip = Integer.parseInt(t);
                    status.clear();
                    loadStops(trip);
                } catch (NumberFormatException nfe) {
                    status.setText("TripNumber must be an integer.");
                    tableStops.setItems(FXCollections.observableArrayList());
                }
            }
        });

        btnReset.setOnAction(e -> {
            tfTrip.clear();
            status.clear();
            loadStops(null); // all trips
        });

        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        // Initial load: show ALL trips
        loadStops(null);
    }

    // Loads stops. If tripFilter is null, loads all trips; otherwise loads only that trip
    private void loadStops(Integer tripFilter) {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();

        final String sqlAll =
            "SELECT T1.TripNumber, T1.StopNumber, T1.SequenceNumber, T1.DrivingTime, S.StopAddress " +
            "FROM TripStopInfo AS T1 " +
            "JOIN Stop AS S ON T1.StopNumber = S.StopNumber " +
            "ORDER BY T1.TripNumber, T1.SequenceNumber";

        final String sqlByTrip =
            "SELECT T1.TripNumber, T1.StopNumber, T1.SequenceNumber, T1.DrivingTime, S.StopAddress " +
            "FROM TripStopInfo AS T1 " +
            "JOIN Stop AS S ON T1.StopNumber = S.StopNumber " +
            "WHERE T1.TripNumber = ? " +
            "ORDER BY T1.SequenceNumber";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(tripFilter == null ? sqlAll : sqlByTrip)) {

            if (tripFilter != null) {
                ps.setInt(1, tripFilter);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("trip",  Integer.toString(rs.getInt("TripNumber")));
                    row.put("stop",  Integer.toString(rs.getInt("StopNumber")));
                    row.put("seq",   Integer.toString(rs.getInt("SequenceNumber")));
                    row.put("drive", Integer.toString(rs.getInt("DrivingTime")));
                    row.put("addr",  rs.getString("StopAddress"));
                    items.add(row);
                }
            }

            tableStops.setItems(items);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public VBox getView() {
        return root;
    }
}
