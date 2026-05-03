package ui;

import service.ApiService;
import model.RequestData;
import util.*;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;

public class MainFrame extends JFrame {

    private JTextField urlField;
    private JComboBox<String> methodBox;
    private JTextArea bodyArea;
    private JTextArea responseArea;
    private JTextArea headersArea;
    private JComboBox<String> historyBox;

    private final ApiService api = new ApiService();

    public MainFrame() {
        setTitle("Mini Postman Java");
        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        urlField = new JTextField("https://viacep.com.br/ws/01001000/json/");
        methodBox = new JComboBox<>(new String[]{"GET","POST","PUT","DELETE"});

        bodyArea = new JTextArea();
        responseArea = new JTextArea();
        headersArea = new JTextArea("Content-Type: application/json");

        responseArea.setEditable(false);

        JButton sendBtn = new JButton("Enviar");
        JButton copyBtn = new JButton("Copiar resposta");

        historyBox = new JComboBox<>();

        sendBtn.addActionListener(e -> send());
        copyBtn.addActionListener(e -> ClipboardUtil.copy(responseArea.getText()));

        JPanel top = new JPanel(new BorderLayout());
        top.add(methodBox, BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(sendBtn, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Body", new JScrollPane(bodyArea));
        tabs.add("Headers", new JScrollPane(headersArea));

        JPanel center = new JPanel(new BorderLayout());
        center.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new JScrollPane(responseArea), BorderLayout.CENTER);
        bottom.add(copyBtn, BorderLayout.SOUTH);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel("Histórico"), BorderLayout.NORTH);
        historyPanel.add(historyBox, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(center, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        add(historyPanel, BorderLayout.EAST);
    }

    private void send() {
        try {
            HashMap<String,String> headers = new HashMap<>();

            for (String line : headersArea.getText().split("\n")) {
                if (line.contains(":")) {
                    String[] parts = line.split(":",2);
                    headers.put(parts[0].trim(), parts[1].trim());
                }
            }

            RequestData data = new RequestData(
                    urlField.getText(),
                    (String) methodBox.getSelectedItem(),
                    bodyArea.getText(),
                    headers
            );

            var response = api.send(data);

            String formatted = JsonFormatter.format(response.body());

            responseArea.setText("Status: " + response.statusCode() + "\n\n" + formatted);

            HistoryManager.add(data.method + " " + data.url);
            historyBox.addItem(data.method + " " + data.url);

        } catch (Exception ex) {
            responseArea.setText("Erro: " + ex.getMessage());
        }
    }
}