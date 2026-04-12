// OOP CONCEPT : Control Flow & Operators
// ASSIGNMENT  : 1
// PURPOSE     : JavaFX Application entry point — launches the login screen on the primary stage.

package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import store.DataStore;

public class MainApp extends Application {

    // JavaFX calls start() automatically after launch() — this is the GUI entry point
    @Override
    public void start(Stage primaryStage) {
        DataStore store = DataStore.getInstance();

        primaryStage.setTitle("Civilian Complaint Portal");
        primaryStage.setWidth(900);
        primaryStage.setHeight(650);
        primaryStage.setResizable(true);

        LoginScreen loginScreen = new LoginScreen(primaryStage);
        primaryStage.setScene(loginScreen.buildScene());
        primaryStage.show();
    }

    // Required by JavaFX — delegates to Application.launch()
    public static void main(String[] args) {
        launch(args);
    }
}
