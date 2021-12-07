package de.waldorfaugsburg.lessoncontrol.client.util;

import javax.swing.*;
import java.awt.*;

public final class DialogUtil {

    private static JFrame dummyFrame;

    public static void openErrorDialog(final String title, final String message) {
        ((Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation")).run();
        SwingUtilities.invokeLater(() -> {
            final JFrame frame = new JFrame();
            frame.setAlwaysOnTop(true);
            JOptionPane.showMessageDialog(frame, message + "\n\nBitte wenden Sie sich umgehend an den IT-Support!", title, JOptionPane.ERROR_MESSAGE);
        });
    }

    public static boolean openYesNoQuestionDialog(final String title, final String message) {
        ((Runnable) Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.exclamation")).run();

        if (dummyFrame == null) {
            dummyFrame = new JFrame();
            dummyFrame.setLocationRelativeTo(null);
            dummyFrame.setAlwaysOnTop(true);
        }

        SwingUtilities.invokeLater(() -> dummyFrame.setVisible(true));
        final int result = JOptionPane.showConfirmDialog(dummyFrame, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        dummyFrame.dispose();

        return result == JOptionPane.YES_OPTION;
    }

}
