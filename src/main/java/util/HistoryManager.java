package util;

import java.util.*;

public class HistoryManager {
    private static final List<String> history = new ArrayList<>();

    public static void add(String entry) {
        history.add(0, entry);
    }

    public static List<String> get() {
        return history;
    }
}