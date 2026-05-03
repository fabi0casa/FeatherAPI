package util;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ClipboardUtil {
    public static void copy(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }
}