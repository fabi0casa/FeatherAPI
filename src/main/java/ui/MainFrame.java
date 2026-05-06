package ui;

import service.ApiService;
import model.RequestData;
import model.HistoryEntry;
import util.*;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.HashMap;

public class MainFrame extends JFrame {

    private JTextField urlField;
    private JComboBox<String> methodBox;
    private RSyntaxTextArea bodyArea;
    private RSyntaxTextArea responseArea;
    private JTextArea headersArea;
    private DefaultListModel<HistoryEntry> historyModel;
    private JList<HistoryEntry> historyList;
    private JCheckBox darkThemeBox;

    private final ApiService api = new ApiService();

    public MainFrame() {
        setTitle("Feather API");

        ImageIcon icon = new ImageIcon(getClass().getResource("/feather.png"));
        setIconImage(icon.getImage());

        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        urlField = new JTextField("https://viacep.com.br/ws/01001000/json/");
        methodBox = new JComboBox<>(new String[]{"GET", "POST", "PUT", "DELETE", "PATCH"});

        bodyArea = new RSyntaxTextArea();
        bodyArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        bodyArea.setCodeFoldingEnabled(true);
        setupEditor(bodyArea);

        responseArea = new RSyntaxTextArea();
        responseArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        responseArea.setEditable(false);
        setupEditor(responseArea);

        headersArea = new JTextArea("Content-Type: application/json");
        // Para headers mantemos JTextArea simples ou configuramos sem numeração de linha
        // mas vamos aplicar o setupEditor para o Undo/Redo

        JButton sendBtn = new JButton("Enviar");
        JButton copyBtn = new JButton("Copiar resposta");

        darkThemeBox = new JCheckBox("Tema Escuro");
        darkThemeBox.addActionListener(e -> applyTheme(darkThemeBox.isSelected()));

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
        buttonPanel.add(darkThemeBox);
        buttonPanel.add(sendBtn);

        JPanel top = new JPanel(new BorderLayout());
        top.add(methodBox, BorderLayout.WEST);
        top.add(urlField, BorderLayout.CENTER);
        top.add(buttonPanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.add("Body", new RTextScrollPane(bodyArea));
        tabs.add("Headers", new JScrollPane(headersArea));

        JPanel center = new JPanel(new BorderLayout());
        center.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(new RTextScrollPane(responseArea), BorderLayout.CENTER);
        bottom.add(copyBtn, BorderLayout.SOUTH);

        // Split vertical entre Body e Resposta
        JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, center, bottom);
        verticalSplit.setDividerLocation(300);
        verticalSplit.setResizeWeight(0.5);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.add(new JLabel(" Histórico"), BorderLayout.NORTH);
        historyPanel.add(new JScrollPane(historyList), BorderLayout.CENTER);

        // Split horizontal entre a área principal e o Histórico
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, verticalSplit, historyPanel);
        mainSplit.setDividerLocation(750);
        mainSplit.setResizeWeight(0.8);

        setLayout(new BorderLayout());
        add(top, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);

        // Aplicar Undo/Redo no headersArea que não é RSyntaxTextArea
        setupHeadersEditor(headersArea);
    }

    private void setupEditor(RSyntaxTextArea textArea) {
        textArea.setTabSize(4);
        textArea.setTabsEmulated(true); // Converte TAB em espaços automaticamente

        // Undo / Redo já é nativo no RSyntaxTextArea, mas podemos reforçar atalhos se necessário
        // No RSyntaxTextArea, Ctrl+Z e Ctrl+Y funcionam por padrão.
    }

    private void setupHeadersEditor(JTextArea textArea) {
        UndoManager undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(e -> undoManager.addEdit(e.getEdit()));
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");
        textArea.getActionMap().put("Undo", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) undoManager.undo();
            }
        });
    }

    private void applyTheme(boolean dark) {
        try {
            if (dark) {
                FlatDarkLaf.setup();
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                theme.apply(bodyArea);
                theme.apply(responseArea);
            } else {
                FlatLightLaf.setup();
                Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
                theme.apply(bodyArea);
                theme.apply(responseArea);
            }
            SwingUtilities.updateComponentTreeUI(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void send() {
        try {
            HashMap<String, String> headers = new HashMap<>();

            for (String line : headersArea.getText().split("\n")) {
                if (line.contains(":")) {
                    String[] parts = line.split(":", 2);
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
