// OOP CONCEPT : Generics + Encapsulation + Operators
// ASSIGNMENT  : 5, 2, 1
// PURPOSE     : Admin dashboard — typed tabs, citizen data access, officer assignment, XOR log panel.

package gui;

import complaints.*;
import enums.ComplaintCategory;
import enums.Status;
import exceptions.UnauthorizedAccessException;
import priority.PriorityCalculator;
import search.ComplaintSearch;
import store.DataStore;
import users.Admin;
import users.Citizen;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard {

    private final Stage stage;
    private final Admin admin;
    private final DataStore store = DataStore.getInstance();

    private Button bellButton;
    private BaseComplaint selectedComplaint;

    // Per-tab table data — each backed by its own typed ComplaintBox (Generics demo)
    private ObservableList<BaseComplaint> infraData;
    private ObservableList<BaseComplaint> corruptionData;
    private ObservableList<BaseComplaint> noiseData;

    private Label selectedComplaintLabel;

    public AdminDashboard(Stage stage, Admin admin) {
        this.stage = stage;
        this.admin = admin;
    }

    // Builds and returns the full admin dashboard scene
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCenterPanel());
        root.setRight(buildActionPanel());
        root.setBottom(buildLogPanel());

        store.notificationThread.registerCallback(admin.userId,
            () -> Platform.runLater(this::updateBellCount));

        refreshAllTabs();
        return new Scene(root, 1100, 700);
    }

    // Builds the top bar with admin label and bell
    private HBox buildTopBar() {
        Label titleLabel = new Label("Admin Dashboard  —  " + admin.username
                                     + "  [Level " + admin.adminLevel + "]");
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

    // Builds the center panel with search bar and the 3-tab complaint view
    private VBox buildCenterPanel() {
        VBox centerPanel = new VBox(8, buildSearchBar(), buildTabPane());
        centerPanel.setPadding(new Insets(10));
        VBox.setVgrow(centerPanel.getChildren().get(1), Priority.ALWAYS);
        return centerPanel;
    }

    // Builds the search bar — same pattern as OfficerDashboard
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
            refreshAllTabs();
        });

        // Routes to correct ComplaintSearch overload based on dropdown
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
                        showErrorAlert("Invalid category name.");
                        return;
                    }
                }
                default -> results = new ArrayList<>();
            }

            // Show results across all tabs by filtering each
            infraData.setAll(results.stream().filter(c -> c instanceof InfrastructureComplaint).toList());
            corruptionData.setAll(results.stream().filter(c -> c instanceof CorruptionComplaint).toList());
            noiseData.setAll(results.stream().filter(c -> c instanceof NoiseComplaint).toList());
        });

        HBox searchBar = new HBox(8, searchTypeBox, searchInputField, searchButton, resetButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        return searchBar;
    }

    // Builds 3-tab pane — each tab backed by a separate typed ComplaintBox (Generics demo)
    private TabPane buildTabPane() {
        // GENERICS DEMO:
        // infraData    ← store.infraBox.getAllComplaints()     ComplaintBox<InfrastructureComplaint>
        // corruptionData ← store.corruptionBox.getAllComplaints() ComplaintBox<CorruptionComplaint>
        // noiseData    ← store.noiseBox.getAllComplaints()     ComplaintBox<NoiseComplaint>
        // Each box can only hold its declared type — enforced at compile time, not by if/else.

        infraData      = FXCollections.observableArrayList();
        corruptionData = FXCollections.observableArrayList();
        noiseData      = FXCollections.observableArrayList();

        Tab infraTab      = new Tab("Infrastructure", buildTabTable(infraData));
        Tab corruptionTab = new Tab("Corruption",     buildTabTable(corruptionData));
        Tab noiseTab      = new Tab("Noise",          buildTabTable(noiseData));

        infraTab.setClosable(false);
        corruptionTab.setClosable(false);
        noiseTab.setClosable(false);

        TabPane tabPane = new TabPane(infraTab, corruptionTab, noiseTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return tabPane;
    }

    // Builds a TableView for one tab — columns include citizen name and assigned officer (admin can see all)
    private TableView<BaseComplaint> buildTabTable(ObservableList<BaseComplaint> data) {
        TableView<BaseComplaint> table = new TableView<>(data);
        table.setPlaceholder(new Label("No complaints in this category."));

        TableColumn<BaseComplaint, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().complaintId).asObject());
        idCol.setPrefWidth(45);

        TableColumn<BaseComplaint, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().title));
        titleCol.setPrefWidth(180);

        // Admin CAN see citizen username — resolves from DataStore by filedByUserId
        TableColumn<BaseComplaint, String> citizenCol = new TableColumn<>("Citizen");
        citizenCol.setCellValueFactory(row -> {
            int userId = row.getValue().filedByUserId;
            String name = store.citizens.stream()
                .filter(c -> c.userId == userId)
                .map(c -> c.username)
                .findFirst().orElse("Unknown");
            return new SimpleStringProperty(name);
        });
        citizenCol.setPrefWidth(90);

        TableColumn<BaseComplaint, Integer> scoreCol = new TableColumn<>("Priority");
        scoreCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().priorityScore).asObject());
        scoreCol.setPrefWidth(65);

        TableColumn<BaseComplaint, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().status.toString()));
        statusCol.setPrefWidth(100);

        TableColumn<BaseComplaint, String> officerCol = new TableColumn<>("Assigned Officer");
        officerCol.setCellValueFactory(row -> {
            int officerId = row.getValue().assignedToOfficerId;
            if (officerId == -1) return new SimpleStringProperty("Unassigned");
            String name = store.officers.stream()
                .filter(o -> o.userId == officerId)
                .map(o -> o.username)
                .findFirst().orElse("Unknown");
            return new SimpleStringProperty(name);
        });
        officerCol.setPrefWidth(120);

        table.getColumns().addAll(idCol, titleCol, citizenCol, scoreCol, statusCol, officerCol);

        // Track selected complaint for the action panel
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newItem) -> {
            if (newItem != null) {
                store.sessionTimeoutThread.resetTimer();
                selectedComplaint = newItem;
                selectedComplaintLabel.setText("#" + newItem.complaintId + " — " + newItem.title);
            }
        });

        return table;
    }

    // Builds the right action panel — assign officer and view citizen details
    private VBox buildActionPanel() {
        Label heading = new Label("Actions");
        heading.setFont(Font.font("System", FontWeight.BOLD, 14));

        selectedComplaintLabel = new Label("None selected");
        selectedComplaintLabel.setWrapText(true);
        selectedComplaintLabel.setMaxWidth(190);

        // Officer assignment
        ChoiceBox<Officer> officerChoiceBox = new ChoiceBox<>();
        officerChoiceBox.getItems().addAll(store.officers);
        officerChoiceBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Officer o)   { return o == null ? "" : o.username; }
            @Override public Officer fromString(String s) { return null; }
        });

        Button assignButton = new Button("Assign Officer");
        assignButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        assignButton.setOnAction(e -> {
            store.sessionTimeoutThread.resetTimer();
            handleAssignOfficer(officerChoiceBox.getValue());
        });

        // Citizen details — calls getVerifiedData(admin) via citizen.viewProfile(admin)
        Button viewCitizenButton = new Button("View Citizen Details");
        viewCitizenButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        viewCitizenButton.setOnAction(e -> {
            store.sessionTimeoutThread.resetTimer();
            handleViewCitizenDetails();
        });

        VBox actionPanel = new VBox(10,
            heading,
            new Separator(),
            new Label("Selected:"),
            selectedComplaintLabel,
            new Separator(),
            new Label("Assign to Officer:"),
            officerChoiceBox,
            assignButton,
            new Separator(),
            viewCitizenButton
        );
        actionPanel.setPadding(new Insets(15));
        actionPanel.setPrefWidth(220);
        actionPanel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #bdc3c7; -fx-border-width: 0 0 0 1;");
        return actionPanel;
    }

    // Builds the bottom log panel — obfuscated and decoded logs side by side (Assignment 1 demo)
    private HBox buildLogPanel() {
        Label heading = new Label("System Log (XOR Obfuscation Demo)");
        heading.setFont(Font.font("System", FontWeight.BOLD, 12));

        TextArea obfuscatedArea = new TextArea();
        obfuscatedArea.setEditable(false);
        obfuscatedArea.setPromptText("Obfuscated log will appear here...");
        obfuscatedArea.setPrefRowCount(3);
        obfuscatedArea.setWrapText(true);

        TextArea decodedArea = new TextArea();
        decodedArea.setEditable(false);
        decodedArea.setPromptText("Decoded log will appear here...");
        decodedArea.setPrefRowCount(3);
        decodedArea.setWrapText(true);

        Button generateButton = new Button("Generate Log");
        generateButton.setStyle("-fx-background-color: #16a085; -fx-text-fill: white;");
        generateButton.setOnAction(e -> {
            store.sessionTimeoutThread.resetTimer();
            // Build a log entry and run it through PriorityCalculator XOR methods
            String rawLog = "Admin:" + admin.username
                + " | Action:SystemView"
                + " | Time:" + LocalDateTime.now()
                + " | Complaints:" + getTotalComplaintCount();
            String obfuscated = PriorityCalculator.obfuscateLog(rawLog);
            String decoded    = PriorityCalculator.decodeLog(obfuscated);
            obfuscatedArea.setText(obfuscated);
            decodedArea.setText(decoded);
        });

        VBox leftLog  = new VBox(4, new Label("Obfuscated Log:"), obfuscatedArea);
        VBox rightLog = new VBox(4, new Label("Decoded Log:"),    decodedArea);
        HBox.setHgrow(leftLog,  Priority.ALWAYS);
        HBox.setHgrow(rightLog, Priority.ALWAYS);

        HBox logPanel = new HBox(10, new VBox(4, heading, generateButton), leftLog, rightLog);
        logPanel.setPadding(new Insets(10, 15, 10, 15));
        logPanel.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-width: 1 0 0 0;");
        return logPanel;
    }

    // Assigns the selected officer to the selected complaint — admin authority bypasses assignOfficer() validation
    private void handleAssignOfficer(Officer selectedOfficer) {
        if (selectedComplaint == null) {
            showErrorAlert("Please select a complaint first.");
            return;
        }
        if (selectedOfficer == null) {
            showErrorAlert("Please select an officer to assign.");
            return;
        }
        // Admin directly sets the field — this is an administrative override, not a normal officer action
        selectedComplaint.assignedToOfficerId = selectedOfficer.userId;
        selectedOfficer.assignedComplaints++;
        refreshAllTabs();
        System.out.println("[Admin] Assigned complaint #" + selectedComplaint.complaintId
                           + " to " + selectedOfficer.username);
    }

    // Finds the citizen who filed the selected complaint and shows their full profile in a modal window
    private void handleViewCitizenDetails() {
        if (selectedComplaint == null) {
            showErrorAlert("Please select a complaint first.");
            return;
        }

        Citizen targetCitizen = null;
        for (Citizen citizen : store.citizens) {
            if (citizen.userId == selectedComplaint.filedByUserId) {
                targetCitizen = citizen;
                break;
            }
        }

        if (targetCitizen == null) {
            showErrorAlert("Citizen not found for this complaint.");
            return;
        }

        // ENCAPSULATION DEMO: getVerifiedData() is the ONLY way to read CitizenProfile fields.
        // viewProfile() returns null if the requestor is not an Admin (UnauthorizedAccessException caught inside).
        String profileData = targetCitizen.viewProfile(admin);

        if (profileData == null) {
            showErrorAlert("Unauthorized access — only Admins can view citizen profile data.");
            return;
        }

        // Show in a proper modal window — not an Alert, per spec
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Citizen Profile — Verified Data");

        Label profileLabel = new Label(profileData);
        profileLabel.setWrapText(true);
        profileLabel.setPadding(new Insets(20));
        profileLabel.setFont(Font.font("Monospaced", 13));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(ev -> modal.close());

        VBox modalContent = new VBox(15, profileLabel, closeButton);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(20));

        modal.setScene(new Scene(modalContent, 400, 200));
        modal.showAndWait();
    }

    // Reloads each tab's data from its respective typed ComplaintBox
    private void refreshAllTabs() {
        infraData.setAll(store.infraBox.getAllComplaints());
        corruptionData.setAll(store.corruptionBox.getAllComplaints());
        noiseData.setAll(store.noiseBox.getAllComplaints());
    }

    // Returns the total complaint count across all 7 boxes — used in log generation
    private int getTotalComplaintCount() {
        return store.infraBox.size() + store.corruptionBox.size() + store.noiseBox.size()
             + store.trafficBox.size() + store.sanitationBox.size()
             + store.waterSupplyBox.size() + store.electricityBox.size();
    }

    // Updates bell button with current unread notification count
    private void updateBellCount() {
        int count = store.userNotifications.getOrDefault(admin.userId, List.of()).size();
        bellButton.setText("🔔 " + count);
    }

    // Shows notifications popup for admin
    private void showNotificationsPopup() {
        List<String> messages = store.userNotifications.getOrDefault(admin.userId, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Admin Notifications (" + messages.size() + ")");
        alert.setContentText(messages.isEmpty() ? "No notifications." : String.join("\n\n", messages));
        alert.showAndWait();
    }

    // Deregisters callback, stops session, returns to login
    private void handleLogout() {
        store.notificationThread.deregisterCallback(admin.userId);
        store.sessionTimeoutThread.stopThread();
        stage.setScene(new LoginScreen(stage).buildScene());
    }

    // Prints to console AND shows JavaFX ERROR Alert — Rule 7
    private void showErrorAlert(String message) {
        System.err.println("[ADMIN ERROR] " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
