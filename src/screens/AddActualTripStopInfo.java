package screens;

import database.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AddActualTripStopInfo {
    private VBox root;

    public AddActualTripStopInfo(Stage stage) {
        Label title = new Label("Add Actual Trip Stop Info");

        TextField tfTrip = new TextField();
        tfTrip.setPromptText("TripNumber");

        TextField tfDate = new TextField();
        tfDate.setPromptText("Date (YYYY-MM-DD)");

        TextField tfSchedStart = new TextField();
        tfSchedStart.setPromptText("Scheduled Start Time (HH:MM:SS)");

        TextField tfStop = new TextField();
        tfStop.setPromptText("StopNumber");

        TextField tfSchedArr = new TextField();
        tfSchedArr.setPromptText("Scheduled Arrival Time (HH:MM:SS)");

        TextField tfActualStart = new TextField();
        tfActualStart.setPromptText("Actual Start Time (HH:MM:SS)");

        TextField tfActualArr = new TextField();
        tfActualArr.setPromptText("Actual Arrival Time (HH:MM:SS)");

        TextField tfIn = new TextField();
        tfIn.setPromptText("Passengers IN");

        TextField tfOut = new TextField();
        tfOut.setPromptText("Passengers OUT");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        form.add(tfTrip, 0, 0);
        form.add(tfDate, 1, 0);
        form.add(tfSchedStart, 0, 1);
        form.add(tfStop, 1, 1);
        form.add(tfSchedArr, 0, 2);
        form.add(tfActualStart, 1, 2);
        form.add(tfActualArr, 0, 3);
        form.add(tfIn, 1, 3);
        form.add(tfOut, 0, 4);

        Button btnSave = new Button("Save");
        Button btnBack = new Button("Back");

        TextArea status = new TextArea();
        status.setEditable(false);
        status.setPrefRowCount(4);

        btnSave.setOnAction(e -> saveActual(tfTrip.getText().trim(),
                tfDate.getText().trim(),
                tfSchedStart.getText().trim(),
                tfStop.getText().trim(),
                tfSchedArr.getText().trim(),
                tfActualStart.getText().trim(),
                tfActualArr.getText().trim(),
                tfIn.getText().trim(),
                tfOut.getText().trim(),
                status));

        btnBack.setOnAction(e -> stage.getScene().setRoot(new HomePage(stage).getView()));

        root = new VBox(12, title, form, btnSave, btnBack, status);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("page");
        title.getStyleClass().add("title-label");
        btnSave.getStyleClass().add("button");
        btnBack.getStyleClass().add("button");

    }

    private void saveActual(String tripStr, String date, String schedStart,
                            String stopStr, String schedArr, String actualStart,
                            String actualArr, String inStr, String outStr,
                            TextArea status) {
        status.clear();
        try {
            int trip = Integer.parseInt(tripStr);
            int stop = Integer.parseInt(stopStr);
            int in = Integer.parseInt(inStr);
            int out = Integer.parseInt(outStr);

            String sql = """
                INSERT INTO ActualTripStopInfo
                (TripNumber, Date, ScheduledStartTime, StopNumber,
                 ScheduledArrivalTime, ActualStartTime, ActualArrivalTime,
                 NumberOfPassengerIn, NumberOfPassengerOut)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

            try (Connection conn = Database.connect();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, trip);
                ps.setString(2, date);
                ps.setString(3, schedStart);
                ps.setInt(4, stop);
                ps.setString(5, schedArr);
                ps.setString(6, actualStart);
                ps.setString(7, actualArr);
                ps.setInt(8, in);
                ps.setInt(9, out);

                ps.executeUpdate();
                status.setText("Actual trip stop info saved.");

            }
        } catch (NumberFormatException nfe) {
            status.setText("TripNumber, StopNumber, Passengers IN and OUT must be integers.");
        } catch (Exception ex) {
            ex.printStackTrace();
            status.setText("Error saving actual trip stop info, check console.");
        }
    }

    public VBox getView() {
        return root;
    }
}
