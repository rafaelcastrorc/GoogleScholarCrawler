import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by rafaelcastro on 5/31/17.
 * Displays the information
 */
public class View {
    JPanel viewPanel;
    JTextField mainTextField;
    JFormattedTextField numberOfResults;
    JButton search;
    JTextArea outputTextField;
    JLabel resultText;
    JLabel requestLabel;
    private JButton downloadButton;
    private JLabel downloadedLabel;


    /**
     * Constructor.
     */
    View() {

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

    /**
     * Display error as pop up message
     *
     * @param message String with error message
     */
    void displayError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Adds a listener for the search button
     *
     * @param pressed ActionListener
     */
    void addSearchListener(ActionListener pressed) {
        search.addActionListener(pressed);
    }

    /**
     * Adds a listener for the download button
     *
     * @param pressed ActionListener
     */
    void addDownloadListener(ActionListener pressed) {
        downloadButton.addActionListener(pressed);
    }


    /**
     * Updates the download field tag to show the number of PDFs downloaded
     *
     * @param numOfPDFs String with the number of PDFs downloaded
     */
    void updateDownloadField(String numOfPDFs) {
        StringBuilder sb = new StringBuilder();
        downloadedLabel.setText(sb.append(numOfPDFs).toString());

    }

    /**
     * Updates the text area content
     *
     * @param outputText String with the text that wants to be displayed
     */
    public void updateOutputTextField(String outputText) {
        outputTextField.setText(outputText);
    }

}
