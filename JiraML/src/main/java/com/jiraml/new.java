import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.CosineSimilarity;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class QuestionAnswerSystem {

    static class QAEntry {
        public String question;
        public String answer;

        public QAEntry() {}

        public QAEntry(String question, String answer) {
            this.question = question;
            this.answer = answer;
        }
    }

    public static void main(String[] args) {
        List<QAEntry> qaDataset = loadDataset("src/main/resources/qa_dataset.json");

        if (qaDataset == null) {
            System.out.println("Failed to load the dataset.");
            return;
        }

        JFrame frame = new JFrame("CS Q&A System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        JTextField questionField = new JTextField(20);
        JButton askButton = new JButton("Ask");
        JLabel answerLabel = new JLabel("Answer will appear here.");

        JPanel panel = new JPanel();
        panel.add(new JLabel("Enter your question:"));
        panel.add(questionField);
        panel.add(askButton);
        panel.add(answerLabel);

        frame.add(panel);
        frame.setVisible(true);

        askButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userQuestion = questionField.getText();
                try {
                    // Send the user question and dataset to the Python API
                    int bestMatchIndex = findBestMatch(userQuestion, qaDataset);

                    if (bestMatchIndex != -1) {
                        String bestAnswer = qaDataset.get(bestMatchIndex).answer;
                        answerLabel.setText("Answer: " + bestAnswer);
                    } else {
                        answerLabel.setText("Sorry, no matching question found.");
                    }

                } catch (Exception ex) {
                    ex.printStackTrace();
                    answerLabel.setText("Error occurred while finding the answer.");
                }
            }
        });
    }

    // Method to load dataset from JSON file
    public static List<QAEntry> loadDataset(String filepath) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return Arrays.asList(mapper.readValue(new File(filepath), QAEntry[].class));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Method to send HTTP request to the Python API and get the best match index
    public static int findBestMatch(String userQuestion, List<QAEntry> qaDataset) throws IOException, InterruptedException {
        // Create HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Prepare dataset questions as a list of strings
        List<String> datasetQuestions = new ArrayList<>();
        for (QAEntry entry : qaDataset) {
            datasetQuestions.add(entry.question);
        }

        // Create JSON payload for the POST request
        Map<String, Object> jsonPayload = new HashMap<>();
        jsonPayload.put("user_question", userQuestion);
        jsonPayload.put("dataset_questions", datasetQuestions);

        // Convert JSON payload to string
        ObjectMapper objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(jsonPayload);

        // Create an HTTP request to the Python API
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/find-best-match"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Send the request and get the response
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Parse the response JSON to get the best match index
        Map<String, Integer> responseJson = objectMapper.readValue(response.body(), Map.class);
        return responseJson.get("best_match_index");
    }
}
