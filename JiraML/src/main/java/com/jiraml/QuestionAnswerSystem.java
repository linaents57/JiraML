package com.jiraml;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.text.similarity.CosineSimilarity;
import java.util.List;
import java.util.ArrayList; // If you are using ArrayList
import java.util.Map; // If you are using Maps for character frequency
import java.util.HashMap;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.*;

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
        // Load the question-answer dataset
        List<QAEntry> qaDataset = loadDataset("qa_dataset.json");

        if (qaDataset == null) {
            JOptionPane.showMessageDialog(null, "Failed to load the dataset.");
            return;
        }

        // Create and show GUI
        SwingUtilities.invokeLater(() -> createAndShowGUI(qaDataset));
    }

    public static void createAndShowGUI(List<QAEntry> qaDataset) {
        JFrame frame = new JFrame("CS Q&A System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new BorderLayout());

        JLabel promptLabel = new JLabel("Enter your question below:");
        promptLabel.setHorizontalAlignment(SwingConstants.CENTER);
        frame.add(promptLabel, BorderLayout.NORTH);

        JTextField questionField = new JTextField();
        frame.add(questionField, BorderLayout.CENTER);

        JButton askButton = new JButton("Ask");
        frame.add(askButton, BorderLayout.SOUTH);

        JTextArea answerArea = new JTextArea(5, 20);
        answerArea.setLineWrap(true);
        answerArea.setWrapStyleWord(true);
        answerArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(answerArea);
        frame.add(scrollPane, BorderLayout.EAST);

        askButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String userQuestion = questionField.getText();
                String bestAnswer = findBestAnswer(userQuestion, qaDataset);
                answerArea.setText("Answer: " + bestAnswer);
            }
        });

        frame.setVisible(true);
    }

    public static List<QAEntry> loadDataset(String filepath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_COMMENTS, true);
        try {
            // Load the file from the resources folder using the class loader
            File file = new File(Objects.requireNonNull(QuestionAnswerSystem.class.getClassLoader().getResource(filepath)).getFile());
            return Arrays.asList(mapper.readValue(file, QAEntry[].class));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String findBestAnswer(String userQuestion, List<QAEntry> qaDataset) {
        CosineSimilarity cosineSimilarity = new CosineSimilarity();
        double highestSimilarity = -1.0;
        String bestAnswer = "I'm sorry, I don't know the answer to that.";

        // Convert user question to character frequency map
        Map<CharSequence, Integer> userQuestionFreqMap = toCharFrequencyMap(userQuestion);

        for (QAEntry qaEntry : qaDataset) {
            // Convert each stored question to character frequency map
            Map<CharSequence, Integer> qaQuestionFreqMap = toCharFrequencyMap(qaEntry.question);

            // Compute similarity
            Double similarity = cosineSimilarity.cosineSimilarity(userQuestionFreqMap, qaQuestionFreqMap);
            if (similarity != null && similarity > highestSimilarity) {
                highestSimilarity = similarity;
                bestAnswer = qaEntry.answer;
            }
        }

        return bestAnswer;
    }

    private static Map<CharSequence, Integer> toCharFrequencyMap(String text) {
        Map<CharSequence, Integer> frequencyMap = new HashMap<>();
        for (char ch : text.toCharArray()) {
            frequencyMap.merge(String.valueOf(ch), 1, Integer::sum);
        }
        return frequencyMap;
    }
}

