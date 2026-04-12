// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Login screen that stores the authenticated user as BaseUser and routes by role.

package gui;

import enums.Role;
import store.DataStore;
import threads.SessionTimeoutThread;
import users.*;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class LoginScreen {

    private final Stage primaryStage;
    private final DataStore store = DataStore.getInstance();

    public LoginScreen(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    // Builds and returns the login scene — called by MainApp and by logout handlers in dashboards
    public Scene buildScene() {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(40));

        // Title
        Label titleLabel = new Label("Civilian Complaint Portal");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        GridPane.setColumnSpan(titleLabel, 2);
        GridPane.setHalignment(titleLabel, HPos.CENTER);

        // Fields
        TextField     usernameField  = new TextField();
        PasswordField passwordField  = new PasswordField();
        ChoiceBox<Role> roleChoiceBox = new ChoiceBox<>();
        roleChoiceBox.getItems().addAll(Role.values());
        roleChoiceBox.setValue(Role.CITIZEN);

        // Error label — hidden until a failed login attempt
        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        errorLabel.setVisible(false);
        GridPane.setColumnSpan(errorLabel, 2);
        GridPane.setHalignment(errorLabel, HPos.CENTER);

        Button loginButton = new Button("Login");
        loginButton.setDefaultButton(true);
        loginButton.setPrefWidth(120);

        // Layout
        grid.add(titleLabel,                     0, 0);
        grid.add(new Label("Username:"),         0, 1);
        grid.add(usernameField,                  1, 1);
        grid.add(new Label("Password:"),         0, 2);
        grid.add(passwordField,                  1, 2);
        grid.add(new Label("Role:"),             0, 3);
        grid.add(roleChoiceBox,                  1, 3);
        grid.add(loginButton,                    1, 4);
        grid.add(errorLabel,                     0, 5);

        // Login button handler — authenticates and routes to the correct dashboard
        loginButton.setOnAction(event -> handleLogin(
            usernameField.getText().trim(),
            passwordField.getText(),
            roleChoiceBox.getValue(),
            errorLabel
        ));

        return new Scene(grid, 900, 650);
    }

    // Iterates the correct user list, calls login(), and opens the matching dashboard
    private void handleLogin(String username, String password, Role selectedRole, Label errorLabel) {
        BaseUser matchedUser = findUser(username, password, selectedRole);

        if (matchedUser == null) {
            // Rule: inline error label only — never a popup for login failures
            errorLabel.setText("Invalid credentials or wrong role selected.");
            errorLabel.setVisible(true);
            return;
        }

        // POLYMORPHISM DEMO — called on BaseUser reference, dispatches to correct subclass at runtime
        matchedUser.performAction();

        // Start session timeout thread — 60 second idle limit
        SessionTimeoutThread sessionThread = new SessionTimeoutThread(60,
            () -> javafx.application.Platform.runLater(() -> {
                primaryStage.setScene(buildScene());
                System.out.println("[Session] Timeout — user returned to login screen.");
            }));
        sessionThread.setDaemon(true);
        sessionThread.start();
        store.sessionTimeoutThread = sessionThread;

        errorLabel.setVisible(false);
        openDashboard(matchedUser);
    }

    // Searches the correct list based on selected role and validates credentials
    private BaseUser findUser(String username, String password, Role selectedRole) {
        switch (selectedRole) {
            case CITIZEN -> {
                for (Citizen citizen : store.citizens) {
                    if (citizen.login(username, password)) return citizen;
                }
            }
            case OFFICER -> {
                for (Officer officer : store.officers) {
                    if (officer.login(username, password)) return officer;
                }
            }
            case ADMIN -> {
                for (Admin admin : store.admins) {
                    if (admin.login(username, password)) return admin;
                }
            }
        }
        return null;
    }

    // Routes the authenticated user to their role-specific dashboard
    private void openDashboard(BaseUser loggedInUser) {
        switch (loggedInUser.role) {
            case CITIZEN -> primaryStage.setScene(
                new CitizenDashboard(primaryStage, (Citizen) loggedInUser).buildScene());
            case OFFICER -> primaryStage.setScene(
                new OfficerDashboard(primaryStage, (Officer) loggedInUser).buildScene());
            case ADMIN   -> primaryStage.setScene(
                new AdminDashboard(primaryStage, (Admin) loggedInUser).buildScene());
        }
    }
}
