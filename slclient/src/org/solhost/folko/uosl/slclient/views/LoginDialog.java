package org.solhost.folko.uosl.slclient.views;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.solhost.folko.uosl.slclient.controllers.MainController;

public class LoginDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private final MainController controller;
    private final JTextField hostField, nameField, passwordField;

    public LoginDialog(JFrame parent, MainController controller) {
        super(parent, "Login");
        this.controller = controller;

        setLayout(new GridLayout(4, 2));

        setMinimumSize(new Dimension(200, 0));

        add(new JLabel("Host"));
        hostField = new JTextField();
        add(hostField);

        add(new JLabel("Name"));
        nameField = new JTextField();
        add(nameField);

        add(new JLabel("Password"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener((x) -> controller.onGameWindowClosed());
        add(quitButton);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener((x) -> onLogin());
        add(loginButton);

        getRootPane().setDefaultButton(loginButton);

        pack();
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setModal(true);
        setLocationRelativeTo(parent);
    }

    public void onLogin() {
        controller.onLoginRequest(hostField.getText(), nameField.getText(), passwordField.getText());
    }

    public void setBusy(boolean busy) {
        SwingUtilities.invokeLater(() -> {
            hostField.setEnabled(!busy);
            nameField.setEnabled(!busy);
            passwordField.setEnabled(!busy);
        });
    }

    public void close() {
        SwingUtilities.invokeLater(() -> setVisible(false));
    }

    public void showError(String error) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE));
    }
}
