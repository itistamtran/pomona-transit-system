package screens;

import database.Database;
import javafx.beans.property.ReadOnlyStringWrapper;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class AddDriver {
    private VBox root;

    private final TableView<Map<String, String>> tableDriver = new TableView<>();
    private final TableColumn<Map<String, String>, String> colDriverName =
            new TableColumn<>("Driver");
    private final TableColumn<Map<String, String>, String> colPhoneNumber =
            new TableColumn<>("Telephone");

    public AddDriver(Stage stage) {
        Label title = new Label("Add a Driver");
        title.getStyleClass().add("screen-title");

        // Inputs
        TextField tfName = new TextField();
        tfName.setPromptText("Driver name");

        TextField tfPhone = new TextField();
        tfPhone.setPromptText("Telephone number");

        // Buttons
        Button btnAdd = new Button("Add driver");
        btnAdd.getStyleClass().add("primary-btn");  
        Button btnBack = new Button("Back");

        // Status
        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        // Table setup
        colDriverName.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("name")));
        colPhoneNumber.setCellValueFactory(cd -> new ReadOnlyStringWrapper(cd.getValue().get("phone")));
        colDriverName.setPrefWidth(300);
        colPhoneNumber.setPrefWidth(260);

        tableDriver.getColumns().setAll(colDriverName, colPhoneNumber);
        tableDriver.setPlaceholder(new Label("No drivers found"));
        tableDriver.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tableDriver.setPrefHeight(300);

        // Layout rows
        HBox formRow1 = new HBox(10, tfName, tfPhone);
        HBox.setHgrow(tfName, Priority.ALWAYS);
        HBox.setHgrow(tfPhone, Priority.ALWAYS);

        HBox buttonsRow = new HBox(10, btnAdd, btnBack);
        buttonsRow.setAlignment(Pos.CENTER);

        root = new VBox(12, title, formRow1, buttonsRow, tableDriver, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnAdd.setOnAction(e -> {
            addDriver(tfName.getText().trim(), tfPhone.getText().trim(), status);
            loadAllDrivers();    // refresh table after insert
            tfName.clear();
            tfPhone.clear();
        });
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        // Initial load
        loadAllDrivers();
    }

    private void addDriver(String name, String phone, TextArea status) {
        status.clear();
        if (name.isEmpty()) {
            status.setText("Driver name is required.");
            return;
        }

        final String sql = "INSERT INTO Driver (DriverName, DriverTelephoneNumber) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, phone.isEmpty() ? null : phone);
            ps.executeUpdate();
            status.setText("Driver added successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error adding driver: " + ex.getMessage());
        }
    }

    private void loadAllDrivers() {
        ObservableList<Map<String, String>> items = FXCollections.observableArrayList();
        final String sql = "SELECT DriverName, DriverTelephoneNumber FROM Driver ORDER BY DriverName";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> row = new HashMap<>();
                row.put("name",  rs.getString("DriverName"));
                row.put("phone", rs.getString("DriverTelephoneNumber"));
                items.add(row);
            }
            tableDriver.setItems(items);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public VBox getView() {
        return root;
    }
}
