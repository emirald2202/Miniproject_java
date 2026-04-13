



package gui;

import complaints.*;
import enums.ComplaintCategory;
import enums.OfficerDepartment;
import enums.Status;
import exceptions.UnauthorizedAccessException;
import profile.CitizenProfile;
import search.ComplaintSearch;
import store.DataStore;
import threads.SessionTimeoutThread;
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

import java.util.ArrayList;
import java.util.List;

public class AdminDashboard {

    private final Stage stage;
    private final Admin admin;
    private final SessionTimeoutThread sessionThread;
    private final DataStore store = DataStore.getInstance();

    private Button bellButton;
    private BaseComplaint selectedComplaint;

    
    private ObservableList<BaseComplaint> infraData;
    private ObservableList<BaseComplaint> corruptionData;
    private ObservableList<BaseComplaint> noiseData;

    private Label selectedComplaintLabel;
    private ChoiceBox<Officer> officerChoiceBox;

    public AdminDashboard(Stage stage, Admin admin, SessionTimeoutThread sessionThread) {
        this.stage         = stage;
        this.admin         = admin;
        this.sessionThread = sessionThread;
    }

    
    public Scene buildScene() {
        BorderPane root = new BorderPane();
        root.setTop(buildTopBar());
        root.setCenter(buildCenterPanel());
        root.setRight(buildActionPanel());

        store.notificationThread.registerCallback(admin.userId,
            () -> Platform.runLater(() -> { updateBellCount(); refreshAllTabs(); }));

        refreshAllTabs();
        return new Scene(root, 1100, 700);
    }

    
    private HBox buildTopBar() {
        Label titleLabel = new Label("Admin Dashboard  —  " + admin.username
                                     + "  [Level " + admin.adminLevel + "]");
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
        VBox centerPanel = new VBox(8, buildSearchBar(), buildTabPane());
        centerPanel.setPadding(new Insets(10));
        VBox.setVgrow(centerPanel.getChildren().get(1), Priority.ALWAYS);
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
            refreshAllTabs();
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
                        showErrorAlert("Invalid category name.");
                        return;
                    }
                }
                default -> results = new ArrayList<>();
            }

            
            infraData.setAll(results.stream().filter(c -> c instanceof InfrastructureComplaint).toList());
            corruptionData.setAll(results.stream().filter(c -> c instanceof CorruptionComplaint).toList());
            noiseData.setAll(results.stream().filter(c -> c instanceof NoiseComplaint).toList());
        });

        HBox searchBar = new HBox(8, searchTypeBox, searchInputField, searchButton, resetButton);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        return searchBar;
    }

    
    private TabPane buildTabPane() {
        
        
        
        
        

        infraData      = FXCollections.observableArrayList();
        corruptionData = FXCollections.observableArrayList();
        noiseData      = FXCollections.observableArrayList();

        Tab infraTab      = new Tab("Infrastructure", buildTabTable(infraData));
        Tab corruptionTab = new Tab("Corruption",     buildTabTable(corruptionData));
        Tab noiseTab      = new Tab("Noise",          buildTabTable(noiseData));
        Tab manageTab     = new Tab("Manage Users",   buildManageUsersPane());

        infraTab.setClosable(false);
        corruptionTab.setClosable(false);
        noiseTab.setClosable(false);
        manageTab.setClosable(false);

        TabPane tabPane = new TabPane(infraTab, corruptionTab, noiseTab, manageTab);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        return tabPane;
    }

    
    private TableView<BaseComplaint> buildTabTable(ObservableList<BaseComplaint> data) {
        TableView<BaseComplaint> table = new TableView<>(data);
        table.setPlaceholder(new Label("No complaints in this category."));

        TableColumn<BaseComplaint, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(row -> new SimpleIntegerProperty(row.getValue().complaintId).asObject());
        idCol.setPrefWidth(45);

        TableColumn<BaseComplaint, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(row -> new SimpleStringProperty(row.getValue().title));
        titleCol.setPrefWidth(180);

        
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

        
        table.getSelectionModel().selectedItemProperty().addListener((obs, old, newItem) -> {
            if (newItem != null) {
                sessionThread.resetTimer();
                selectedComplaint = newItem;
                selectedComplaintLabel.setText("#" + newItem.complaintId + " — " + newItem.title);
            }
        });

        return table;
    }

    
    private VBox buildActionPanel() {
        Label heading = new Label("Actions");
        heading.setFont(Font.font("System", FontWeight.BOLD, 14));

        selectedComplaintLabel = new Label("None selected");
        selectedComplaintLabel.setWrapText(true);
        selectedComplaintLabel.setMaxWidth(190);

        
        officerChoiceBox = new ChoiceBox<>();
        officerChoiceBox.getItems().addAll(store.officers);
        officerChoiceBox.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(Officer o)   { return o == null ? "" : o.username; }
            @Override public Officer fromString(String s) { return null; }
        });

        Button assignButton = new Button("Assign Officer");
        assignButton.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        assignButton.setOnAction(e -> {
            sessionThread.resetTimer();
            handleAssignOfficer(officerChoiceBox.getValue());
        });

        
        Button viewCitizenButton = new Button("View Citizen Details");
        viewCitizenButton.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        viewCitizenButton.setOnAction(e -> {
            sessionThread.resetTimer();
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


    
    private void handleAssignOfficer(Officer selectedOfficer) {
        if (selectedComplaint == null) {
            showErrorAlert("Please select a complaint first.");
            return;
        }
        if (selectedOfficer == null) {
            showErrorAlert("Please select an officer to assign.");
            return;
        }
        
        selectedComplaint.assignedToOfficerId = selectedOfficer.userId;
        selectedOfficer.assignedComplaints++;

        
        store.notificationQueue.offer("USERID:" + selectedOfficer.userId
            + "|MSG:You have been assigned complaint #" + selectedComplaint.complaintId
            + ": \"" + selectedComplaint.title + "\". Please review and take action.");

        
        store.notificationQueue.offer("USERID:" + selectedComplaint.filedByUserId
            + "|MSG:Your complaint \"" + selectedComplaint.title
            + "\" has been assigned to an officer and is now being handled.");

        refreshAllTabs();
        System.out.println("[Admin] Assigned complaint #" + selectedComplaint.complaintId
                           + " to " + selectedOfficer.username);
    }

    
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

        
        
        String profileData = targetCitizen.viewProfile(admin);

        if (profileData == null) {
            showErrorAlert("Unauthorized access — only Admins can view citizen profile data.");
            return;
        }

        
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

    
    private ScrollPane buildManageUsersPane() {
        VBox pane = new VBox(20);
        pane.setPadding(new Insets(20));

        
        Label officerHeading = new Label("Add New Officer");
        officerHeading.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField oUsername = new TextField(); oUsername.setPromptText("Username");
        javafx.scene.control.PasswordField oPassword = new javafx.scene.control.PasswordField();
        oPassword.setPromptText("Password");
        ChoiceBox<OfficerDepartment> oDept = new ChoiceBox<>();
        oDept.getItems().addAll(OfficerDepartment.values());
        oDept.setValue(OfficerDepartment.PWD);

        Button addOfficerBtn = new Button("Add Officer");
        addOfficerBtn.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white;");
        addOfficerBtn.setOnAction(e -> {
            sessionThread.resetTimer();
            String u = oUsername.getText().trim();
            String p = oPassword.getText();
            if (u.isEmpty() || p.isEmpty()) { showErrorAlert("Fill all officer fields."); return; }
            int newId = generateNextUserId();
            Officer newOfficer = new Officer(newId, u, p, oDept.getValue());
            store.officers.add(newOfficer);
            officerChoiceBox.getItems().add(newOfficer);
            oUsername.clear(); oPassword.clear();
            showInfoAlert("Officer \"" + u + "\" added (ID=" + newId + ").");
        });

        
        Label citizenHeading = new Label("Add New Citizen");
        citizenHeading.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField cUsername = new TextField(); cUsername.setPromptText("Username");
        javafx.scene.control.PasswordField cPassword = new javafx.scene.control.PasswordField();
        cPassword.setPromptText("Password");
        TextField cFullName = new TextField(); cFullName.setPromptText("Full Name");
        TextField cAadhaar  = new TextField(); cAadhaar.setPromptText("Aadhaar (XXXX-XXXX-XXXX)");
        TextField cPhone    = new TextField(); cPhone.setPromptText("Phone Number");
        TextField cAddress  = new TextField(); cAddress.setPromptText("Home Address");

        Button addCitizenBtn = new Button("Add Citizen");
        addCitizenBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        addCitizenBtn.setOnAction(e -> {
            sessionThread.resetTimer();
            String u = cUsername.getText().trim();
            String p = cPassword.getText();
            String n = cFullName.getText().trim();
            String a = cAadhaar.getText().trim();
            String ph = cPhone.getText().trim();
            String ad = cAddress.getText().trim();
            if (u.isEmpty() || p.isEmpty() || n.isEmpty() || a.isEmpty() || ph.isEmpty() || ad.isEmpty()) {
                showErrorAlert("Fill all citizen fields."); return;
            }
            int newId = generateNextUserId();
            CitizenProfile profile = new CitizenProfile(n, a, ph, ad);
            Citizen newCitizen = new Citizen(newId, u, p, profile);
            store.citizens.add(newCitizen);
            cUsername.clear(); cPassword.clear(); cFullName.clear();
            cAadhaar.clear(); cPhone.clear(); cAddress.clear();
            showInfoAlert("Citizen \"" + u + "\" registered (ID=" + newId + ").");
        });

        
        Label adminHeading = new Label("Add New Admin");
        adminHeading.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField aUsername = new TextField(); aUsername.setPromptText("Username");
        javafx.scene.control.PasswordField aPassword = new javafx.scene.control.PasswordField();
        aPassword.setPromptText("Password");
        TextField aLevel = new TextField(); aLevel.setPromptText("Admin Level (1-5)");
        aLevel.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) aLevel.setText(oldVal);
        });

        Button addAdminBtn = new Button("Add Admin");
        addAdminBtn.setStyle("-fx-background-color: #8e44ad; -fx-text-fill: white;");
        addAdminBtn.setOnAction(e -> {
            sessionThread.resetTimer();
            String u = aUsername.getText().trim();
            String p = aPassword.getText();
            if (u.isEmpty() || p.isEmpty() || aLevel.getText().isEmpty()) {
                showErrorAlert("Fill all admin fields."); return;
            }
            int newId = generateNextUserId();
            int level = Integer.parseInt(aLevel.getText());
            Admin newAdmin = new Admin(newId, u, p, level);
            store.admins.add(newAdmin);
            aUsername.clear(); aPassword.clear(); aLevel.clear();
            showInfoAlert("Admin \"" + u + "\" added (ID=" + newId + ").");
        });

        pane.getChildren().addAll(
            officerHeading,
            new Label("Username:"), oUsername,
            new Label("Password:"), oPassword,
            new Label("Department:"), oDept,
            addOfficerBtn,
            new Separator(),
            citizenHeading,
            new Label("Username:"), cUsername,
            new Label("Password:"), cPassword,
            new Label("Full Name:"), cFullName,
            new Label("Aadhaar:"), cAadhaar,
            new Label("Phone:"), cPhone,
            new Label("Address:"), cAddress,
            addCitizenBtn,
            new Separator(),
            adminHeading,
            new Label("Username:"), aUsername,
            new Label("Password:"), aPassword,
            new Label("Admin Level:"), aLevel,
            addAdminBtn
        );

        ScrollPane scroll = new ScrollPane(pane);
        scroll.setFitToWidth(true);
        return scroll;
    }

    
    private int generateNextUserId() {
        int max = 0;
        for (Citizen c  : store.citizens) if (c.userId  > max) max = c.userId;
        for (Officer o  : store.officers) if (o.userId  > max) max = o.userId;
        for (Admin a    : store.admins)   if (a.userId  > max) max = a.userId;
        return max + 1;
    }

    
    private void refreshAllTabs() {
        infraData.setAll(store.infraBox.getAllComplaints());
        corruptionData.setAll(store.corruptionBox.getAllComplaints());
        noiseData.setAll(store.noiseBox.getAllComplaints());
    }


    
    private void updateBellCount() {
        int count = store.userNotifications.getOrDefault(admin.userId, List.of()).size();
        bellButton.setText("🔔 " + count);
    }

    
    private void showNotificationsPopup() {
        List<String> messages = store.userNotifications.getOrDefault(admin.userId, List.of());
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Notifications");
        alert.setHeaderText("Admin Notifications (" + messages.size() + ")");
        alert.setContentText(messages.isEmpty() ? "No notifications." : String.join("\n\n", messages));
        alert.showAndWait();
    }

    
    private void handleLogout() {
        store.notificationThread.deregisterCallback(admin.userId);
        sessionThread.stopThread();
        stage.setScene(new LoginScreen(stage).buildScene());
    }

    
    private void showErrorAlert(String message) {
        System.err.println("[ADMIN ERROR] " + message);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("System Alert");
        alert.setContentText(message);
        alert.showAndWait();
    }

    
    private void showInfoAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
