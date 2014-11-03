package org.solhost.folko.uosl.slclient.views;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.solhost.folko.uosl.slclient.controllers.MainController;

public class MainView extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WINDOW_TITLE = "Ultima Online: Shattered Legacy";
    private static final int DEFAULT_WIDTH  = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private final MainController controller;
    private final GameView gameView;
    private final LoginDialog loginDialog;

    public MainView(MainController mainController) {
        super(WINDOW_TITLE);

        this.controller = mainController;
        this.loginDialog = new LoginDialog(this, controller);
        this.gameView = new GameView(controller);

        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(gameView, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.onGameWindowClosed();
            }
        });
    }

    public GameView getGameView() {
        return gameView;
    }

    public void showLoginDialog() {
        loginDialog.setVisible(true);
    }

    public LoginDialog getLoginView() {
        return loginDialog;
    }

    public void setTitleSuffix(String string) {
        SwingUtilities.invokeLater(() -> setTitle(WINDOW_TITLE + " " + string));
    }
}
