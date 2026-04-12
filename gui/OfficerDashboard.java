// OOP CONCEPT : Encapsulation & Data Hiding
// ASSIGNMENT  : 2
// PURPOSE     : Officer dashboard — NO CitizenProfile import; citizen data is permanently blocked.

// ENCAPSULATION PROOF: Search this entire file for "CitizenProfile" — it does not appear.
// The red PROTECTED label below is the only thing an officer ever sees about a complainant's identity.

package gui;

import complaints.BaseComplaint;
import enums.ComplaintCategory;
import enums.Status;
import exceptions.ComplaintExpiredException;
import exceptions.InvalidStatusTransitionException;
import search.ComplaintSearch;
import store.DataStore;
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
    private final DataStore store = DataStore.getInstance();

    private Button bellButton;
    private TableView<BaseComplaint> complaintTable;
    private ObservableList<BaseComplaint> tableData;

    // Detail panel labels — populated on row selection
    private Label detailTitleLabel;
    private Label detailDescLabel;
    private Label detailDateLabel;
    private ChoiceBox<Status> statusChoiceBox;
    private BaseComplaint selectedComplaint;

    public OfficerDashboard(Stage stage, Officer officer) {
        this.stage   = stage;
        this.officer = officer;
    }

    // Builds and returns the full officer dashboard scene
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCenterPanel());
        root.setRight(buildDetailPanel());

        store.notificationThread.registerCallback(officer.userId,
            () -> Platform.runLater(this::updateBellCount));

        loadAllComplaints();
        return new Scene(root, 1000, 650);
    }

    // Builds the top bar with officer name and notification bell
    private HBox buildTopBar() {
        Label titleLabel = new Label("Officer Dashboard  —  " + officer.username
                                     + "  [" + officer.department + "]");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.WHITE);

        bellButton = new Button("🔔 0");
        bellButton.setOnAction(e -> {
            store.sessionTimeoutThread.resetTimer();
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

    // Builds the center panel — search bar above the complaints table
    private VBox buildCenterPanel() {
        VBox centerPanel = new VBox(10, buildSearchBar(), buildTable());
        centerPanel.setPadding(new Insets(10));
        return centerPanel;
    }

    // Builds the search bar with type dropdown, input field, and search button
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
            store.sessionTimeoutThread.resetTimer();
            loadAllComplaints();
        });

        // Delegates to the correct ComplaintSearch overload based on dropdown selection
        searchButton.setOnAction(e -> {
            store.sessionTimeoutThread.resetTimer();
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

    // Builds the complaints TableView — NO citizen name or contact columns
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

        // Populate detail panel on row selection
        complaintTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldComplaint, newComplaint) -> {
                if (newComplaint != null) {
                    store.sessionTimeoutThread.resetTimer();
                    populateDetailPanel(newComplaint);
                }
            });

        return complaintTable;
    }

    // Builds the right-side detail panel — strictly no citizen identity data exposed
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

        // ENCAPSULATION DEMO: The label below always shows PROTECTED — officer can never see citizen details.
        // There is no CitizenProfile import in this file. No code path leads to that data.
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
            store.sessionTimeoutThread.resetTimer();
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

    // Fills the detail panel fields with the selected complaint's data
    private void populateDetailPanel(BaseComplaint complaint) {
        selectedComplaint = complaint;
        detailTitleLabel.setText(complaint.title);
        detailDescLabel.setText(complaint.description);
        detailDateLabel.setText(complaint.filedDate.toLocalDate().toString());
        // Show only the numeric ID — never a name, phone, or Aadhaar
        detailTitleLabel.getParent().getChildrenUnmodifiable().stream()
            .filter(node -> node instanceof Label && ((Label) node).getText().startsWith("Citizen #"))
            .findFirst()
            .ifPresent(node -> ((Label) node).setText("Citizen #" + complaint.filedByUserId));
    }

    // Attempts to update the selected complaint's status and pushes a notification to the queue
    private void handleStatusUpdate() {
        if (selectedComplaint == null) {
            showErrorAlert("Please select a complaint from the table first.");
            return;
        }

        Status chosenStatus = statusChoiceBox.getValue();
        try {
            selectedComplaint.updateStatus(chosenStatus);

            // Push user-friendly notification to queue — NotificationThread delivers it
            String message = buildNotificationMessage(selectedComplaint, chosenStatus);
            store.notificationQueue.offer(
                "USERID:" + selectedComplaint.filedByUserId + "|MSG:" + message);

            loadAllComplaints();
            System.out.println("[Officer] Updated complaint #" + selectedComplaint.complaintId
                               + " → " + chosenStatus);

        } catch (ComplaintExpiredException expiredException) {
            showErrorAlert(expiredException.getMessage());
        } catch (InvalidStatusTransitionException transitionException) {
            showErrorAlert(transitionException.getMessage());
        }
    }

    // Builds the user-friendly notification message for a given status transition
    private String buildNotificationMessage(BaseComplaint complaint, Status newStatus) {
        return switch (newStatus) {
            case RESOLVED     -> "Your complaint \"" + complaint.title + "\" has been resolved. Thank you for reporting.";
            case REJECTED     -> "Your complaint \"" + complaint.title + "\" has been reviewed and closed.";
            case UNDER_REVIEW -> "An officer has started reviewing your complaint \"" + complaint.title + "\".";
            default           -> "Your complaint \"" + complaint.title + "\" status updated to " + newStatus + ".";
        };
    }

    // Loads all complaints from all 7 boxes into the table
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

    // Updates the bell button with the current unread notification count
    private void updateBellCount() {
        int count = store.userNotifications.getOrDefault(officer.userId, List.of()).size();
        bellButton.setText("🔔 " + count);
    }

    // Shows all notifications for this officer in a popup
    private void showNotificationsPopup() {
        List<String> messages = store.userNotifications.getOrDefault(officer.userId, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Your Notifications (" + messages.size() + ")");
        alert.setContentText(messages.isEmpty() ? "No notifications yet." : String.join("\n\n", messages));
        alert.showAndWait();
    }

    // Deregisters callback and stops session thread, then returns to login
    private void handleLogout() {
        store.notificationThread.deregisterCallback(officer.userId);
        store.sessionTimeoutThread.stopThread();
        stage.setScene(new LoginScreen(stage).buildScene());
    }

    // Prints to console AND shows JavaFX ERROR Alert — Rule 7
    private void showErrorAlert(String message) {
        System.err.println("[OFFICER ERROR] " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
