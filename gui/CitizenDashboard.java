



package gui;

import complaints.*;
import containers.ComplaintBox;
import enums.ComplaintCategory;
import enums.Status;
import exceptions.DuplicateComplaintException;
import priority.PriorityCalculator;
import store.DataStore;
import threads.SessionTimeoutThread;
import users.Admin;
import users.Citizen;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CitizenDashboard {

    private final Stage stage;
    private final Citizen citizen;
    private final SessionTimeoutThread sessionThread;
    private final DataStore store = DataStore.getInstance();

    private Button      bellButton;
    private ListView<String> complaintListView;
    private List<BaseComplaint> ownComplaints = new ArrayList<>();

    public CitizenDashboard(Stage stage, Citizen citizen, SessionTimeoutThread sessionThread) {
        this.stage         = stage;
        this.citizen       = citizen;
        this.sessionThread = sessionThread;
    }

    
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setLeft(buildComplaintList());
        root.setCenter(buildFilingForm());

        
        store.notificationThread.registerCallback(citizen.userId,
            () -> Platform.runLater(() -> { updateBellCount(); refreshComplaintList(); }));

        refreshComplaintList();
        return new Scene(root, 900, 650);
    }

    
    private HBox buildTopBar() {
        Label titleLabel = new Label("Civilian Complaint Portal  —  Welcome, " + citizen.username);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        bellButton = new Button("🔔 0");
        bellButton.setOnAction(e -> {
            sessionThread.resetTimer();
            showNotificationsPopup();
        });

        HBox topBar = new HBox(titleLabel, new Pane(), bellButton);
        HBox.setHgrow(topBar.getChildren().get(1), Priority.ALWAYS);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        titleLabel.setTextFill(Color.WHITE);
        bellButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());
        logoutButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");
        topBar.getChildren().add(logoutButton);
        return topBar;
    }

    
    private VBox buildComplaintList() {
        Label heading = new Label("My Complaints");
        heading.setFont(Font.font("System", FontWeight.BOLD, 14));
        heading.setPadding(new Insets(10, 10, 5, 10));

        complaintListView = new ListView<>();
        complaintListView.setPrefWidth(240);

        
        complaintListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if      (item.startsWith("[FILED]"))        setStyle("-fx-text-fill: #2980b9;");
                    else if (item.startsWith("[UNDER_REVIEW]")) setStyle("-fx-text-fill: #e67e22;");
                    else if (item.startsWith("[RESOLVED]"))     setStyle("-fx-text-fill: #27ae60;");
                    else if (item.startsWith("[ESCALATED]"))    setStyle("-fx-text-fill: #e74c3c;");
                    else if (item.startsWith("[REJECTED]"))     setStyle("-fx-text-fill: #95a5a6;");
                    else setStyle("");
                }
            }
        });

        
        complaintListView.getSelectionModel().selectedIndexProperty().addListener(
            (obs, oldIdx, newIdx) -> {
                int idx = newIdx.intValue();
                if (idx >= 0 && idx < ownComplaints.size()) {
                    sessionThread.resetTimer();
                    showComplaintDetailPopup(ownComplaints.get(idx));
                }
            });

        VBox leftPanel = new VBox(heading, complaintListView);
        leftPanel.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 0 1 0 0;");
        return leftPanel;
    }

    
    private ScrollPane buildFilingForm() {
        Label formHeading = new Label("File a New Complaint");
        formHeading.setFont(Font.font("System", FontWeight.BOLD, 16));

        TextField titleField        = new TextField();
        titleField.setPromptText("Brief title of the issue");

        TextArea descriptionArea    = new TextArea();
        descriptionArea.setPromptText("Describe the issue in detail");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setWrapText(true);

        ChoiceBox<ComplaintCategory> categoryChoiceBox = new ChoiceBox<>();
        categoryChoiceBox.getItems().addAll(ComplaintCategory.values());
        categoryChoiceBox.setValue(ComplaintCategory.INFRASTRUCTURE);

        TextField areaCodeField     = new TextField();
        areaCodeField.setPromptText("Numeric area code (e.g. 101)");
        
        areaCodeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) areaCodeField.setText(oldVal);
        });

        Slider urgencySlider = new Slider(1, 5, 1);
        urgencySlider.setMajorTickUnit(1);
        urgencySlider.setMinorTickCount(0);
        urgencySlider.setSnapToTicks(true);
        urgencySlider.setShowTickMarks(true);
        urgencySlider.setShowTickLabels(true);

        Label priorityScoreLabel = new Label("Priority Score: —");
        priorityScoreLabel.setFont(Font.font("System", FontWeight.BOLD, 13));

        Button submitButton = new Button("Submit Complaint");
        submitButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setPrefWidth(180);

        
        urgencySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            sessionThread.resetTimer();
            int areaCode = areaCodeField.getText().isEmpty() ? 0 : Integer.parseInt(areaCodeField.getText());
            int typeInt  = categoryToInt(categoryChoiceBox.getValue());
            int score    = PriorityCalculator.calculateScore(typeInt, newVal.intValue(), areaCode);
            priorityScoreLabel.setText("Priority Score: " + score);
        });

        
        categoryChoiceBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            sessionThread.resetTimer();
            int areaCode = areaCodeField.getText().isEmpty() ? 0 : Integer.parseInt(areaCodeField.getText());
            int typeInt  = categoryToInt(newVal);
            int score    = PriorityCalculator.calculateScore(typeInt, (int) urgencySlider.getValue(), areaCode);
            priorityScoreLabel.setText("Priority Score: " + score);
        });

        submitButton.setOnAction(e -> {
            sessionThread.resetTimer();
            handleSubmit(titleField, descriptionArea, categoryChoiceBox, areaCodeField, urgencySlider, priorityScoreLabel);
        });

        
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(20));
        form.add(formHeading,                      0, 0, 2, 1);
        form.add(new Label("Title:"),              0, 1);  form.add(titleField,          1, 1);
        form.add(new Label("Description:"),        0, 2);  form.add(descriptionArea,     1, 2);
        form.add(new Label("Category:"),           0, 3);  form.add(categoryChoiceBox,   1, 3);
        form.add(new Label("Area Code:"),          0, 4);  form.add(areaCodeField,       1, 4);
        form.add(new Label("Urgency (1–5):"),      0, 5);  form.add(urgencySlider,       1, 5);
        form.add(new Label(""),                    0, 6);  form.add(priorityScoreLabel,  1, 6);
        form.add(submitButton,                     1, 7);

        ColumnConstraints col1 = new ColumnConstraints(130);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(col1, col2);

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        return scrollPane;
    }

    
    private void handleSubmit(TextField titleField, TextArea descArea,
                               ChoiceBox<ComplaintCategory> categoryBox,
                               TextField areaCodeField, Slider urgencySlider,
                               Label priorityScoreLabel) {
        String title       = titleField.getText().trim();
        String description = descArea.getText().trim();
        ComplaintCategory category = categoryBox.getValue();

        if (title.isEmpty() || description.isEmpty() || areaCodeField.getText().isEmpty()) {
            showErrorAlert("Please fill in all fields before submitting.");
            return;
        }

        int areaCode     = Integer.parseInt(areaCodeField.getText());
        int urgencyLevel = (int) urgencySlider.getValue();
        int typeInt      = categoryToInt(category);
        int score        = PriorityCalculator.calculateScore(typeInt, urgencyLevel, areaCode);
        int newId        = generateNextComplaintId();

        BaseComplaint newComplaint = createComplaint(newId, title, description, areaCode, urgencyLevel, category);
        newComplaint.priorityScore = score;
        
        newComplaint.status = Status.FILED;

        try {
            addToCorrectBox(newComplaint, category);

            
            for (Admin admin : store.admins) {
                store.notificationQueue.offer("USERID:" + admin.userId
                    + "|MSG:New complaint filed by " + citizen.username
                    + ": \"" + newComplaint.title + "\"");
            }

            refreshComplaintList();
            clearForm(titleField, descArea, areaCodeField, urgencySlider, priorityScoreLabel);
            showSuccessAlert("Complaint filed successfully!\nStatus: FILED\nPriority Score: " + score);
        } catch (DuplicateComplaintException duplicateException) {
            showErrorAlert(duplicateException.getMessage());
        }
    }

    
    private BaseComplaint createComplaint(int id, String title, String desc, int areaCode, int urgency, ComplaintCategory category) {
        LocalDateTime now = LocalDateTime.now();
        return switch (category) {
            case INFRASTRUCTURE -> new InfrastructureComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case CORRUPTION     -> new CorruptionComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case NOISE          -> new NoiseComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case TRAFFIC        -> new TrafficComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case SANITATION     -> new SanitationComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case WATER_SUPPLY   -> new WaterSupplyComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
            case ELECTRICITY    -> new ElectricityComplaint(id, title, desc, citizen.userId, areaCode, urgency, now);
        };
    }

    
    @SuppressWarnings("unchecked")
    private void addToCorrectBox(BaseComplaint complaint, ComplaintCategory category)
            throws DuplicateComplaintException {
        switch (category) {
            case INFRASTRUCTURE -> store.infraBox.addComplaint((InfrastructureComplaint) complaint);
            case CORRUPTION     -> store.corruptionBox.addComplaint((CorruptionComplaint) complaint);
            case NOISE          -> store.noiseBox.addComplaint((NoiseComplaint) complaint);
            case TRAFFIC        -> store.trafficBox.addComplaint((TrafficComplaint) complaint);
            case SANITATION     -> store.sanitationBox.addComplaint((SanitationComplaint) complaint);
            case WATER_SUPPLY   -> store.waterSupplyBox.addComplaint((WaterSupplyComplaint) complaint);
            case ELECTRICITY    -> store.electricityBox.addComplaint((ElectricityComplaint) complaint);
        }
    }

    
    private void refreshComplaintList() {
        ownComplaints = getAllOwnComplaints();
        List<String> items = new ArrayList<>();
        for (BaseComplaint c : ownComplaints) {
            items.add("[" + c.status + "] " + c.title);
        }
        
        
        complaintListView.getItems().setAll(items);
    }

    
    private List<BaseComplaint> getAllOwnComplaints() {
        List<BaseComplaint> own = new ArrayList<>();
        List<BaseComplaint> all = new ArrayList<>();
        all.addAll(store.infraBox.getAllComplaints());
        all.addAll(store.corruptionBox.getAllComplaints());
        all.addAll(store.noiseBox.getAllComplaints());
        all.addAll(store.trafficBox.getAllComplaints());
        all.addAll(store.sanitationBox.getAllComplaints());
        all.addAll(store.waterSupplyBox.getAllComplaints());
        all.addAll(store.electricityBox.getAllComplaints());
        for (BaseComplaint c : all) {
            if (c.filedByUserId == citizen.userId) own.add(c);
        }
        return own;
    }

    
    private void updateBellCount() {
        int count = store.userNotifications.getOrDefault(citizen.userId, List.of()).size();
        bellButton.setText("🔔 " + count);
    }

    
    private void showNotificationsPopup() {
        List<String> messages = store.userNotifications.getOrDefault(citizen.userId, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Your Notifications (" + messages.size() + ")");
        alert.setContentText(messages.isEmpty() ? "No notifications yet." : String.join("\n\n", messages));
        alert.showAndWait();
    }

    
    private int categoryToInt(ComplaintCategory category) {
        return switch (category) {
            case INFRASTRUCTURE -> PriorityCalculator.TYPE_INFRASTRUCTURE;
            case CORRUPTION     -> PriorityCalculator.TYPE_CORRUPTION;
            case NOISE          -> PriorityCalculator.TYPE_NOISE;
            case TRAFFIC        -> PriorityCalculator.TYPE_TRAFFIC;
            case SANITATION     -> PriorityCalculator.TYPE_SANITATION;
            case WATER_SUPPLY   -> PriorityCalculator.TYPE_WATER_SUPPLY;
            case ELECTRICITY    -> PriorityCalculator.TYPE_ELECTRICITY;
        };
    }

    
    private int generateNextComplaintId() {
        int maxId = 0;
        List<BaseComplaint> all = new ArrayList<>();
        all.addAll(store.infraBox.getAllComplaints());
        all.addAll(store.corruptionBox.getAllComplaints());
        all.addAll(store.noiseBox.getAllComplaints());
        all.addAll(store.trafficBox.getAllComplaints());
        all.addAll(store.sanitationBox.getAllComplaints());
        all.addAll(store.waterSupplyBox.getAllComplaints());
        all.addAll(store.electricityBox.getAllComplaints());
        for (BaseComplaint c : all) {
            if (c.complaintId > maxId) maxId = c.complaintId;
        }
        return maxId + 1;
    }

    
    private void showComplaintDetailPopup(BaseComplaint complaint) {
        Stage modal = new Stage();
        modal.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        modal.setTitle("Complaint Details — #" + complaint.complaintId);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(20));

        String category = complaint.getClass().getSimpleName().replace("Complaint", "");
        String[][] rows = {
            {"ID",             String.valueOf(complaint.complaintId)},
            {"Title",          complaint.title},
            {"Description",    complaint.description},
            {"Category",       category},
            {"Status",         complaint.status.toString()},
            {"Priority Score", String.valueOf(complaint.priorityScore)},
            {"Filed",          complaint.filedDate.toLocalDate().toString()},
        };

        for (int i = 0; i < rows.length; i++) {
            Label key = new Label(rows[i][0] + ":");
            key.setFont(Font.font("System", FontWeight.BOLD, 12));
            Label val = new Label(rows[i][1]);
            val.setWrapText(true);
            val.setMaxWidth(280);
            grid.add(key, 0, i);
            grid.add(val, 1, i);
        }

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> modal.close());
        GridPane.setHalignment(closeButton, javafx.geometry.HPos.RIGHT);
        grid.add(closeButton, 1, rows.length);

        modal.setScene(new Scene(grid, 450, 300));
        modal.showAndWait();
        complaintListView.getSelectionModel().clearSelection();
    }

    
    private void clearForm(TextField titleField, TextArea descArea,
                            TextField areaCodeField, Slider urgencySlider,
                            Label priorityScoreLabel) {
        titleField.clear();
        descArea.clear();
        areaCodeField.clear();
        urgencySlider.setValue(1);
        priorityScoreLabel.setText("Priority Score: —");
    }

    
    private void handleLogout() {
        store.notificationThread.deregisterCallback(citizen.userId);
        sessionThread.stopThread();
        stage.setScene(new LoginScreen(stage).buildScene());
    }

    
    private void showErrorAlert(String message) {
        System.err.println("[CITIZEN ERROR] " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    private void showSuccessAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
