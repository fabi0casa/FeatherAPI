import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.http.*;

public class ApiTester extends JFrame {

    private JTextField urlField;
    private JComboBox<String> methodBox;
    private JTextArea bodyArea;
    private JTextArea responseArea;

    private HttpClient client = HttpClient.newHttpClient();

    public ApiTester() {
        setTitle("Mini API Tester");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        urlField = new JTextField("http://localhost:5191/api/test");
        methodBox = new JComboBox<>(new String[]{"GET", "POST"});
        bodyArea = new JTextArea(5, 40);
        responseArea = new JTextArea(15, 40);
        responseArea.setEditable(false);

        JButton sendButton = new JButton("Enviar");

        sendButton.addActionListener(e -> sendRequest());

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(methodBox, BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Body (JSON):"), BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(sendButton, BorderLayout.NORTH);
        bottomPanel.add(new JScrollPane(responseArea), BorderLayout.CENTER);

        setLayout(new BorderLayout());
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void sendRequest() {
        try {
            String url = urlField.getText();
            String method = (String) methodBox.getSelectedItem();

            HttpRequest request;

            if ("POST".equals(method)) {
                request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(bodyArea.getText()))
                        .build();
            } else {
                request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
            }

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            responseArea.setText(
                    "Status: " + response.statusCode() + "\n\n" + response.body()
            );

        } catch (Exception ex) {
            responseArea.setText("Erro: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ApiTester().setVisible(true));
    }
}