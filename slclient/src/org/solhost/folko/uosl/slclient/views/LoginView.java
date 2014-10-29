package org.solhost.folko.uosl.slclient.views;

import org.solhost.folko.uosl.slclient.controllers.MainController;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class LoginView {
    private final MainController controller;
    private final Scene scene;
    private final Text notification;
    private final TextField hostField, nameField, passwordField;
    private final Button loginButton;
    private final StringProperty host, name, password;
    private final BooleanProperty busy;

    public LoginView(MainController controller) {
        this.controller = controller;
        this.busy = new SimpleBooleanProperty(false);
        GridPane grid = new GridPane();
        grid.add(new Label("Login to UO: Shattered Legacy"), 0, 0, 2, 1);

        hostField = new TextField();
        hostField.disableProperty().bind(busy);
        host = hostField.textProperty();
        grid.add(new Label("Server"), 0, 1);
        grid.add(hostField, 1, 1);

        nameField = new TextField();
        nameField.disableProperty().bind(busy);
        name = nameField.textProperty();
        grid.add(new Label("Name"), 0, 2);
        grid.add(nameField, 1, 2);

        passwordField = new TextField();
        passwordField.disableProperty().bind(busy);
        password = passwordField.textProperty();
        grid.add(new Label("Password"), 0, 3);
        grid.add(passwordField, 1, 3);

        loginButton = new Button("Login");
        loginButton.disableProperty().bind(busy);
        loginButton.setDefaultButton(true);
        HBox buttonBox = new HBox(loginButton);
        buttonBox.setAlignment(Pos.BASELINE_RIGHT);
        grid.add(buttonBox, 1, 4);

        ProgressIndicator wheel = new ProgressIndicator();
        wheel.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        wheel.visibleProperty().bind(busy);
        grid.add(wheel, 5, 0, 1, 5);

        notification = new Text();
        grid.add(notification, 0, 5, 2, 1);

        scene = new Scene(grid);

        setupLogic();
    }

    public void setBusy(boolean isBusy) {
        busy.set(isBusy);
    }

    private void setupLogic() {
        host.addListener(e -> notification.setText(""));
        name.addListener(e -> notification.setText(""));
        password.addListener(e -> notification.setText(""));
        loginButton.setOnAction(e -> controller.onLoginRequest(host.get(), name.get(), password.get()));
    }

    public void showInfo(String text) {
        notification.setFill(Color.FORESTGREEN);
        notification.setText(text);
    }

    public void showError(String text) {
        notification.setFill(Color.FIREBRICK);
        notification.setText(text);
    }

    public Scene getScene() {
        return scene;
    }
}
