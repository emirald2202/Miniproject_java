



package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import store.DataStore;

public class MainApp extends Application {

    
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

    
    public static void main(String[] args) {
        launch(args);
    }
}
