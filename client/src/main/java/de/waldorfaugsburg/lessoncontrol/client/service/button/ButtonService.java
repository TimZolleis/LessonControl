package de.waldorfaugsburg.lessoncontrol.client.service.button;

import de.waldorfaugsburg.lessoncontrol.client.LessonControlClientApplication;
import de.waldorfaugsburg.lessoncontrol.client.service.AbstractService;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.Voicemeeter;
import de.waldorfaugsburg.lessoncontrol.client.service.voicemeeter.VoicemeeterService;
import de.waldorfaugsburg.lessoncontrol.common.service.ButtonServiceConfiguration;
import de.waldorfaugsburg.lessoncontrol.common.util.Scheduler;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.function.BiConsumer;

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
        dialog.setLayout(new GridLayout(3, getConfiguration().getStripButtons().size()));
        for (final ButtonServiceConfiguration.StripButton button : getConfiguration().getStripButtons()) {
            dialog.add(createStripButton(button));
        }
        for (final ButtonServiceConfiguration.ProfileButton button : getConfiguration().getProfileButtons()) {
            dialog.add(createProfileButton(button));
        }
        final JToggleButton antiHowlButton = createButton("Rückkopplungsvermeidung", 15, true, (b, enabled) -> service.setAntiHowlEnabled(enabled));
        service.addAntiHowlListener(antiHowlButton::setBorderPainted);
        dialog.add(antiHowlButton);
        dialog.add(createButton("Störgeräuschvermeidung", 15, true, (b, enabled) -> service.setNoiseReductionEnabled(enabled)));
        dialog.setUndecorated(true);
        dialog.setOpacity(getConfiguration().isChangeOpacity() ? DEFAULT_OPACITY : FULL_OPACITY);
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
        final JToggleButton javaButton = createButton(button.getLabel(), 30, button.isEnabled(),
                (b, enabled) -> Voicemeeter.setParameterFloat("Strip(" + button.getStrip() + ").Mute", enabled ? 0 : 1));

        service.addMuteListener(button.getStrip(), muted -> javaButton.setBorderPainted(!muted));
        return javaButton;
    }

    private JToggleButton createProfileButton(final ButtonServiceConfiguration.ProfileButton button) {
        final JToggleButton profileButton = createButton(button.getLabel(), 30, true, (b, enabled) -> {
            b.setBackground(Color.CYAN);
            Voicemeeter.setParameterFloat("Strip(2).EQGain1", button.getBass());
            Voicemeeter.setParameterFloat("Strip(2).EQGain2", button.getMedium());
            Voicemeeter.setParameterFloat("Strip(2).EQGain3", button.getHigh());
            Voicemeeter.setParameterFloat("Strip(2).Gain", button.getGain());
        });
        profileButton.setBackground(Color.CYAN);
        return profileButton;
    }

    private JToggleButton createButton(final String label, final int textSize, final boolean enabled, final BiConsumer<JToggleButton, Boolean> toggleListener) {
        final JToggleButton toggleButton = new JToggleButton(label, enabled);
        toggleButton.setFont(new Font("Arial", Font.BOLD, textSize));
        toggleButton.setBorder(new LineBorder(Color.YELLOW, 10, false));
        toggleButton.setBorderPainted(false);
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
                if (getConfiguration().isChangeOpacity()) dialog.setOpacity(FULL_OPACITY);
            }

            @Override
            public void mouseExited(final MouseEvent event) {
                if (getConfiguration().isChangeOpacity()) dialog.setOpacity(DEFAULT_OPACITY);
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

    private void handleButtonAction(final JToggleButton button, final BiConsumer<JToggleButton, Boolean> consumer) {
        updateButtonColor(button);
        consumer.accept(button, button.isSelected());
    }

    private void updateButtonColor(final JToggleButton button) {
        button.setBackground(button.isSelected() ? Color.GREEN : Color.RED);
    }
}
