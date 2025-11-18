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

public class WeeklyScheduleOfDriver {
    private VBox root;

    public WeeklyScheduleOfDriver(Stage stage) {
        Label title = new Label("Weekly Schedule of a Driver");

        TextField tfDriver = new TextField();
        tfDriver.setPromptText("Driver name");

        TextField tfDate = new TextField();
        tfDate.setPromptText("Start date (YYYY-MM-DD)");

        Button btnShow = new Button("Show weekly schedule");
        Button btnBack = new Button("Back");

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefRowCount(12);

        btnShow.setOnAction(e -> displayWeekly(tfDriver.getText().trim(), tfDate.getText().trim(), output));
        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, tfDriver, tfDate, btnShow, btnBack, output);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        // load CSS
        root.getStylesheets().add("styles.css");
        root.getStyleClass().add("page");
    }

    private void displayWeekly(String driverName, String startDate, TextArea output) {
        output.clear();
        String sql = """
            SELECT TripNumber, Date, ScheduledStartTime, ScheduledArrivalTime, BusID
            FROM TripOffering
            WHERE DriverName = ?
              AND Date BETWEEN ? AND DATE_ADD(?, INTERVAL 6 DAY)
            ORDER BY Date, ScheduledStartTime
            """;

        try (Connection conn = Database.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, driverName);
            ps.setString(2, startDate);
            ps.setString(3, startDate);

            ResultSet rs = ps.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("Date       | Trip | StartTime | ArrivalTime | BusID\n");
            sb.append("----------------------------------------------------\n");

            boolean found = false;
            while (rs.next()) {
                found = true;
                sb.append(String.format("%s | %4d | %s | %s | %d%n",
                        rs.getDate("Date").toString(),
                        rs.getInt("TripNumber"),
                        rs.getTime("ScheduledStartTime").toString(),
                        rs.getTime("ScheduledArrivalTime").toString(),
                        rs.getInt("BusID")));
            }
            if (!found) {
                sb.append("No trips found for that driver in the given week.");
            }
            output.setText(sb.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
            output.setText("Error fetching weekly schedule, check console.");
        }
    }

    public VBox getView() {
        return root;
    }
}
