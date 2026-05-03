package ui;

import javax.swing.*;
import java.awt.*;

public class LightTheme {
    public static void apply() {
        UIManager.put("control", new Color(240, 240, 240));
        UIManager.put("info", new Color(240, 240, 240));
        UIManager.put("nimbusBase", new Color(200, 200, 200));
        UIManager.put("nimbusAlertYellow", new Color(248, 187, 0));
        UIManager.put("nimbusDisabledText", new Color(100, 100, 100));
        UIManager.put("nimbusFocus", new Color(115, 164, 209));
        UIManager.put("nimbusGreen", new Color(176, 179, 50));
        UIManager.put("nimbusInfoBlue", new Color(66, 139, 221));
        UIManager.put("nimbusLightBackground", new Color(255, 255, 255));
        UIManager.put("nimbusOrange", new Color(191, 98, 4));
        UIManager.put("nimbusRed", new Color(169, 46, 34));
        UIManager.put("nimbusSelectedText", new Color(0, 0, 0));
        UIManager.put("nimbusSelectionBackground", new Color(184, 207, 229));
        UIManager.put("text", new Color(0, 0, 0));
        
        // Additional component colors
        UIManager.put("Panel.background", new Color(240, 240, 240));
        UIManager.put("TextArea.background", Color.WHITE);
        UIManager.put("TextArea.foreground", Color.BLACK);
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", Color.BLACK);
        UIManager.put("ComboBox.background", Color.WHITE);
        UIManager.put("ComboBox.foreground", Color.BLACK);
        UIManager.put("Button.background", new Color(225, 225, 225));
        UIManager.put("Button.foreground", Color.BLACK);
    }
}