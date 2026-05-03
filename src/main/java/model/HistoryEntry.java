package model;

public class HistoryEntry {
    public String request;
    public String response;
    
    public HistoryEntry(String request, String response) {
        this.request = request;
        this.response = response;
    }
    
    @Override
    public String toString() {
        return request;
    }
}