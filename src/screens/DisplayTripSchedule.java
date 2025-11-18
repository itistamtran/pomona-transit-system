package screens;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DisplayTripSchedule {
    private VBox root;

    public DisplayTripSchedule(Stage stage) {
        Label title = new Label("Display Trip Schedule");

        TextField tfStart = new TextField();
        tfStart.setPromptText("Start location");
        TextField tfDest = new TextField();
        tfDest.setPromptText("Destination");
        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");

        HBox inputs = new HBox(10, tfStart, tfDest, tfDate);
        inputs.setAlignment(Pos.CENTER);

        Button btnSearch = new Button("Search");
        Button btnBack = new Button("Back");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(15);

        btnSearch.setOnAction(e ->
                showSchedule(tfStart.getText().trim(), tfDest.getText().trim(), tfDate.getText().trim(), output));

        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        HBox buttons = new HBox(10, btnSearch, btnBack);
        buttons.setAlignment(Pos.CENTER);

        root = new VBox(15, title, inputs, buttons, output);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void showSchedule(String start, String dest, String date, TextArea output) {
        output.clear();
        String sql = """
            SELECT T.TripNumber,
                   O.Date,
                   O.ScheduledStartTime,
                   O.ScheduledArrivalTime,
                   O.DriverName,
                   O.BusID
            FROM Trip T
            JOIN TripOffering O ON T.TripNumber = O.TripNumber
            WHERE T.StartLocationName = ?
              AND T.DestinationName = ?
              AND O.Date = ?
            ORDER BY O.ScheduledStartTime
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, start);
            ps.setString(2, dest);
            ps.setString(3, date);

            ResultSet rs = ps.executeQuery();
            StringBuilder sb = new StringBuilder();
            sb.append("TripNumber | Date       | StartTime | ArrivalTime | Driver          | BusID\n");
            sb.append("----------------------------------------------------------------------------\n");
            boolean found = false;
            while (rs.next()) {
                found = true;
                sb.append(String.format("%10d | %s | %s | %s | %-15s | %d%n",
                        rs.getInt("TripNumber"),
                        rs.getDate("Date").toString(),
                        rs.getTime("ScheduledStartTime").toString(),
                        rs.getTime("ScheduledArrivalTime").toString(),
                        rs.getString("DriverName"),
                        rs.getInt("BusID")));
            }
            if (!found) {
                sb.append("No trip offerings found.");
            }
            output.setText(sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            output.setText("Error while fetching schedule, check console.");
        }
    }

    public VBox getView() {
        return root;
    }
}
