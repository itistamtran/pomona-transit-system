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
import java.sql.ResultSet;

public class DisplayStops {
    private VBox root;

    public DisplayStops(Stage stage) {
        Label title = new Label("Display Stops for a Trip");

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");

        Button btnShow = new Button("Show stops");
        Button btnBack = new Button("Back");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(12);

        btnShow.setOnAction(e -> displayStops(tfTrip.getText().trim(), output));
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, tfTrip, btnShow, btnBack, output);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void displayStops(String tripStr, TextArea output) {
        output.clear();
        try {
            int trip = Integer.parseInt(tripStr);

            String sql = """
                SELECT S.StopNumber, S.StopAddress,
                       TSI.SequenceNumber, TSI.DrivingTime
                FROM TripStopInfo TSI
                JOIN Stop S ON TSI.StopNumber = S.StopNumber
                WHERE TSI.TripNumber = ?
                ORDER BY TSI.SequenceNumber
                """;

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, trip);
                ResultSet rs = ps.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("Seq | StopNumber | StopAddress                  | DrivingTime\n");
                sb.append("----------------------------------------------------------------\n");

                boolean found = false;
                while (rs.next()) {
                    found = true;
                    sb.append(String.format("%3d | %10d | %-28s | %d%n",
                            rs.getInt("SequenceNumber"),
                            rs.getInt("StopNumber"),
                            rs.getString("StopAddress"),
                            rs.getInt("DrivingTime")));
                }
                if (!found) {
                    sb.append("No stops found for that trip.");
                }
                output.setText(sb.toString());
            }
        } catch (NumberFormatException nfe) {
            output.setText("TripNumber must be an integer.");
        } catch (Exception ex) {
            ex.printStackTrace();
            output.setText("Error displaying stops, check console.");
        }
    }

    public VBox getView() {
        return root;
    }
}
