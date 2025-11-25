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
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AddBus {
    private VBox root;

    private final TableView<Map<String, String>> tableBus = new TableView<>();
    private final TableColumn<Map<String, String>, String> colBusId =
            new TableColumn<>("BusID");
    private final TableColumn<Map<String, String>, String> colModel =
            new TableColumn<>("Model");
    private final TableColumn<Map<String, String>, String> colYear =
            new TableColumn<>("Year");

    public AddBus(Stage stage) {
        Label title = new Label("Add a Bus");
        title.getStyleClass().add("screen-title");

        // Inputs
        TextField tfBusID = new TextField();
        tfBusID.setPromptText("BusID (integer)");

        TextField tfModel = new TextField();
        tfModel.setPromptText("Model");

        TextField tfYear = new TextField();
        tfYear.setPromptText("Year (integer)");

        // Buttons
        Button btnAdd = new Button("Add bus");
        btnAdd.getStyleClass().add("primary-btn");   // same green style as AddDriver
        Button btnBack = new Button("Back");

        // Status
        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        // Table setup
        colBusId.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("busId")));
        colModel.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("model")));
        colYear.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("year")));

        colBusId.setPrefWidth(120);
        colModel.setPrefWidth(260);
        colYear.setPrefWidth(120);

        tableBus.getColumns().setAll(colBusId, colModel, colYear);
        tableBus.setPlaceholder(new Label("No buses found"));
        tableBus.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableBus.setPrefHeight(300);

        // Layout rows
        HBox formRow = new HBox(10, tfBusID, tfModel, tfYear);
        HBox.setHgrow(tfBusID, Priority.ALWAYS);
        HBox.setHgrow(tfModel,  Priority.ALWAYS);
        HBox.setHgrow(tfYear,   Priority.ALWAYS);

        HBox buttonsRow = new HBox(10, btnAdd, btnBack);
        buttonsRow.setAlignment(Pos.CENTER);

        root = new VBox(12, title, formRow, buttonsRow, tableBus, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnAdd.setOnAction(e -> {
            addBus(tfBusID.getText().trim(), tfModel.getText().trim(), tfYear.getText().trim(), status);
            loadAllBuses();        // refresh after insert
            tfBusID.clear();
            tfModel.clear();
            tfYear.clear();
        });
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        // Initial data load
        loadAllBuses();
    }

    private void addBus(String busIDStr, String model, String yearStr, TextArea status) {
        status.clear();
        if (busIDStr.isEmpty() || yearStr.isEmpty()) {
            status.setText("BusID and Year are required (integers).");
            return;
        }
        try {
            int busID = Integer.parseInt(busIDStr);
            int year  = Integer.parseInt(yearStr);

            final String sql = "INSERT INTO Bus (BusID, Model, Year) VALUES (?, ?, ?)";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, busID);
                ps.setString(2, model.isEmpty() ? null : model);
                ps.setInt(3, year);
                ps.executeUpdate();
                status.setText("Bus added successfully.");
            }
        } catch (NumberFormatException nfe) {
            status.setText("BusID and Year must be integers.");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error adding bus: " + ex.getMessage());
        }
    }

    private void loadAllBuses() {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = "SELECT BusID, Model, Year FROM Bus ORDER BY BusID";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("busId", Integer.toString(rs.getInt("BusID")));
                row.put("model", rs.getString("Model"));
                row.put("year",  Integer.toString(rs.getInt("Year")));
                items.add(row);
            }
            tableBus.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public VBox getView() {
        return root;
    }
}