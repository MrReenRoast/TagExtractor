import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class TagExtractorGUI extends JFrame {
    private JTextArea textArea;
    private JLabel fileNameLabel;
    private File textFile;
    private Set<String> stopWords;

    public TagExtractorGUI() {
        setTitle("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // UI components
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        fileNameLabel = new JLabel("No file selected");

        JButton selectFileButton = new JButton("Select Text File");
        selectFileButton.addActionListener(e -> selectTextFile());

        JButton selectStopWordsButton = new JButton("Select Stop Words File");
        selectStopWordsButton.addActionListener(e -> loadStopWords());

        JButton extractTagsButton = new JButton("Extract Tags");
        extractTagsButton.addActionListener(e -> extractTags());

        JButton saveButton = new JButton("Save Tags to File");
        saveButton.addActionListener(e -> saveTagsToFile());

        // Layout
        JPanel topPanel = new JPanel();
        topPanel.add(selectFileButton);
        topPanel.add(selectStopWordsButton);
        topPanel.add(extractTagsButton);
        topPanel.add(saveButton);

        JPanel filePanel = new JPanel();
        filePanel.add(fileNameLabel);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(filePanel, BorderLayout.SOUTH);
    }

    // Method to select the main text file
    private void selectTextFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            textFile = fileChooser.getSelectedFile();
            fileNameLabel.setText("File selected: " + textFile.getName());
        }
    }

    // Method to select and load the stop words file
    private void loadStopWords() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File stopWordsFile = fileChooser.getSelectedFile();
            stopWords = new TreeSet<>();
            try {
                List<String> lines = Files.readAllLines(stopWordsFile.toPath());
                for (String line : lines) {
                    stopWords.add(line.trim().toLowerCase());
                }
                JOptionPane.showMessageDialog(this, "Stop words loaded successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error loading stop words file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Method to extract tags from the selected file
    private void extractTags() {
        if (textFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a text file first.", "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (stopWords == null) {
            JOptionPane.showMessageDialog(this, "Please select a stop words file first.", "No Stop Words File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Map<String, Integer> tagFrequency = new TreeMap<>();
        Pattern pattern = Pattern.compile("[^a-zA-Z]+");

        try (BufferedReader reader = new BufferedReader(new FileReader(textFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = pattern.split(line.toLowerCase());
                for (String word : words) {
                    if (!word.isEmpty() && !stopWords.contains(word)) {
                        tagFrequency.put(word, tagFrequency.getOrDefault(word, 0) + 1);
                    }
                }
            }

            displayTags(tagFrequency);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading the text file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayTags(Map<String, Integer> tagFrequency) {
        textArea.setText("");
        StringBuilder sb = new StringBuilder();
        tagFrequency.forEach((tag, frequency) -> sb.append(tag).append(": ").append(frequency).append("\n"));
        textArea.setText(sb.toString());
    }

    private void saveTagsToFile() {
        if (textArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tags to save. Please extract tags first.", "No Tags", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File saveFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
                writer.write(textArea.getText());
                JOptionPane.showMessageDialog(this, "Tags saved successfully.");
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving tags to file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TagExtractorGUI frame = new TagExtractorGUI();
            frame.setVisible(true);
        });
    }
}