// OOP CONCEPT : Separation of Concerns (GUI)
// ASSIGNMENT  : N/A
// PURPOSE     : Officer interface mapping specific departments to specific tables strictly anonymously.

package gui;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import users.Officer;
import store.DataStore;
import complaints.BaseComplaint;
import enums.OfficerDepartment;
import enums.Status;
import exceptions.ComplaintNotFoundException;
import search.ComplaintSearch;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.List;

public class OfficerDashboard {
    public static Scene getScene(Stage stage, Officer officer) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(15));

        // TOP
        HBox top = new HBox(20);
        Label title = new Label("Officer Dashboard - " + officer.department + " Division");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Button bell = new Button("🔔");
        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> stage.setScene(LoginScreen.getScene(stage)));
        top.getChildren().addAll(title, new Label("Logged in as: " + officer.username), bell, logoutBtn);
        root.setTop(top);

        // FILTER LOGIC
        DataStore store = DataStore.getInstance();
        List<BaseComplaint> myComplaints = new ArrayList<>();
        switch (officer.department) {
            case PWD:
                myComplaints.addAll(store.infraBox.getAll());
                myComplaints.addAll(store.waterSupplyBox.getAll());
                break;
            case ACB:
                myComplaints.addAll(store.corruptionBox.getAll());
                break;
            case MSEB:
                myComplaints.addAll(store.electricityBox.getAll());
                break;
            case LOCAL_POLICE:
                myComplaints.addAll(store.noiseBox.getAll());
                break;
            case MUNICIPAL_CORPORATION:
                myComplaints.addAll(store.sanitationBox.getAll());
                break;
            case TRAFFIC_POLICE:
                myComplaints.addAll(store.trafficBox.getAll());
                break;
        }

        // CENTER TABLE
        TableView<BaseComplaint> table = new TableView<>();
        
        TableColumn<BaseComplaint, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().complaintId));
        
        TableColumn<BaseComplaint, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().title));

        TableColumn<BaseComplaint, String> targetCol = new TableColumn<>("Target Being Reported");
        targetCol.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().targetAgainst));
        
        TableColumn<BaseComplaint, Integer> priorCol = new TableColumn<>("Priority Score");
        priorCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().priorityScore));
        
        TableColumn<BaseComplaint, Status> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cd -> new SimpleObjectProperty<>(cd.getValue().status));

        table.getColumns().addAll(idCol, titleCol, targetCol, priorCol, statusCol);
        table.setItems(FXCollections.observableArrayList(myComplaints));
        root.setCenter(table);

        // RIGHT PANE (DETAILS)
        VBox right = new VBox(10);
        right.setPadding(new Insets(10));
        right.setPrefWidth(250);
        
        // SEARCH BAR
        HBox searchBox = new HBox(5);
        TextField searchFld = new TextField(); 
        searchFld.setPromptText("Search ID / Target...");
        Button searchBtn = new Button("Search");
        Button resetBtn = new Button("Reset");
        searchBox.getChildren().addAll(searchFld, searchBtn, resetBtn);
        
        Label detTitle = new Label("Complaint Details");
        detTitle.setStyle("-fx-font-weight: bold;");
        Label lTarget = new Label("Target: N/A");
        Label lDesc = new Label("Description: N/A");
        
        ChoiceBox<Status> statusChoice = new ChoiceBox<>();
        statusChoice.getItems().addAll(Status.UNDER_REVIEW, Status.RESOLVED, Status.REJECTED);
        Button updateBtn = new Button("Update Status");

        right.getChildren().addAll(new Label("Polymorphic Search:"), searchBox, new Separator(), detTitle, lTarget, lDesc, new Label("Change Status:"), statusChoice, updateBtn);
        root.setRight(right);

        // SEARCH MECHANIC (POLYMORPHISM)
        ComplaintSearch engine = new ComplaintSearch();
        searchBtn.setOnAction(e -> {
            try {
                if (searchFld.getText().matches("\\d+")) {
                    table.setItems(FXCollections.observableArrayList(engine.search(Integer.parseInt(searchFld.getText()))));
                } else {
                    table.setItems(FXCollections.observableArrayList(engine.search(searchFld.getText())));
                }
            } catch (ComplaintNotFoundException ex) {
                new Alert(Alert.AlertType.ERROR, "No matches found!").show();
            }
        });
        resetBtn.setOnAction(e -> table.setItems(FXCollections.observableArrayList(myComplaints)));

        // ROW CLICK LISTENER
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                lTarget.setText("Target: " + newSel.targetAgainst);
                lDesc.setText("Desc: " + newSel.description);
                statusChoice.setValue(newSel.status);
            }
        });

        // UPDATE ACTION (NotificationThread Trigger + Exception Handling)
        updateBtn.setOnAction(e -> {
            BaseComplaint sel = table.getSelectionModel().getSelectedItem();
            if (sel != null && statusChoice.getValue() != null) {
                try {
                    sel.updateStatus(statusChoice.getValue());
                    store.notificationQueue.add("USERID:" + sel.filedByUserId + "|MSG:Complaint #" + sel.complaintId + " status updated to " + sel.status);
                    table.refresh();
                    new Alert(Alert.AlertType.INFORMATION, "Status Successfully Updated!").show();
                } catch (exceptions.ComplaintAlreadyResolvedException ex) {
                    new Alert(Alert.AlertType.ERROR, ex.getMessage()).show();
                }
            }
        });

        // Thread UI Sync
        threads.NotificationThread.uiCallback = () -> {
            int unread = store.userNotifications.containsKey(officer.userId) ? store.userNotifications.get(officer.userId).size() : 0;
            bell.setText("🔔 " + unread);
        };

        return new Scene(root, 900, 600);
    }
}
