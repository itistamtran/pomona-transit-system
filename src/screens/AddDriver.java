package screens;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddDriver {
    private VBox root;

    public AddDriver(Stage stage) {
        Label title = new Label("Add a Driver");

        TextField tfName = new TextField();
        tfName.setPromptText("Driver name");

        TextField tfPhone = new TextField();
        tfPhone.setPromptText("Telephone number");

        Button btnAdd = new Button("Add driver");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        btnAdd.setOnAction(e -> addDriver(tfName.getText().trim(), tfPhone.getText().trim(), status));
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, tfName, tfPhone, btnAdd, btnBack, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void addDriver(String name, String phone, TextArea status) {
        status.clear();
        if (name.isEmpty()) {
            status.setText("Driver name is required.");
            return;
        }

        String sql = "INSERT INTO Driver (DriverName, DriverTelephoneNumber) VALUES (?, ?)";

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, phone.isEmpty() ? null : phone);
            ps.executeUpdate();
            status.setText("Driver added successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error adding driver, maybe this name already exists.");
        }
    }

    public VBox getView() {
        return root;
    }
}
