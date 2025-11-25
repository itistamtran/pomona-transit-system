package screens;

import database.Database;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AddActualTripStopInfo {
    private VBox root;

    private final TextField tfTrip = new TextField();
    private final TextField tfDate = new TextField();
    private final TextField tfSchedStart = new TextField();
    private final TextField tfStop = new TextField();

    private final TableView<Map<String, String>> tableOffer = new TableView<>();
    private final TableView<Map<String, String>> tableStop  = new TableView<>();

    private final TableView<Map<String, String>> tableResult = new TableView<>();

    public AddActualTripStopInfo(Stage stage) {
        Label title = new Label("Add Actual Trip Stop Info");
        title.getStyleClass().add("screen-title");

        // Instructions
        Text tip = new Text("1) Enter TripNumber, Date, ScheduledStartTime and StopNumber\n"
                + "2) Click \"Add Actual Data\" to enter actual information of the trip");
        tip.wrappingWidthProperty().set(900);

        // Key inputs
        tfTrip.setPromptText("TripNumber (e.g., 1)");
        tfDate.setPromptText("Date (YYYY-MM-DD)");
        tfSchedStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        tfStop.setPromptText("StopNumber (e.g., 100)");

        HBox keyRow = new HBox(10, tfTrip, tfDate, tfSchedStart, tfStop);
        keyRow.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfTrip, Priority.ALWAYS);
        HBox.setHgrow(tfDate, Priority.ALWAYS);
        HBox.setHgrow(tfSchedStart, Priority.ALWAYS);
        HBox.setHgrow(tfStop, Priority.ALWAYS);

        // Buttons
        Button btnAddActual = new Button("Add Actual Data");
        btnAddActual.getStyleClass().add("primary-btn");
        Button btnRefresh   = new Button("Refresh Lists");
        Button btnBack      = new Button("Back");
        HBox buttons = new HBox(10, btnAddActual, btnRefresh, btnBack);
        buttons.setAlignment(Pos.CENTER);

        // Status
        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(3);

        // TripOffering table (TripNumber | Date | ScheduledStartTime)
        var colTOTrip  = new TableColumn<Map<String,String>, String>("TripNumber");
        var colTODate  = new TableColumn<Map<String,String>, String>("Date");
        var colTOStart = new TableColumn<Map<String,String>, String>("ScheduledStartTime");
        var colTOArr   = new TableColumn<Map<String,String>, String>("ScheduledArrivalTime"); 

        colTOTrip.setCellValueFactory (cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        colTODate.setCellValueFactory (cd -> new ReadOnlyStringWrapper(cd.getValue().get("date")));
        colTOStart.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("sstart")));
        colTOArr.setCellValueFactory   (cd -> new ReadOnlyStringWrapper(cd.getValue().get("sarr")));

        tableOffer.getColumns().setAll(colTOTrip, colTODate, colTOStart, colTOArr);
        tableOffer.setPlaceholder(new Label("No trip offerings found"));
        tableOffer.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableOffer.setPrefHeight(220);

        // Click to populate keys
        tableOffer.setOnMouseClicked(e -> {
            var row = tableOffer.getSelectionModel().getSelectedItem();
            if (row != null) {
                tfTrip.setText(row.get("trip"));
                tfDate.setText(row.get("date"));
                tfSchedStart.setText(row.get("sstart"));
            }
        });

        // Stop table (StopNumber | StopAddress)
        var colStopNum  = new TableColumn<Map<String,String>, String>("StopNumber");
        var colStopAddr = new TableColumn<Map<String,String>, String>("StopAddress");

        colStopNum.setCellValueFactory (cd -> new ReadOnlyStringWrapper(cd.getValue().get("snum")));
        colStopAddr.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("saddr")));

        tableStop.getColumns().setAll(colStopNum, colStopAddr);
        tableStop.setPlaceholder(new Label("No stops found"));
        tableStop.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableStop.setPrefHeight(220);

        // Click to set StopNumber
        tableStop.setOnMouseClicked(e -> {
            var row = tableStop.getSelectionModel().getSelectedItem();
            if (row != null) {
                tfStop.setText(row.get("snum"));
            }
        });

        // Layout two tables side-by-side
        VBox left  = new VBox(8, new Label("TripOffering (TripNumber | Date | ScheduledStartTime | ScheduledArrivalTime)"), tableOffer);
        VBox right = new VBox(8, new Label("Stop (StopNumber | StopAddress)"), tableStop);
        left.setFillWidth(true);
        right.setFillWidth(true);
        HBox tablesRow = new HBox(15, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        // Result table shows the inserted ActualTripStopInfo row
        var colRTrip   = new TableColumn<Map<String,String>, String>("TripNumber");
        var colRDate   = new TableColumn<Map<String,String>, String>("Date");
        var colRStart  = new TableColumn<Map<String,String>, String>("ScheduledStartTime");
        var colRStop   = new TableColumn<Map<String,String>, String>("StopNumber");
        var colRSArr   = new TableColumn<Map<String,String>, String>("ScheduledArrivalTime");
        var colRAStart = new TableColumn<Map<String,String>, String>("ActualStartTime");
        var colRArr    = new TableColumn<Map<String,String>, String>("ActualArrivalTime");
        var colRIn     = new TableColumn<Map<String,String>, String>("PassengerIn");
        var colROut    = new TableColumn<Map<String,String>, String>("PassengerOut");

        colRTrip  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        colRDate  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("date")));
        colRStart .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("sstart")));
        colRStop  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("stop")));
        colRSArr  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("sarr")));
        colRAStart.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("astart")));
        colRArr   .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("aarr")));
        colRIn    .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("pin")));
        colROut   .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("pout")));

        tableResult.getColumns().setAll(colRTrip, colRDate, colRStart, colRStop, colRSArr, colRAStart, colRArr, colRIn, colROut);
        tableResult.setPlaceholder(new Label("Inserted row will appear here"));
        tableResult.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableResult.setPrefHeight(200);

        // Root layout
        root = new VBox(12, title, tip, keyRow, buttons, tablesRow, new Label("Result (ActualTripStopInfo):"), tableResult, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));
        btnRefresh.setOnAction(e -> {
            status.clear();
            loadTripOffering();
            loadStops();
        });
        btnAddActual.setOnAction(e -> openActualsDialog(stage, status));

        // Initial loads
        loadTripOffering();
        loadStops();
    }

    /** Show a modal dialog to collect actual data */
    private void openActualsDialog(Stage owner, TextArea status) {
        status.clear();

        // Validate keys first
        String stTrip = tfTrip.getText().trim();
        String stDate = tfDate.getText().trim();
        String stSchedStart = tfSchedStart.getText().trim();
        String stStop = tfStop.getText().trim();

        if (stTrip.isEmpty() || stDate.isEmpty() || stSchedStart.isEmpty() || stStop.isEmpty()) {
            status.setText("Please enter TripNumber, Date, ScheduledStartTime, and StopNumber first.");
            return;
        }

        // Dialog UI 
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Enter Actual Trip Stop Data");

        TextField tfActualStart = new TextField(); tfActualStart.setPromptText("Actual Start Time (HH:MM:SS)");
        TextField tfActualArr   = new TextField(); tfActualArr.setPromptText("Actual Arrival Time (HH:MM:SS)");
        TextField tfIn          = new TextField(); tfIn.setPromptText("Passengers IN (int)");
        TextField tfOut         = new TextField(); tfOut.setPromptText("Passengers OUT (int)");

        Button btnSave   = new Button("Save");
        btnSave.getStyleClass().add("primary-btn");
        Button btnCancel = new Button("Cancel");

        HBox btns = new HBox(10, btnSave, btnCancel);
        btns.setAlignment(Pos.CENTER_RIGHT);

        VBox pane = new VBox(10,
                new Label("Keys (context): Trip=" + stTrip + ", Date=" + stDate
                        + ", ScheduledStartTime=" + stSchedStart + ", Stop=" + stStop),
                tfActualStart, tfActualArr, tfIn, tfOut, btns);
        pane.setPadding(new Insets(16));
        pane.getStylesheets().add("styles.css");

        dialog.setScene(new Scene(pane, 520, 260));

        // Actions
        btnCancel.setOnAction(e -> dialog.close());
        btnSave.setOnAction(e -> {
            // Validate integers for IN/OUT and keys
            try {
                Integer.parseInt(stTrip);
                Integer.parseInt(stStop);
                Integer.parseInt(tfIn.getText().trim());
                Integer.parseInt(tfOut.getText().trim());
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber, StopNumber, IN and OUT must be integers.");
                return;
            }

            // Insert 
            boolean ok = saveActual(
                    stTrip, stDate, stSchedStart, stStop,
                    tfActualStart.getText().trim(),
                    tfActualArr.getText().trim(),
                    tfIn.getText().trim(),
                    tfOut.getText().trim(),
                    status
            );
            if (ok) {
                dialog.close();
                loadInsertedRow(stTrip, stDate, stSchedStart, stStop);
            }
        });

        dialog.showAndWait();
    }

    /** Insert ActualTripStopInfo, fetching ScheduledArrivalTime from TripOffering. */
    private boolean saveActual(String tripStr, String date, String schedStart,
                               String stopStr, String actualStart,
                               String actualArr, String inStr, String outStr,
                               TextArea status) 
    {
        String scheduledArrival = null;
        final String lookupSql = """
            SELECT ScheduledArrivalTime
            FROM TripOffering
            WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?
            """;
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(lookupSql)) {

            ps.setInt(1, Integer.parseInt(tripStr));
            ps.setString(2, date);
            ps.setString(3, schedStart);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var t = rs.getTime("ScheduledArrivalTime");
                    scheduledArrival = (t == null) ? null : t.toString();
                } else {
                    status.setText("No matching TripOffering found to derive ScheduledArrivalTime.");
                    return false;
                }
            }

            // Insert into ActualTripStopInfo using fetched scheduledArrival
            final String insertSql = """
                INSERT INTO ActualTripStopInfo
                (TripNumber, Date, ScheduledStartTime, StopNumber,
                 ScheduledArrivalTime, ActualStartTime, ActualArrivalTime,
                 NumberOfPassengerIn, NumberOfPassengerOut)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
            try (PreparedStatement ins = conn.prepareStatement(insertSql)) {
                ins.setInt(1, Integer.parseInt(tripStr));
                ins.setString(2, date);
                ins.setString(3, schedStart);
                ins.setInt(4, Integer.parseInt(stopStr));
                ins.setString(5, scheduledArrival);  // fetched value
                ins.setString(6, actualStart);
                ins.setString(7, actualArr);
                ins.setInt(8, Integer.parseInt(inStr));
                ins.setInt(9, Integer.parseInt(outStr));
                ins.executeUpdate();
            }

            status.setText("Actual trip stop info saved.");
            return true;

        } catch (NumberFormatException nfe) {
            status.setText("TripNumber, StopNumber, Passengers IN and OUT must be integers.");
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error saving actual trip stop info: " + ex.getMessage());
            return false;
        }
    }

    /** Load TripOffering: TripNumber | Date | ScheduledStartTime */
    private void loadTripOffering() {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = """
            SELECT TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime
            FROM TripOffering
            ORDER BY TripNumber, Date, ScheduledStartTime
            """;
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                row.put("trip",   Integer.toString(rs.getInt("TripNumber")));
                row.put("date",   rs.getDate("Date").toString());
                row.put("sstart", rs.getTime("ScheduledStartTime").toString());
                row.put("sarr",   rs.getTime("ScheduledArrivalTime") == null ? "" : rs.getTime("ScheduledArrivalTime").toString()); 
                items.add(row);
            }
            tableOffer.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            tableOffer.setItems(FXCollections.observableArrayList());
        }
    }

    /** Load Stop: StopNumber | StopAddress */
    private void loadStops() {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = "SELECT StopNumber, StopAddress FROM Stop ORDER BY StopNumber";
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String,String> row = new HashMap<>();
                row.put("snum",  Integer.toString(rs.getInt("StopNumber")));
                row.put("saddr", rs.getString("StopAddress"));
                items.add(row);
            }
            tableStop.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            tableStop.setItems(FXCollections.observableArrayList());
        }
    }

    /** After saving, fetch and show the inserted row in tableResult. */
    private void loadInsertedRow(String tripStr, String date, String sstart, String stopStr) {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = """
            SELECT TripNumber, Date, ScheduledStartTime, StopNumber,
                   ScheduledArrivalTime, ActualStartTime, ActualArrivalTime,
                   NumberOfPassengerIn, NumberOfPassengerOut
            FROM ActualTripStopInfo
            WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ? AND StopNumber = ?
            """;
        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(tripStr));
            ps.setString(2, date);
            ps.setString(3, sstart);
            ps.setInt(4, Integer.parseInt(stopStr));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String,String> row = new HashMap<>();
                    row.put("trip",   Integer.toString(rs.getInt("TripNumber")));
                    row.put("date",   rs.getDate("Date").toString());
                    row.put("sstart", rs.getTime("ScheduledStartTime").toString());
                    row.put("stop",   Integer.toString(rs.getInt("StopNumber")));
                    row.put("sarr",   rs.getTime("ScheduledArrivalTime") == null ? "" : rs.getTime("ScheduledArrivalTime").toString());
                    row.put("astart", rs.getTime("ActualStartTime") == null ? "" : rs.getTime("ActualStartTime").toString());
                    row.put("aarr",   rs.getTime("ActualArrivalTime") == null ? "" : rs.getTime("ActualArrivalTime").toString());
                    row.put("pin",    Integer.toString(rs.getInt("NumberOfPassengerIn")));
                    row.put("pout",   Integer.toString(rs.getInt("NumberOfPassengerOut")));
                    items.add(row);
                }
            }
            tableResult.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            tableResult.setItems(FXCollections.observableArrayList());
        }
    }

    public VBox getView() {
        return root;
    }
}
