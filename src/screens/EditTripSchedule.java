package screens;

import database.Database;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class EditTripSchedule {
    private VBox root;

    public EditTripSchedule(Stage stage) {
        Label title = new Label("Edit Trip Schedule");
        title.getStyleClass().add("screen-title");

        Text lblPrompt = new Text("Select an option and click Go:");

        ChoiceBox<String> choice = new ChoiceBox<>();
        choice.getItems().addAll("Delete trip offering", "Add trip offering", "Change driver", "Change bus");
        choice.setValue("Delete trip offering");

        Button btnGo = new Button("Go");
        btnGo.getStyleClass().add("primary-btn");
        Button btnBack = new Button("Back");

        VBox placeholder = new VBox(12);
        placeholder.setAlignment(Pos.TOP_CENTER);

        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));
        btnGo.setOnAction(e -> {
            placeholder.getChildren().clear();
            switch (choice.getValue()) {
                case "Delete trip offering" -> placeholder.getChildren().add(buildDeleteView());
                case "Add trip offering"    -> placeholder.getChildren().add(buildAddView());
                case "Change driver"        -> placeholder.getChildren().add(buildChangeDriverView());
                case "Change bus"           -> placeholder.getChildren().add(buildChangeBusView());
            }
        });

        root = new VBox(12, title, lblPrompt, choice, btnGo, btnBack, placeholder);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    public VBox getView() { return root; }

    /** Builds a TripOffering table with standard columns (reused by all sub-screens). */
    private TableView<Map<String,String>> createTripOfferingTable() {
        TableView<Map<String,String>> table = new TableView<>();

        var cTrip  = new TableColumn<Map<String,String>, String>("TripNumber");
        var cDate  = new TableColumn<Map<String,String>, String>("Date");
        var cStart = new TableColumn<Map<String,String>, String>("ScheduledStartTime");
        var cArr   = new TableColumn<Map<String,String>, String>("ScheduledArrivalTime");
        var cDrv   = new TableColumn<Map<String,String>, String>("DriverName");
        var cBus   = new TableColumn<Map<String,String>, String>("BusID");

        cTrip .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("trip")));
        cDate .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("date")));
        cStart.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("sstart")));
        cArr  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("sarr")));
        cDrv  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("driver")));
        cBus  .setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("bus")));

        table.getColumns().setAll(cTrip, cDate, cStart, cArr, cDrv, cBus);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No trip offerings"));
        table.setPrefHeight(260);
        return table;
    }

    /** Loads TripOffering rows into the given table. Call after any CRUD to refresh. */
    private void loadTripOffering(TableView<Map<String,String>> table) {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = """
            SELECT TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, DriverName, BusID
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
                row.put("driver", rs.getString("DriverName"));
                row.put("bus",    Integer.toString(rs.getInt("BusID")));
                items.add(row);
            }
            table.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
            table.setItems(FXCollections.observableArrayList());
        }
    }

    // Delete Trip Offering
    private Node buildDeleteView() {
        Label head = new Label("Delete Trip Offering");
        head.getStyleClass().add("action-title");

        TableView<Map<String,String>> table = createTripOfferingTable();
        loadTripOffering(table);

        TextField tfTrip  = new TextField(); tfTrip.setPromptText("TripNumber");
        TextField tfDate  = new TextField(); tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField(); tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");

        HBox form = new HBox(10, tfTrip, tfDate, tfStart);
        form.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfTrip, Priority.ALWAYS);
        HBox.setHgrow(tfDate, Priority.ALWAYS);
        HBox.setHgrow(tfStart, Priority.ALWAYS);

        table.setOnMouseClicked(e -> {
            var r = table.getSelectionModel().getSelectedItem();
            if (r != null) { tfTrip.setText(r.get("trip")); tfDate.setText(r.get("date")); tfStart.setText(r.get("sstart")); }
        });

        TextArea status = new TextArea(); status.setEditable(false); status.setPrefRowCount(3);

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("primary-btn");
        btnDelete.setOnAction(e -> {
            status.clear();
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                String date = tfDate.getText().trim();
                String start = tfStart.getText().trim();
                final String sql = "DELETE FROM TripOffering WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?";
                try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, trip); ps.setString(2, date); ps.setString(3, start);
                    int rows = ps.executeUpdate();
                    status.setText(rows > 0 ? "Trip offering deleted." : "No matching trip offering found.");
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber must be an integer.");
            } catch (Exception ex) {
                ex.printStackTrace(); status.setText("Error deleting trip offering: " + ex.getMessage());
            }
            loadTripOffering(table);
        });

        return new VBox(10, head, form, table, btnDelete, status);
    }

    // Add Trip Offering
    private Node buildAddView() {
        Label head = new Label("Add Trip Offering");
        head.getStyleClass().add("action-title");

        TableView<Map<String,String>> table = createTripOfferingTable();
        loadTripOffering(table);

        TextField tfTrip   = new TextField(); tfTrip.setPromptText("TripNumber");
        TextField tfDate   = new TextField(); tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart  = new TextField(); tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfArr    = new TextField(); tfArr.setPromptText("Scheduled Arrival Time (HH:MM:SS)");
        TextField tfDriver = new TextField(); tfDriver.setPromptText("Driver name");
        TextField tfBus    = new TextField(); tfBus.setPromptText("BusID");

        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(8);
        grid.add(tfTrip, 0,0); grid.add(tfDate, 1,0);
        grid.add(tfStart,0,1); grid.add(tfArr,  1,1);
        grid.add(tfDriver,0,2); grid.add(tfBus, 1,2);

        // click row → prefill inputs
        table.setOnMouseClicked(e -> {
            var r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                tfTrip.setText(r.get("trip")); tfDate.setText(r.get("date"));
                tfStart.setText(r.get("sstart")); tfArr.setText(r.get("sarr"));
                tfDriver.setText(r.get("driver")); tfBus.setText(r.get("bus"));
            }
        });

        TextArea status = new TextArea(); status.setEditable(false); status.setPrefRowCount(3);

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().add("primary-btn");
        btnAdd.setOnAction(e -> {
            status.clear();
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                int bus  = Integer.parseInt(tfBus.getText().trim());
                final String sql = """
                    INSERT INTO TripOffering
                    (TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, DriverName, BusID)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
                try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
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
                ex.printStackTrace(); status.setText("Error adding trip offering: " + ex.getMessage());
            }
            loadTripOffering(table);
        });

        return new VBox(10, head, grid, table, btnAdd, status);
    }

    // Change Driver
    private Node buildChangeDriverView() {
        Label head = new Label("Change Driver");
        head.getStyleClass().add("action-title");

        TableView<Map<String,String>> table = createTripOfferingTable();
        loadTripOffering(table);

        TextField tfTrip   = new TextField(); tfTrip.setPromptText("TripNumber");
        TextField tfDate   = new TextField(); tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart  = new TextField(); tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfDriver = new TextField(); tfDriver.setPromptText("New Driver Name");

        HBox form = new HBox(10, tfTrip, tfDate, tfStart, tfDriver);
        form.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfTrip, Priority.ALWAYS);
        HBox.setHgrow(tfDate, Priority.ALWAYS);
        HBox.setHgrow(tfStart, Priority.ALWAYS);
        HBox.setHgrow(tfDriver, Priority.ALWAYS);

        table.setOnMouseClicked(e -> {
            var r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                tfTrip.setText(r.get("trip")); tfDate.setText(r.get("date"));
                tfStart.setText(r.get("sstart")); tfDriver.setText(r.get("driver"));
            }
        });

        TextArea status = new TextArea(); status.setEditable(false); status.setPrefRowCount(3);

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().add("primary-btn");
        btnUpdate.setOnAction(e -> {
            status.clear();
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                final String sql = """
                    UPDATE TripOffering
                    SET DriverName = ?
                    WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?
                    """;
                try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, tfDriver.getText().trim());
                    ps.setInt(2, trip);
                    ps.setString(3, tfDate.getText().trim());
                    ps.setString(4, tfStart.getText().trim());
                    int rows = ps.executeUpdate();
                    status.setText(rows > 0 ? "Driver updated." : "No matching trip offering found.");
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber must be an integer.");
            } catch (Exception ex) {
                ex.printStackTrace(); status.setText("Error updating driver: " + ex.getMessage());
            }
            loadTripOffering(table);
        });

        return new VBox(10, head, form, table, btnUpdate, status);
    }

    // Change Bus
    private Node buildChangeBusView() {
        Label head = new Label("Change Bus");
        head.getStyleClass().add("action-title");

        TableView<Map<String,String>> table = createTripOfferingTable();
        loadTripOffering(table);

        TextField tfTrip  = new TextField(); tfTrip.setPromptText("TripNumber");
        TextField tfDate  = new TextField(); tfDate.setPromptText("Date (YYYY-MM-DD)");
        TextField tfStart = new TextField(); tfStart.setPromptText("Scheduled Start Time (HH:MM:SS)");
        TextField tfBus   = new TextField(); tfBus.setPromptText("New BusID");

        HBox form = new HBox(10, tfTrip, tfDate, tfStart, tfBus);
        form.setAlignment(Pos.CENTER);
        HBox.setHgrow(tfTrip, Priority.ALWAYS);
        HBox.setHgrow(tfDate, Priority.ALWAYS);
        HBox.setHgrow(tfStart, Priority.ALWAYS);
        HBox.setHgrow(tfBus, Priority.ALWAYS);

        table.setOnMouseClicked(e -> {
            var r = table.getSelectionModel().getSelectedItem();
            if (r != null) {
                tfTrip.setText(r.get("trip")); tfDate.setText(r.get("date"));
                tfStart.setText(r.get("sstart")); tfBus.setText(r.get("bus"));
            }
        });

        TextArea status = new TextArea(); status.setEditable(false); status.setPrefRowCount(3);

        Button btnUpdate = new Button("Update");
        btnUpdate.getStyleClass().add("primary-btn");
        btnUpdate.setOnAction(e -> {
            status.clear();
            try {
                int trip = Integer.parseInt(tfTrip.getText().trim());
                int bus  = Integer.parseInt(tfBus.getText().trim());
                final String sql = """
                    UPDATE TripOffering
                    SET BusID = ?
                    WHERE TripNumber = ? AND Date = ? AND ScheduledStartTime = ?
                    """;
                try (Connection conn = Database.connect(); PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setInt(1, bus);
                    ps.setInt(2, trip);
                    ps.setString(3, tfDate.getText().trim());
                    ps.setString(4, tfStart.getText().trim());
                    int rows = ps.executeUpdate();
                    status.setText(rows > 0 ? "Bus updated." : "No matching trip offering found.");
                }
            } catch (NumberFormatException nfe) {
                status.setText("TripNumber and BusID must be integers.");
            } catch (Exception ex) {
                ex.printStackTrace(); status.setText("Error updating bus: " + ex.getMessage());
            }
            loadTripOffering(table);
        });

        return new VBox(10, head, form, table, btnUpdate, status);
    }
}
