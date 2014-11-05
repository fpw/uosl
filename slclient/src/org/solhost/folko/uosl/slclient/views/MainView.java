package org.solhost.folko.uosl.slclient.views;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;

import org.solhost.folko.uosl.slclient.controllers.MainController;

public class MainView extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final String WINDOW_TITLE = "Ultima Online: Shattered Legacy";
    private static final int DEFAULT_WIDTH  = 800;
    private static final int DEFAULT_HEIGHT = 600;
    private static final int FPS_LIMIT = 20;

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

        setupMenu();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                controller.onGameWindowClosed();
            }
        });
    }

    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener((a) -> controller.onGameWindowClosed());
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        JMenu optionsMenu = new JMenu("Options");
        JCheckBoxMenuItem fpsItem = new JCheckBoxMenuItem(String.format("Limit frame rate to %d FPS", FPS_LIMIT), true);
        fpsItem.addActionListener((a) -> controller.setFrameLimit(fpsItem.getState() ? FPS_LIMIT : 0));
        gameView.setFrameLimit(fpsItem.getState() ? FPS_LIMIT : 0);
        optionsMenu.add(fpsItem);

        JCheckBoxMenuItem musicItem = new JCheckBoxMenuItem("Music", true);
        musicItem.addActionListener((a) -> controller.setEnableMusic(musicItem.getState()));
        controller.setEnableMusic(musicItem.getState());
        optionsMenu.add(musicItem);

        menuBar.add(optionsMenu);

        setJMenuBar(menuBar);
    }

    public GameView getGameView() {
        return gameView;
    }

    public void showLoginDialog() {
        SwingUtilities.invokeLater(() -> {
            loginDialog.setVisible(true);
        });
    }

    public LoginDialog getLoginView() {
        return loginDialog;
    }

    public void setTitleSuffix(String string) {
        SwingUtilities.invokeLater(() -> setTitle(WINDOW_TITLE + " " + string));
    }

    public void close() {
        SwingUtilities.invokeLater(() -> dispose());
    }
}
