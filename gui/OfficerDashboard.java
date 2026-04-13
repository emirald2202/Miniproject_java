






package gui;

import complaints.BaseComplaint;
import enums.ComplaintCategory;
import enums.Status;
import exceptions.ComplaintExpiredException;
import exceptions.InvalidStatusTransitionException;
import search.ComplaintSearch;
import store.DataStore;
import threads.SessionTimeoutThread;
import users.Admin;
import users.Officer;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class OfficerDashboard {

    private final Stage stage;
    private final Officer officer;
    private final SessionTimeoutThread sessionThread;
    private final DataStore store = DataStore.getInstance();

    private Button bellButton;
    private TableView<BaseComplaint> complaintTable;
    private ObservableList<BaseComplaint> tableData;

    
    private Label detailTitleLabel;
    private Label detailDescLabel;
    private Label detailDateLabel;
    private ChoiceBox<Status> statusChoiceBox;
    private BaseComplaint selectedComplaint;

    public OfficerDashboard(Stage stage, Officer officer, SessionTimeoutThread sessionThread) {
        this.stage         = stage;
        this.officer       = officer;
        this.sessionThread = sessionThread;
    }

    
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCenterPanel());
        root.setRight(buildDetailPanel());

        store.notificationThread.registerCallback(officer.userId,
            () -> Platform.runLater(() -> { updateBellCount(); loadAllComplaints(); }));

        loadAllComplaints();
        return new Scene(root, 1000, 650);
    }

    
    private HBox buildTopBar() {
        Label titleLabel = new Label("Officer Dashboard  —  " + officer.username
                                     + "  [" + officer.department + "]");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);

        bellButton = new Button("🔔 0");
        bellButton.setOnAction(e -> {
            sessionThread.resetTimer();
            showNotificationsPopup();
        });
        bellButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> handleLogout());
        logoutButton.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white;");

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topBar = new HBox(10, titleLabel, spacer, bellButton, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 15, 10, 15));
        topBar.setStyle("-fx-background-color: #2c3e50;");
        return topBar;
    }

    
    private VBox buildCenterPanel() {
        VBox centerPanel = new VBox(10, buildSearchBar(), buildTable());
        centerPanel.setPadding(new Insets(10));
        return centerPanel;
    }

    
    private HBox buildSearchBar() {
        ChoiceBox<String> searchTypeBox = new ChoiceBox<>();
        searchTypeBox.getItems().addAll("Search by ID", "Search by Name", "Search by Category");
        searchTypeBox.setValue("Search by ID");

        TextField searchInputField = new TextField();
        searchInputField.setPromptText("Enter search value...");
        searchInputField.setPrefWidth(200);

        Button searchButton = new Button("Search");
        searchButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");

        Button resetButton = new Button("Show All");
        resetButton.setOnAction(e -> {
            sessionThread.resetTimer();
            loadAllComplaints();
        });

        
        searchButton.setOnAction(e -> {
            sessionThread.resetTimer();
            String input = searchInputField.getText().trim();
            if (input.isEmpty()) return;

            ComplaintSearch searcher = new ComplaintSearch();
            List<BaseComplaint> results;

            switch (searchTypeBox.getValue()) {
                case "Search by ID" -> {
                    try {
                        results = searcher.search(Integer.parseInt(input));
                    } catch (NumberFormatException numEx) {
                        showErrorAlert("Please enter a valid numeric ID.");
                        return;
                    }
                }
                case "Search by Name"     -> results = searcher.search(input);
                case "Search by Category" -> {
                    try {
                        results = searcher.search(ComplaintCategory.valueOf(input.toUpperCase()));
                    } catch (IllegalArgumentException argEx) {
                        showErrorAlert("Invalid category. Use: INFRASTRUCTURE, CORRUPTION, NOISE, TRAFFIC, SANITATION, WATER_SUPPLY, ELECTRICITY");
                        return;
                    }
                }
                default -> results = new ArrayList<>();
            }

            tableData.setAll(results);
        });

        HBox searchBar = new HBox(8, searchTypeBox, searchInputField, searchButton, resetButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        return searchBar;
    }

    
    private TableView<BaseComplaint> buildTable() {
        complaintTable = new TableView<>();
        tableData      = FXCollections.observableArrayList();
        complaintTable.setItems(tableData);
        complaintTable.setPlaceholder(new Label("No complaints found."));

        TableColumn<BaseComplaint, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().complaintId).asObject());
        idCol.setPrefWidth(50);

        TableColumn<BaseComplaint, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().title));
        titleCol.setPrefWidth(220);

        TableColumn<BaseComplaint, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(row -> new SimpleStringProperty(
            row.getValue().getClass().getSimpleName().replace("Complaint", "")));
        categoryCol.setPrefWidth(110);

        TableColumn<BaseComplaint, Integer> scoreCol = new TableColumn<>("Priority Score");
        scoreCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().priorityScore).asObject());
        scoreCol.setPrefWidth(110);

        TableColumn<BaseComplaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().status.toString()));
        statusCol.setPrefWidth(110);

        complaintTable.getColumns().addAll(idCol, titleCol, categoryCol, scoreCol, statusCol);

        
        complaintTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldComplaint, newComplaint) -> {
                if (newComplaint != null) {
                    sessionThread.resetTimer();
                    populateDetailPanel(newComplaint);
                }
            });

        return complaintTable;
    }

    
    private VBox buildDetailPanel() {
        Label heading = new Label("Complaint Details");
        heading.setFont(Font.font("System", FontWeight.BOLD, 14));

        detailTitleLabel = new Label("—");
        detailTitleLabel.setWrapText(true);
        detailTitleLabel.setMaxWidth(200);

        detailDescLabel  = new Label("—");
        detailDescLabel.setWrapText(true);
        detailDescLabel.setMaxWidth(200);

        detailDateLabel  = new Label("—");

        
        
        Label citizenIdLabel = new Label("Citizen #—");
        Label protectedLabel = new Label("PROTECTED — Unauthorized Access");
        protectedLabel.setTextFill(Color.RED);
        protectedLabel.setFont(Font.font("System", FontWeight.BOLD, 11));

        statusChoiceBox = new ChoiceBox<>();
        statusChoiceBox.getItems().addAll(Status.UNDER_REVIEW, Status.RESOLVED, Status.REJECTED);
        statusChoiceBox.setValue(Status.UNDER_REVIEW);

        Button updateButton = new Button("Update Status");
        updateButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        updateButton.setOnAction(e -> {
            sessionThread.resetTimer();
            handleStatusUpdate();
        });

        VBox detailPanel = new VBox(10,
            heading,
            new Separator(),
            new Label("Title:"),     detailTitleLabel,
            new Label("Description:"), detailDescLabel,
            new Label("Filed:"),     detailDateLabel,
            new Label("Complainant:"), citizenIdLabel,
            protectedLabel,
            new Separator(),
            new Label("Update Status:"),
            statusChoiceBox,
            updateButton
        );
        detailPanel.setPadding(new Insets(15));
        detailPanel.setPrefWidth(220);
        detailPanel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 0 1;");

        return detailPanel;
    }

    
    private void populateDetailPanel(BaseComplaint complaint) {
        selectedComplaint = complaint;
        detailTitleLabel.setText(complaint.title);
        detailDescLabel.setText(complaint.description);
        detailDateLabel.setText(complaint.filedDate.toLocalDate().toString());
        
        detailTitleLabel.getParent().getChildrenUnmodifiable().stream()
            .filter(node -> node instanceof Label && ((Label) node).getText().startsWith("Citizen #"))
            .findFirst()
            .ifPresent(node -> ((Label) node).setText("Citizen #" + complaint.filedByUserId));
    }

    
    private void handleStatusUpdate() {
        if (selectedComplaint == null) {
            showErrorAlert("Please select a complaint from the table first.");
            return;
        }

        Status chosenStatus = statusChoiceBox.getValue();
        try {
            selectedComplaint.updateStatus(chosenStatus);

            
            String citizenMsg = buildNotificationMessage(selectedComplaint, chosenStatus);
            store.notificationQueue.offer(
                "USERID:" + selectedComplaint.filedByUserId + "|MSG:" + citizenMsg);

            
            String adminMsg = "Complaint #" + selectedComplaint.complaintId
                + " \"" + selectedComplaint.title + "\" updated to " + chosenStatus
                + " by officer " + officer.username + ".";
            for (Admin admin : store.admins) {
                store.notificationQueue.offer("USERID:" + admin.userId + "|MSG:" + adminMsg);
            }

            
            int assignedId = selectedComplaint.assignedToOfficerId;
            if (assignedId != -1 && assignedId != officer.userId) {
                store.notificationQueue.offer("USERID:" + assignedId + "|MSG:Complaint #"
                    + selectedComplaint.complaintId + " \"" + selectedComplaint.title
                    + "\" you are assigned to has been updated to " + chosenStatus + ".");
            }

            loadAllComplaints();
            System.out.println("[Officer] Updated complaint #" + selectedComplaint.complaintId
                               + " → " + chosenStatus);

        } catch (ComplaintExpiredException expiredException) {
            showErrorAlert(expiredException.getMessage());
        } catch (InvalidStatusTransitionException transitionException) {
            showErrorAlert(transitionException.getMessage());
        }
    }

    
    private String buildNotificationMessage(BaseComplaint complaint, Status newStatus) {
        return switch (newStatus) {
            case RESOLVED     -> "Your complaint \"" + complaint.title + "\" has been resolved. Thank you for reporting.";
            case REJECTED     -> "Your complaint \"" + complaint.title + "\" has been reviewed and closed.";
            case UNDER_REVIEW -> "An officer has started reviewing your complaint \"" + complaint.title + "\".";
            default           -> "Your complaint \"" + complaint.title + "\" status updated to " + newStatus + ".";
        };
    }

    
    private void loadAllComplaints() {
        List<BaseComplaint> all = new ArrayList<>();
        all.addAll(store.infraBox.getAllComplaints());
        all.addAll(store.corruptionBox.getAllComplaints());
        all.addAll(store.noiseBox.getAllComplaints());
        all.addAll(store.trafficBox.getAllComplaints());
        all.addAll(store.sanitationBox.getAllComplaints());
        all.addAll(store.waterSupplyBox.getAllComplaints());
        all.addAll(store.electricityBox.getAllComplaints());
        tableData.setAll(all);
    }

    
    private void updateBellCount() {
        int count = store.userNotifications.getOrDefault(officer.userId, List.of()).size();
        bellButton.setText("🔔 " + count);
    }

    
    private void showNotificationsPopup() {
        List<String> messages = store.userNotifications.getOrDefault(officer.userId, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Your Notifications (" + messages.size() + ")");
        alert.setContentText(messages.isEmpty() ? "No notifications yet." : String.join("\n\n", messages));
        alert.showAndWait();
    }

    
    private void handleLogout() {
        store.notificationThread.deregisterCallback(officer.userId);
        sessionThread.stopThread();
        stage.setScene(new LoginScreen(stage).buildScene());
    }

    
    private void showErrorAlert(String message) {
        System.err.println("[OFFICER ERROR] " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
