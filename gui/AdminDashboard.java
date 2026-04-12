// OOP CONCEPT : Separation of Concerns (GUI)
// ASSIGNMENT  : N/A
// PURPOSE     : Admin interface managing 7 generic categories and assigning officers.

package gui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import users.Admin;
import store.DataStore;
import complaints.BaseComplaint;
import enums.Status;
import priority.PriorityCalculator;

public class AdminDashboard {
    
    public static Scene getScene(Stage stage, Admin admin) {
        BorderPane root = new BorderPane();
        DataStore store = DataStore.getInstance();
        
        Label topLbl = new Label("Root Admin Dashboard");
        topLbl.setStyle("-fx-font-size: 18px; -fx-padding: 10px;");
        Button bellBtn = new Button("🔔 " + (store.userNotifications.containsKey(admin.userId) ? store.userNotifications.get(admin.userId).size() : 0));
        
        TextField searchFld = new TextField(); searchFld.setPromptText("Search ID / Target");
        Button searchBtn = new Button("Search Global");
        
        HBox topArea = new HBox(15, topLbl, bellBtn, searchFld, searchBtn);
        root.setTop(topArea);

        TabPane tabs = new TabPane();
        
        tabs.getTabs().add(createTab("Infrastructure", store.infraBox.getAll()));
        tabs.getTabs().add(createTab("Corruption", store.corruptionBox.getAll()));
        tabs.getTabs().add(createTab("Noise", store.noiseBox.getAll()));
        tabs.getTabs().add(createTab("Traffic", store.trafficBox.getAll()));
        tabs.getTabs().add(createTab("Sanitation", store.sanitationBox.getAll()));
        tabs.getTabs().add(createTab("Water Supply", store.waterSupplyBox.getAll()));
        tabs.getTabs().add(createTab("Electricity", store.electricityBox.getAll()));

        root.setCenter(tabs);

        // Global Search Bind
        search.ComplaintSearch engine = new search.ComplaintSearch();
        searchBtn.setOnAction(e -> {
            try {
                java.util.List<BaseComplaint> res;
                if(searchFld.getText().matches("\\d+")) {
                    res = engine.search(Integer.parseInt(searchFld.getText()));
                } else {
                    res = engine.search(searchFld.getText());
                }
                
                Tab searchTab = createTab("Search Results", res);
                tabs.getTabs().add(0, searchTab);
                tabs.getSelectionModel().select(searchTab);
                
            } catch (exceptions.ComplaintNotFoundException ex) {
                new Alert(Alert.AlertType.ERROR, "No matching complaints across any category.").show();
            }
        });

        // Live notification
        threads.NotificationThread.uiCallback = () -> {
            int unread = store.userNotifications.containsKey(admin.userId) ? store.userNotifications.get(admin.userId).size() : 0;
            bellBtn.setText("🔔 " + unread);
        };

        VBox right = new VBox(15);
        right.setPadding(new Insets(10));
        ChoiceBox<Integer> assignOpt = new ChoiceBox<>(); // simplified to just show IDs
        for(users.Officer o : store.officers) assignOpt.getItems().add(o.userId);
        
        Button assignBtn = new Button("Assign Officer");
        Button ctznBtn = new Button("View Citizen Details");
        Button logoutBtn = new Button("Logout");

        // View Citizen Logic (Encapsulation Demo)
        ctznBtn.setOnAction(e -> {
            Tab selTab = tabs.getSelectionModel().getSelectedItem();
            if (selTab != null && selTab.getContent() instanceof TableView) {
                @SuppressWarnings("unchecked")
                TableView<BaseComplaint> tv = (TableView<BaseComplaint>) selTab.getContent();
                BaseComplaint selC = tv.getSelectionModel().getSelectedItem();
                if (selC != null) {
                    try {
                        users.Citizen targetCitizen = null;
                        for(users.Citizen c : store.citizens) {
                            if(c.userId == selC.filedByUserId) targetCitizen = c;
                        }
                        if(targetCitizen != null) {
                            String secureData = targetCitizen.getProfile().getVerifiedData(admin);
                            new Alert(Alert.AlertType.INFORMATION, "Encapsulation Passed:\n" + secureData).show();
                        }
                    } catch (exceptions.UnauthorizedAccessException ex) {
                        new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                    }
                }
            }
        });

        // Assign Logic
        assignBtn.setOnAction(e -> {
            Tab selTab = tabs.getSelectionModel().getSelectedItem();
            if (selTab != null && selTab.getContent() instanceof TableView && assignOpt.getValue() != null) {
                @SuppressWarnings("unchecked")
                TableView<BaseComplaint> tv = (TableView<BaseComplaint>) selTab.getContent();
                BaseComplaint selC = tv.getSelectionModel().getSelectedItem();
                if (selC != null) {
                    selC.assignedToOfficerId = assignOpt.getValue();
                    new Alert(Alert.AlertType.INFORMATION, "Officer ID " + assignOpt.getValue() + " assigned!").show();
                }
            }
        });

        logoutBtn.setOnAction(e -> stage.setScene(LoginScreen.getScene(stage)));

        right.getChildren().addAll(new Label("Select Officer ID:"), assignOpt, assignBtn, ctznBtn, logoutBtn);
        root.setRight(right);

        HBox bot = new HBox(20);
        bot.setPadding(new Insets(10));
        Label obfLog = new Label("Obfuscated Log: " + PriorityCalculator.obfuscateLog("Admin Log In"));
        Label decLog = new Label("Decoded Log: " + PriorityCalculator.decodeLog(PriorityCalculator.obfuscateLog("Admin Log In")));
        bot.getChildren().addAll(obfLog, decLog);
        root.setBottom(bot);

        return new Scene(root, 1000, 700);
    }
    
    private static Tab createTab(String title, java.util.List<? extends BaseComplaint> items) {
        Tab t = new Tab(title);
        t.setClosable(false);
        TableView<BaseComplaint> tv = new TableView<>();
        
        TableColumn<BaseComplaint, Integer> idC = new TableColumn<>("ID");
        idC.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().complaintId));
        
        TableColumn<BaseComplaint, String> tC = new TableColumn<>("Title");
        tC.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().title));
        
        TableColumn<BaseComplaint, String> tgC = new TableColumn<>("Target");
        tgC.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().targetAgainst));
        
        TableColumn<BaseComplaint, Integer> uC = new TableColumn<>("Filed By (ID)");
        uC.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().filedByUserId));
        
        tv.getColumns().addAll(idC, tC, tgC, uC);
        
        @SuppressWarnings("unchecked")
        java.util.List<BaseComplaint> upcast = (java.util.List<BaseComplaint>) items;
        tv.getItems().addAll(upcast);
        
        t.setContent(tv);
        return t;
    }
}
