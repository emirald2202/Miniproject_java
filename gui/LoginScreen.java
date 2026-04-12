// OOP CONCEPT : Separation of Concerns (GUI)
// ASSIGNMENT  : N/A
// PURPOSE     : User authentication and dynamic polymorphic routing screen.

package gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import enums.Role;
import store.DataStore;
import users.*;

public class LoginScreen {

    public static Scene getScene(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);
        grid.setHgap(10);

        Label title = new Label("Civilian Complaint Portal - Login");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        grid.add(title, 0, 0, 2, 1);

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        grid.add(userLabel, 0, 1);
        grid.add(userField, 1, 1);

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        grid.add(passLabel, 0, 2);
        grid.add(passField, 1, 2);

        Label roleLabel = new Label("Role:");
        ChoiceBox<Role> roleChoice = new ChoiceBox<>();
        roleChoice.getItems().addAll(Role.CITIZEN, Role.OFFICER, Role.ADMIN);
        roleChoice.setValue(Role.CITIZEN);
        grid.add(roleLabel, 0, 3);
        grid.add(roleChoice, 1, 3);

        Button loginBtn = new Button("Login");
        grid.add(loginBtn, 1, 4);

        Label errorLabel = new Label();
        errorLabel.setTextFill(Color.RED);
        grid.add(errorLabel, 0, 5, 2, 1);

        loginBtn.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            Role role = roleChoice.getValue();

            BaseUser loggedInUser = null;
            DataStore store = DataStore.getInstance();

            if (role == Role.CITIZEN) {
                for (Citizen c : store.citizens) {
                    if (c.login(username, password)) loggedInUser = c;
                }
            } else if (role == Role.OFFICER) {
                for (Officer o : store.officers) {
                    if (o.login(username, password)) loggedInUser = o;
                }
            } else if (role == Role.ADMIN) {
                for (Admin a : store.admins) {
                    if (a.login(username, password)) loggedInUser = a;
                }
            }

            if (loggedInUser != null) {
                // Route to correct dashboard dynamically
                if (loggedInUser instanceof Citizen) {
                    primaryStage.setScene(CitizenDashboard.getScene(primaryStage, (Citizen) loggedInUser));
                } else if (loggedInUser instanceof Officer) {
                    primaryStage.setScene(OfficerDashboard.getScene(primaryStage, (Officer) loggedInUser));
                } else if (loggedInUser instanceof Admin) {
                    primaryStage.setScene(AdminDashboard.getScene(primaryStage, (Admin) loggedInUser));
                }
            } else {
                errorLabel.setText("Invalid credentials for the selected role.");
            }
        });

        return new Scene(grid, 400, 300);
    }
}
