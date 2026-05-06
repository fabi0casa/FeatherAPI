package ui;

import service.ApiService;
import model.RequestData;
import model.HistoryEntry;
import util.*;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;

public class MainFrame extends JFrame {

    private JTextField urlField;
    private JComboBox<String> methodBox;
    private JTextArea bodyArea;
    private JTextArea responseArea;
    private JTextArea headersArea;
    private JComboBox<HistoryEntry> historyBox;

    private final ApiService api = new ApiService();

    public MainFrame() {
        setTitle("Feather API");

        ImageIcon icon = new ImageIcon(getClass().getResource("/feather.png"));
        setIconImage(icon.getImage());

        setSize(900,600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        urlField = new JTextField("https://viacep.com.br/ws/01001000/json/");
        methodBox = new JComboBox<>(new String[]{"GET","POST","PUT","DELETE","PATCH"});

        bodyArea = new JTextArea();
        setupEditor(bodyArea);

        responseArea = new JTextArea();
        headersArea = new JTextArea("Content-Type: application/json");
        setupEditor(headersArea);

        responseArea.setEditable(false);

        JButton sendBtn = new JButton("Enviar");
        JButton copyBtn = new JButton("Copiar resposta");

        historyBox = new JComboBox<>();
        historyBox.addActionListener(e -> {
            HistoryEntry selected = (HistoryEntry) historyBox.getSelectedItem();
            if (selected != null) {
                responseArea.setText(selected.response);
            }
        });

        sendBtn.addActionListener(e -> send());
        copyBtn.addActionListener(e -> ClipboardUtil.copy(responseArea.getText()));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(sendBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(methodBox, BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(buttonPanel, BorderLayout.EAST);

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

    private void setupEditor(JTextArea textArea) {
        // Tab -> 4 spaces
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume();
                    textArea.replaceSelection("    ");
                }
            }
        });

        // Undo / Redo
        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));

        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "Undo");
        textArea.getActionMap().put("Undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });

        textArea.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "Redo");
        textArea.getActionMap().put("Redo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) undoManager.redo();
            }
        });
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
            String fullResponse = "Status: " + response.statusCode() + "\n\n" + formatted;

            responseArea.setText(fullResponse);

            String requestInfo = data.method + " " + data.url;
            HistoryManager.add(requestInfo, fullResponse);
            historyBox.addItem(new HistoryEntry(requestInfo, fullResponse));

        } catch (Exception ex) {
            responseArea.setText("Erro: " + ex.getMessage());
        }
    }

}
