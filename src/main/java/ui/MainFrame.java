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
    private DefaultListModel<HistoryEntry> historyModel;
    private JList<HistoryEntry> historyList;

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

        historyModel = new DefaultListModel<>();
        historyList = new JList<>(historyModel);
        historyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                HistoryEntry selected = historyList.getSelectedValue();
                if (selected != null) {
                    responseArea.setText(selected.response);
                }
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

        // Split vertical entre Body e Resposta
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, bottom);
        verticalSplit.setDividerLocation(250);
        verticalSplit.setResizeWeight(0.5);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel(" Histórico"), BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        // Split horizontal entre a área principal e o Histórico
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, historyPanel);
        mainSplit.setDividerLocation(650);
        mainSplit.setResizeWeight(0.8);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
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
            historyModel.insertElementAt(new HistoryEntry(requestInfo, fullResponse), 0);

        } catch (Exception ex) {
            responseArea.setText("Erro: " + ex.getMessage());
        }
    }

}
