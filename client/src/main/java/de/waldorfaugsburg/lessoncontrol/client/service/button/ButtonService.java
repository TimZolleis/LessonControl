package de.waldorfaugsburg.lessoncontrol.client.service.button;

import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.common.service.ButtonServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import javax.swing.*;
import java.awt.*;

public final class ButtonService extends AbstractService<ButtonServiceConfiguration> {

    private JDialog dialog;

    public ButtonService(final ButtonServiceConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void enable() {
        dialog = new JDialog();
        dialog.setLayout(new GridLayout(getConfiguration().getRows(), getConfiguration().getColumns()));

        for (final ButtonServiceConfiguration.Button button : getConfiguration().getButtons()) {
            dialog.add(createButton(button));
        }

        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setModal(true);
        dialog.pack();

        Scheduler.runLater(() -> dialog.setVisible(true), 0);
    }

    @Override
    public void disable() {
        dialog.dispose();
    }

    private JToggleButton createButton(final ButtonServiceConfiguration.Button button) {
        final JToggleButton toggleButton = new JToggleButton(button.getText(), button.isEnabled());
        toggleButton.setFont(new Font("Arial", Font.BOLD, button.getTextSize()));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setOpaque(true);
        return toggleButton;
    }
}
