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

public class DeleteBus {
    private VBox root;

    public DeleteBus(Stage stage) {
        Label title = new Label("Delete a Bus");

        TextField tfBusID = new TextField();
        tfBusID.setPromptText("BusID to delete");

        Button btnDelete = new Button("Delete");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        btnDelete.setOnAction(e -> deleteBus(tfBusID.getText().trim(), status));
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, tfBusID, btnDelete, btnBack, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void deleteBus(String busIDStr, TextArea status) {
        status.clear();
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

    public VBox getView() {
        return root;
    }
}
