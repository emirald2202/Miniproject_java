// OOP CONCEPT : Inheritance & Polymorphism
// ASSIGNMENT  : 4
// PURPOSE     : Login screen that stores the authenticated user as BaseUser and routes by role.

package gui;

import enums.Role;
import profile.CitizenProfile;
import store.DataStore;
import threads.SessionTimeoutThread;
import users.*;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
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

        // Opens a second independent login window sharing the same DataStore
        Button newWindowButton = new Button("New Window");
        newWindowButton.setOnAction(e -> {
            Stage newStage = new Stage();
            newStage.setTitle("Civilian Complaint Portal");
            newStage.setWidth(900);
            newStage.setHeight(650);
            newStage.setScene(new LoginScreen(newStage).buildScene());
            newStage.show();
        });

        // Success label — shown after successful registration
        Label successLabel = new Label();
        successLabel.setTextFill(Color.GREEN);
        successLabel.setVisible(false);
        GridPane.setColumnSpan(successLabel, 2);
        GridPane.setHalignment(successLabel, HPos.CENTER);

        // Register button — opens citizen self-registration modal
        Button registerButton = new Button("New? Register as Citizen");
        registerButton.setOnAction(e -> showRegistrationForm(successLabel));
        GridPane.setColumnSpan(registerButton, 2);
        GridPane.setHalignment(registerButton, HPos.CENTER);

        // Layout
        grid.add(titleLabel,                     0, 0);
        grid.add(new Label("Username:"),         0, 1);
        grid.add(usernameField,                  1, 1);
        grid.add(new Label("Password:"),         0, 2);
        grid.add(passwordField,                  1, 2);
        grid.add(new Label("Role:"),             0, 3);
        grid.add(roleChoiceBox,                  1, 3);
        grid.add(newWindowButton,                0, 4);
        grid.add(loginButton,                    1, 4);
        grid.add(errorLabel,                     0, 5);
        grid.add(registerButton,                 0, 6);
        grid.add(successLabel,                   0, 7);

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

        // Start a per-window session timeout thread — 300 second idle limit
        // Each window gets its own independent thread so windows don't interfere with each other
        SessionTimeoutThread sessionThread = new SessionTimeoutThread(300,
            () -> javafx.application.Platform.runLater(() -> {
                primaryStage.setScene(buildScene());
                System.out.println("[Session] Timeout — user returned to login screen.");
            }));
        sessionThread.setDaemon(true);
        sessionThread.start();

        errorLabel.setVisible(false);
        openDashboard(matchedUser, sessionThread);
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

    // Opens a modal registration form — citizens only; officers/admins must be created by admin
    private void showRegistrationForm(Label successLabel) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Register — New Citizen Account");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(25));
        form.setAlignment(Pos.CENTER);

        TextField     usernameField   = new TextField();
        PasswordField passwordField   = new PasswordField();
        PasswordField confirmField    = new PasswordField();
        TextField     fullNameField   = new TextField();
        TextField     aadhaarField    = new TextField();
        TextField     phoneField      = new TextField();
        TextField     addressField    = new TextField();

        Label formError = new Label();
        formError.setTextFill(Color.RED);
        formError.setWrapText(true);
        formError.setMaxWidth(280);
        GridPane.setColumnSpan(formError, 2);

        form.add(new Label("Username:"),         0, 0); form.add(usernameField,  1, 0);
        form.add(new Label("Password:"),         0, 1); form.add(passwordField,  1, 1);
        form.add(new Label("Confirm Password:"), 0, 2); form.add(confirmField,   1, 2);
        form.add(new Label("Full Name:"),        0, 3); form.add(fullNameField,  1, 3);
        form.add(new Label("Aadhaar:"),          0, 4); form.add(aadhaarField,   1, 4);
        form.add(new Label("Phone:"),            0, 5); form.add(phoneField,     1, 5);
        form.add(new Label("Address:"),          0, 6); form.add(addressField,   1, 6);
        form.add(formError,                      0, 7);

        Button submitButton = new Button("Create Account");
        submitButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        submitButton.setPrefWidth(150);
        GridPane.setHalignment(submitButton, HPos.RIGHT);
        form.add(submitButton, 1, 8);

        submitButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String confirm  = confirmField.getText();
            String fullName = fullNameField.getText().trim();
            String aadhaar  = aadhaarField.getText().trim();
            String phone    = phoneField.getText().trim();
            String address  = addressField.getText().trim();

            // Validate all fields filled
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()
                    || fullName.isEmpty() || aadhaar.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                formError.setText("All fields are required.");
                return;
            }
            // Validate passwords match
            if (!password.equals(confirm)) {
                formError.setText("Passwords do not match.");
                return;
            }
            // Validate username not already taken
            for (Citizen existing : store.citizens) {
                if (existing.username.equalsIgnoreCase(username)) {
                    formError.setText("Username \"" + username + "\" is already taken.");
                    return;
                }
            }

            // Create account
            int newId = generateNextUserId();
            CitizenProfile profile = new CitizenProfile(fullName, aadhaar, phone, address);
            Citizen newCitizen = new Citizen(newId, username, password, profile);
            store.citizens.add(newCitizen);

            modal.close();
            successLabel.setText("Account created! Log in as Citizen with username \"" + username + "\".");
            successLabel.setVisible(true);
        });

        modal.setScene(new Scene(form, 420, 380));
        modal.showAndWait();
    }

    // Generates the next unique user ID across all user lists
    private int generateNextUserId() {
        int max = 0;
        for (Citizen c  : store.citizens) if (c.userId  > max) max = c.userId;
        for (Officer o  : store.officers) if (o.userId  > max) max = o.userId;
        for (Admin a    : store.admins)   if (a.userId  > max) max = a.userId;
        return max + 1;
    }

    // Routes the authenticated user to their role-specific dashboard, passing the per-window session thread
    private void openDashboard(BaseUser loggedInUser, SessionTimeoutThread sessionThread) {
        switch (loggedInUser.role) {
            case CITIZEN -> primaryStage.setScene(
                new CitizenDashboard(primaryStage, (Citizen) loggedInUser, sessionThread).buildScene());
            case OFFICER -> primaryStage.setScene(
                new OfficerDashboard(primaryStage, (Officer) loggedInUser, sessionThread).buildScene());
            case ADMIN   -> primaryStage.setScene(
                new AdminDashboard(primaryStage, (Admin) loggedInUser, sessionThread).buildScene());
        }
    }
}
