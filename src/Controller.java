import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rafaelcastro on 5/31/17.
 * Controls the View and the Crawler class
 */
class Controller extends Observer {
    private Crawler crawler;
    private String title;
    private View view;
    private boolean hasSearchedBefore = false;
    private Output outputScreenWorker;
    private Labels label;
    private String numOfPDFToDownload;


    Controller(Crawler crawler, View view, boolean displayGUI) {
        this.crawler = crawler;
        this.crawler.attach(this);
        this.view = view;
        if (displayGUI) {
            view.addSearchListener(new SearchListener());
            view.addDownloadListener(new DownloadListener());
        }
    }

    /**
     * Performs all the operations to search for a paper in Google Scholar
     */
    private void search() {
        try {
            outputScreenWorker.updateOutputText("Searching...");

            crawler.searchForArticle(title, hasSearchedBefore);

            String numberOfCitations = crawler.getNumberOfCitations();
            if (numberOfCitations.isEmpty() || numberOfCitations.equals("Provide feedback")) {
                numberOfCitations = "Could not find paper";
                hasSearchedBefore = false;
            } else if (numberOfCitations.equals("There was more than 1 result found for your given query")) {
                numberOfCitations = "ERROR: There was more than 1 result found for your given query";
            } else {
                numberOfCitations = numberOfCitations + " different papers";
                hasSearchedBefore = true;

            }
            view.resultText.setText(numberOfCitations);
        } catch (Exception e) {
            view.displayError(e.getMessage());

        }
    }


    /**
     * If the subject changes state, then update the corresponding information in the view
     */
    @Override
    protected void update() {

        String outputText = crawler.getTextState();
        //In case this objects have not been initialized  yet
        if (outputScreenWorker == null && label == null) {
            view.updateOutputTextField(outputText);
        } else {
            if (outputText.contains("Number of requests")) {
                if (label == null) {
                    view.requestLabel.setText(outputText);
                } else {
                    label.updateOutputText("numOfRequests", outputText);
                }
            } else if (outputText.contains("Downloaded")) {
                label.updateOutputText("numOfDownloads", outputText);

            } else {
                //If it is not a tag, then output it to the text area
                outputScreenWorker.updateOutputText(outputText);
            }
        }
    }


    /**
     * Listens to a button press event on the search button located in the view class
     */
    class SearchListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = view.mainTextField.getText();
            if (text.isEmpty()) {
                view.displayError("Please write a title");
            } else {
                title = text;
                //Display loading message dialog
                JOptionPane.showMessageDialog(null, "Loading...");
                outputScreenWorker = new Output();
                outputScreenWorker.execute();

            }
        }
    }

    /**
     * Listens to a button press event on the download button located in the view class
     */
    class DownloadListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String text = view.numberOfResults.getText();
            //Make sure that only numbers are accepted
            Pattern numbersOnly = Pattern.compile("^[0-9]+$");
            Matcher matcher = numbersOnly.matcher(text);
            if (!hasSearchedBefore) {
                view.displayError("Please search for a paper before pressing download.");
            } else {
                if (matcher.find()) {
                    numOfPDFToDownload = matcher.group();
                    label = new Labels();
                    label.execute();

                } else {
                    view.displayError("Please only write numbers here.");
                }
            }
        }

    }

    /**
     * SwingWorker class to handle the text area.
     */
    public class Output extends SwingWorker<Void, String> {

        /**
         * No args constructor
         */
        Output() {
        }


        /**
         * Performs the search operation in the background, to keep GUI responsive
         *
         * @return null
         * @throws Exception IOException or IllegalArgumentException
         */
        @Override
        protected Void doInBackground() throws Exception {
            //Runs the search function in background
            search();
            return null;
        }

        /**
         * Once search is done, display message
         */
        @Override
        protected void done() {
            view.outputTextField.setText("Done Searching");
        }

        @Override
        protected void process(List<String> chunks) {
            String latestOutput = chunks.get(chunks.size() - 1);
            view.outputTextField.setText(latestOutput);
        }


        /**
         * Call to update information in the GUI while processing.
         *
         * @param s String of text to add
         */
        void updateOutputText(String s) {
            publish(s);
        }

    }


    /**
     * SwingWorker class to handle the update of labels.
     */
    public class Labels extends SwingWorker<Void, String> {
        String label = "";

        /**
         * No arg constructor
         */
        Labels() {

        }


        /**
         * Perform the getPDFs() method in the background
         *
         * @return null
         * @throws Exception IOException
         */
        @Override
        protected Void doInBackground() throws Exception {
            try {
                crawler.getPDFs(Integer.parseInt(numOfPDFToDownload));
            } catch (Exception e1) {
                view.displayError(e1.getMessage());
            }
            return null;
        }

        /**
         * Message to display once search is done
         */
        @Override
        protected void done() {
            view.outputTextField.setText("All PDFs requested have been downloaded");
        }

        @Override
        protected void process(List<String> chunks) {
            String latestOutput = chunks.get(chunks.size() - 1);

            if (label.equals("numOfRequests")) {
                view.requestLabel.setText(latestOutput);
            } else if (label.equals("numOfDownloads")) {
                view.updateDownloadField(latestOutput);
            } else {
                view.updateOutputTextField(latestOutput);
            }
        }

        /**
         * Call to update information in tone of the labesl
         *
         * @param label label that is going to be updated
         * @param s     String to output
         */
        public void updateOutputText(String label, String s) {
            this.label = label;
            publish(s);
        }

    }


}
