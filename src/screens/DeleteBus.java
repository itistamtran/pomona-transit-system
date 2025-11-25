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

public class DeleteBus {
    private VBox root;

    private final TableView<Map<String, String>> tableBus = new TableView<>();
    private final TableColumn<Map<String, String>, String> colBusId =
            new TableColumn<>("BusID");
    private final TableColumn<Map<String, String>, String> colModel =
            new TableColumn<>("Model");
    private final TableColumn<Map<String, String>, String> colYear =
            new TableColumn<>("Year");

    public DeleteBus(Stage stage) {
        Label title = new Label("Delete a Bus");
        title.getStyleClass().add("screen-title");

        Text lblPrompt = new Text("Enter BusID to remove a bus:");
        TextField tfBusID = new TextField();
        tfBusID.setPromptText("BusID to delete");

        Button btnDelete = new Button("Delete");
        btnDelete.getStyleClass().add("primary-btn");
        Button btnBack = new Button("Back");

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

        // Layout
        HBox formRow = new HBox(10, tfBusID);
        HBox.setHgrow(tfBusID, Priority.ALWAYS);

        HBox buttonsRow = new HBox(10, btnDelete, btnBack);
        buttonsRow.setAlignment(Pos.CENTER);

        root = new VBox(12, title, lblPrompt, formRow, buttonsRow, tableBus, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");

        // Actions
        btnDelete.setOnAction(e -> {
            deleteBus(tfBusID.getText().trim(), status);
            loadAllBuses();      // refresh after delete
            tfBusID.clear();
        });
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        // Initial data load
        loadAllBuses();
    }

    private void deleteBus(String busIDStr, TextArea status) {
        status.clear();
        if (busIDStr.isEmpty()) {
            status.setText("BusID must be an integer.");
            return;
        }
        try {
            int busID = Integer.parseInt(busIDStr);
            String sql = "DELETE FROM Bus WHERE BusID = ?";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, busID);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    status.setText("Bus deleted successfully.");
                } else {
                    status.setText("No bus found with that ID.");
                }
            }
        } catch (NumberFormatException nfe) {
            status.setText("BusID must be an integer.");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error deleting bus, maybe it is referenced by a TripOffering.");
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
