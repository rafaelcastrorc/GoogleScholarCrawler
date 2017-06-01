import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rafaelcastro on 5/31/17.
 */
public class View {
    private JButton Crawl;
    JPanel viewPanel;
    private JLabel instructions;
    JTextField mainTextField;
    JFormattedTextField numberOfResults;
    private JLabel instuctions2;
    JButton search;
    private JTextArea outputTextField;
    JLabel resultText;
    private JButton downloadButton;
    private JLabel downloadedLabel;
    String title ="";
    Crawler crawler;


    View(Crawler crawler) {
        this.crawler = crawler;

        mainTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        numberOfResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    void displayError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }


    public void displayMessage(String s) {
    }

    void addSearchListener(ActionListener pressed) {
        search.addActionListener(pressed);
    }

    void addDownloadListener(ActionListener pressed) {
        downloadButton.addActionListener(pressed);
    }


    void updateDownloadField(int numOfPDFs) {
        StringBuilder sb = new StringBuilder();
        downloadedLabel.setText(sb.append("PDFs downloaded : ").append(numOfPDFs).toString());

    }

    public void updateOutputTextField(String outputText) {
        outputTextField.setText(outputText);
    }
}
