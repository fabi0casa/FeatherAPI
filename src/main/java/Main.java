import javax.swing.*;
import ui.MainFrame;
import ui.DarkTheme;

public class Main {
    public static void main(String[] args) {
        DarkTheme.apply();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}