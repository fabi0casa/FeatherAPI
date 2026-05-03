package util;

import model.HistoryEntry;
import java.util.*;

public class HistoryManager {
    private static final List<HistoryEntry> history = new ArrayList<>();

    public static void add(String request, String response) {
        history.add(0, new HistoryEntry(request, response));
    }

    public static List<HistoryEntry> get() {
        return history;
    }
    
    public static HistoryEntry getEntry(int index) {
        if (index >= 0 && index < history.size()) {
            return history.get(index);
        }
        return null;
    }
}
