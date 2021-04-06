package de.waldorfaugsburg.lessoncontrol.client.splash;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkClient;
import de.waldorfaugsburg.lessoncontrol.client.network.NetworkState;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import javax.swing.*;
import java.awt.*;

public final class SplashManager {

    private final LessonControlClientApplication application;

    private JDialog dialog;
    private JLabel label;

    public SplashManager(final LessonControlClientApplication application) {
        this.application = application;
        registerReceivers();
    }

    private void registerReceivers() {
        final NetworkClient networkClient = application.getNetworkClient();
        networkClient.addListener(state -> {
            if (state == NetworkState.CONNECTING || state == NetworkState.CONNECTED || state == NetworkState.READY) {
                splash(state.getMessage());
                if (state == NetworkState.READY) {
                    Scheduler.runLater(this::dispose, 5000);
                }
            }
        });
    }

    private void splash(final String text) {
        if (dialog != null) {
            label.setText(text);
            return;
        }

        dialog = new JDialog();

        final JLabel image = new JLabel(new ImageIcon(getClass().getResource("/splash.png")));
        image.setLayout(new BorderLayout());

        label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 20));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(180, 50, 0, 50));
        image.add(label);
        dialog.add(image);

        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setSize(600, 300);
        dialog.setAlwaysOnTop(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setModal(true);
        dialog.setLocationRelativeTo(null);

        Scheduler.runLater(() -> dialog.setVisible(true), 0);
    }

    private void dispose() {
        dialog.dispose();
        dialog = null;
    }
}