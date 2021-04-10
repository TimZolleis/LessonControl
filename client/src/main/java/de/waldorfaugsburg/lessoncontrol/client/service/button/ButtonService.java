package de.waldorfaugsburg.lessoncontrol.client.service.button;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.Voicemeeter;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.VoicemeeterService;
import de.waldorfaugsburg.lessoncontrol.common.service.ButtonServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.Consumer;

public final class ButtonService extends AbstractService<ButtonServiceConfiguration> {

    private static final float DEFAULT_OPACITY = 0.2f;
    private static final float FULL_OPACITY = 1f;

    private final VoicemeeterService service;

    private JDialog dialog;
    private int pX;
    private int pY;

    private long clickStartedAt;

    public ButtonService(final LessonControlClientApplication application, final ButtonServiceConfiguration configuration) {
        super(application, configuration);
        this.service = getApplication().getServiceManager().getService(VoicemeeterService.class);
    }

    @Override
    public void enable() {
        dialog = new JDialog();
        dialog.setLayout(new GridLayout(1, getConfiguration().getStripButtons().size()));
        for (final ButtonServiceConfiguration.StripButton button : getConfiguration().getStripButtons()) {
            dialog.add(createStripButton(button));
        }
        dialog.add(createButton("autom. Stummschaltung", 15, true, service::setAntiHowlEnabled));
        dialog.setUndecorated(true);
        dialog.setResizable(false);
        dialog.setAlwaysOnTop(true);
        dialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setModal(true);
        dialog.pack();
        Scheduler.runLater(() -> dialog.setVisible(true), 0);
    }

    @Override
    public void disable(final boolean shutdown) {
        if (!shutdown) dialog.dispose();
    }

    private JToggleButton createStripButton(final ButtonServiceConfiguration.StripButton button) {
        return createButton(button.getLabel(), 30, button.isEnabled(),
                enabled -> Voicemeeter.setParameterFloat("Strip(" + button.getStrip() + ").Mute", enabled ? 0 : 1));
    }

    private JToggleButton createButton(final String label, final int textSize, final boolean enabled, final Consumer<Boolean> toggleListener) {
        final JToggleButton toggleButton = new JToggleButton(label, enabled);
        toggleButton.setFont(new Font("Arial", Font.BOLD, textSize));
        toggleButton.setContentAreaFilled(false);
        toggleButton.setOpaque(true);
        toggleButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent event) {
                clickStartedAt = System.currentTimeMillis();
                pX = event.getX();
                pY = event.getY();
            }

            @Override
            public void mouseReleased(final MouseEvent e) {
                // If delay is smaller than 200ms it's a button click, otherwise the window is dragged
                if (System.currentTimeMillis() - clickStartedAt <= 200) {
                    handleButtonAction(toggleButton, toggleListener);
                }
            }

            public void mouseDragged(final MouseEvent event) {
                dialog.setLocation(dialog.getLocation().x + event.getX() - pX, dialog.getLocation().y + event.getY() - pY);
            }

            @Override
            public void mouseEntered(final MouseEvent event) {
                dialog.setOpacity(FULL_OPACITY);
            }

            @Override
            public void mouseExited(final MouseEvent event) {
                dialog.setOpacity(DEFAULT_OPACITY);
            }
        });
        toggleButton.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(final MouseEvent event) {
                dialog.setLocation(dialog.getLocation().x + event.getX() - pX, dialog.getLocation().y + event.getY() - pY);
            }
        });
        handleButtonAction(toggleButton, toggleListener);
        return toggleButton;
    }

    private void handleButtonAction(final JToggleButton button, final Consumer<Boolean> consumer) {
        consumer.accept(button.isSelected());
        updateButtonColor(button);
    }

    private void updateButtonColor(final JToggleButton button) {
        button.setBackground(button.isSelected() ? Color.GREEN : Color.RED);
    }
}
