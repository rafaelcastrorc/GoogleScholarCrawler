import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rafaelcastro on 5/31/17.
 */
class Controller extends Observer {
    private Crawler crawler;
    private String title;
    private int numOfResults;
    private View view;
    boolean search = false;

    Controller(Crawler crawler, View view, boolean displayGUI) {

        this.crawler = crawler;
        this.crawler.attach(this);
        this.view = view;
        if (displayGUI) {
            view.addSearchListener(new SearchListener());
            view.addDownloadListener(new DownloadListener());
        }
    }

    void search() {
        search = true;
        try {
            view.displayMessage("Searching...");
            crawler.searchForArticle(title, numOfResults);
            //Todo: Check if there was more than one result

            String numberOfCitations  = crawler.getNumberOfCitations() + " different papers";
            if (numberOfCitations.equals("Provide feedback")) {
                numberOfCitations = "Could not find paper";
            }
            view.resultText.setText(numberOfCitations);
        } catch (Exception e) {
            view.displayError(e.getMessage());
        }


    }

    void getNumOfResults(int i) {
        if (title.isEmpty()) {
            //Todo: throw error;
        } else {
            this.numOfResults = i;
        }
    }

    @Override
    protected void update() {

        int pdfCounter = crawler.getPDFDownloadState();
        String outputText = crawler.getTextState();
        view.updateDownloadField(pdfCounter);
        view.updateOutputTextField(outputText);
    }


    class SearchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = view.mainTextField.getText();
            if (text.isEmpty()) {
                view.displayError("Please write a title");
            } else {
                title = text;
                JOptionPane.showMessageDialog(null, "Loading...");
                search();
            }
        }
    }

    class DownloadListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = view.numberOfResults.getText();
            Pattern numbersOnly = Pattern.compile("^[0-9]+$");
            Matcher matcher = numbersOnly.matcher(text);
            if (!search) {
                view.displayError("Please search for a paper before pressing download.");
            } else {
                if (matcher.find()) {
                    try {
                        int timeToWait = crawler.getTimeToWait();
                        crawler.setTextState("Waiting " + timeToWait + " seconds before searching...", false);
                        crawler.getPDFs(Integer.parseInt(matcher.group()));
                    } catch (Exception e1) {
                        view.displayError(e1.getMessage());
                    }
                } else {
                    view.displayError("Please only write numbers here.");
                }
            }
        }

    }






}
