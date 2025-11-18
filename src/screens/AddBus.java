package screens;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddBus {
    private VBox root;

    public AddBus(Stage stage) {
        Label title = new Label("Add a Bus");

        TextField tfBusID = new TextField();
        tfBusID.setPromptText("BusID (integer)");

        TextField tfModel = new TextField();
        tfModel.setPromptText("Model");

        TextField tfYear = new TextField();
        tfYear.setPromptText("Year (integer)");

        Button btnAdd = new Button("Add bus");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        btnAdd.setOnAction(e -> addBus(tfBusID.getText().trim(), tfModel.getText().trim(), tfYear.getText().trim(), status));
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, tfBusID, tfModel, tfYear, btnAdd, btnBack, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void addBus(String busIDStr, String model, String yearStr, TextArea status) {
        status.clear();
        try {
            int busID = Integer.parseInt(busIDStr);
            int year = Integer.parseInt(yearStr);

            String sql = "INSERT INTO Bus (BusID, Model, Year) VALUES (?, ?, ?)";

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, busID);
                ps.setString(2, model);
                ps.setInt(3, year);
                ps.executeUpdate();
                status.setText("Bus added successfully.");
            }

        } catch (NumberFormatException nfe) {
            status.setText("BusID and Year must be integers.");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error adding bus, check console.");
        }
    }

    public VBox getView() {
        return root;
    }
}
