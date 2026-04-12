// OOP CONCEPT : Separation of Concerns (GUI)
// ASSIGNMENT  : N/A
// PURPOSE     : Visual hub for citizen operations.

package gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import users.Citizen;
import enums.ComplaintCategory;
import store.DataStore;
import complaints.*;
import java.time.LocalDateTime;
import priority.PriorityCalculator;
import javafx.collections.FXCollections;
import java.util.ArrayList;

public class CitizenDashboard {
    public static Scene getScene(Stage stage, Citizen citizen) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));
        
        // Header
        HBox header = new HBox(30);
        Label welcome = new Label("Welcome, " + citizen.username);
        welcome.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        Button notifications = new Button("🔔 " + (DataStore.getInstance().userNotifications.containsKey(citizen.userId) ? DataStore.getInstance().userNotifications.get(citizen.userId).size() : 0));
        Button logoutBtn = new Button("Logout");
        header.getChildren().addAll(welcome, notifications, logoutBtn);
        root.setTop(header);
        
        // History List
        VBox leftPane = new VBox(10);
        leftPane.setPadding(new Insets(10));
        leftPane.getChildren().add(new Label("Your Complaints History"));
        ListView<String> history = new ListView<>();
        leftPane.getChildren().add(history);
        root.setLeft(leftPane);
        
        // Input Form
        GridPane form = new GridPane();
        form.setVgap(10); form.setHgap(10); form.setPadding(new Insets(20));
        form.add(new Label("File New Complaint"), 0, 0, 2, 1);
        TextField titleFld = new TextField();
        TextArea descFld = new TextArea(); descFld.setPrefRowCount(3);
        TextField targetFld = new TextField(); targetFld.setPromptText("Who/What is this against?");
        TextField areaFld = new TextField(); areaFld.setPromptText("numeric code");
        ChoiceBox<ComplaintCategory> box = new ChoiceBox<>();
        box.getItems().addAll(ComplaintCategory.values());
        
        form.add(new Label("Title:"), 0, 1); form.add(titleFld, 1, 1);
        form.add(new Label("Description:"), 0, 2); form.add(descFld, 1, 2);
        form.add(new Label("Target:"), 0, 3); form.add(targetFld, 1, 3);
        form.add(new Label("Area Code:"), 0, 4); form.add(areaFld, 1, 4);
        form.add(new Label("Category:"), 0, 5); form.add(box, 1, 5);
        Slider s = new Slider(1, 5, 1); s.setShowTickMarks(true);
        form.add(new Label("Urgency:"), 0, 6); form.add(s, 1, 6);
        Button submitBtn = new Button("Submit");
        form.add(submitBtn, 1, 7);
        root.setCenter(form);

        // Load History Method
        Runnable refreshHistory = () -> {
            ArrayList<String> texts = new ArrayList<>();
            DataStore store = DataStore.getInstance();
            ArrayList<BaseComplaint> allComps = new ArrayList<>();
            allComps.addAll(store.infraBox.getAll()); allComps.addAll(store.corruptionBox.getAll());
            allComps.addAll(store.noiseBox.getAll()); allComps.addAll(store.trafficBox.getAll());
            allComps.addAll(store.sanitationBox.getAll()); allComps.addAll(store.waterSupplyBox.getAll());
            allComps.addAll(store.electricityBox.getAll());

            for (BaseComplaint c : allComps) {
                if (c.filedByUserId == citizen.userId) {
                    texts.add("ID: " + c.complaintId + " | " + c.status + " | " + c.title);
                }
            }
            history.setItems(FXCollections.observableArrayList(texts));
        };
        refreshHistory.run();

        // Notification Hook
        threads.NotificationThread.uiCallback = () -> {
            int unread = DataStore.getInstance().userNotifications.containsKey(citizen.userId) ? 
                         DataStore.getInstance().userNotifications.get(citizen.userId).size() : 0;
            notifications.setText("🔔 " + unread);
        };

        // Submit Button Logic
        submitBtn.setOnAction(e -> {
            try {
                int areaCode = Integer.parseInt(areaFld.getText());
                int urgency = (int) s.getValue();
                ComplaintCategory cat = box.getValue();
                
                if (cat == null || titleFld.getText().isEmpty() || targetFld.getText().isEmpty()) {
                    new Alert(Alert.AlertType.ERROR, "Please fill in all blanks.").show();
                    return;
                }

                int cId = (int)(Math.random() * 10000);
                BaseComplaint comp = null;
                switch(cat) {
                    case INFRASTRUCTURE: comp = new InfrastructureComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case CORRUPTION: comp = new CorruptionComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case NOISE: comp = new NoiseComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case TRAFFIC: comp = new TrafficComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case SANITATION: comp = new SanitationComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case WATER_SUPPLY: comp = new WaterSupplyComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                    case ELECTRICITY: comp = new ElectricityComplaint(cId, titleFld.getText(), descFld.getText(), citizen.userId, areaCode, urgency, LocalDateTime.now(), targetFld.getText()); break;
                }

                int score = PriorityCalculator.calculateScore(comp);
                comp.priorityScore = score;
                comp.status = PriorityCalculator.autoAssignStatus(score);

                DataStore store = DataStore.getInstance();
                switch(cat) {
                    case INFRASTRUCTURE: store.infraBox.add((InfrastructureComplaint)comp); break;
                    case CORRUPTION: store.corruptionBox.add((CorruptionComplaint)comp); break;
                    case NOISE: store.noiseBox.add((NoiseComplaint)comp); break;
                    case TRAFFIC: store.trafficBox.add((TrafficComplaint)comp); break;
                    case SANITATION: store.sanitationBox.add((SanitationComplaint)comp); break;
                    case WATER_SUPPLY: store.waterSupplyBox.add((WaterSupplyComplaint)comp); break;
                    case ELECTRICITY: store.electricityBox.add((ElectricityComplaint)comp); break;
                }

                new Alert(Alert.AlertType.INFORMATION, "Complaint Successfully Filed (Score: " + score + ")").show();
                refreshHistory.run();

            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Area code must be numeric!").show();
            } catch (exceptions.DuplicateComplaintException ex) {
                new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
            }
        });

        logoutBtn.setOnAction(e -> stage.setScene(LoginScreen.getScene(stage)));

        return new Scene(root, 900, 650);
    }
}
