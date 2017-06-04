import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by rafaelcastro on 5/31/17.
 * Crawler to gather data from Google Scholar.
 * It is the subject of the Observer pattern
 */


public class Crawler extends Thread {
    private String numOfCitations = "";
    private String citingPapersURL = "";
    //Counts number of requests
    private int requestCounter = 0;
    private List<Observer> observers = new ArrayList<>();
    private int pdfCounter;
    private String outputText = "";
    private Integer[] listOfTimes;
    private ArrayList<String[]> listOfIPs;
    private String[] ipAndPort;
    private Integer timeToWait;


    /**
     * Search for an article in Google Schoolar
     *
     * @param keyword         String with the title of the document
     * @param hasSearchBefore has the user press the button search before
     * @throws IOException error while openning/connecting to the website
     */
    void searchForArticle(String keyword, boolean hasSearchBefore) throws IOException {
        //If the list of ips has not been initialized, then call the method
        if (listOfIPs == null) {
            getProxys();
        }
        int invalidAttempts = 0;
        //Replace space by + in the keyword as in the google search url
        keyword = keyword.replace(" ", "+");
        //Search google scholar
        String url = "https://scholar.google.com/scholar?hl=en&q=" + keyword;
        boolean found = false;
        while (!found) {
            if (invalidAttempts >= 2) {
                setTextState("Could not find paper, please try writing more specific information");
                numOfCitations = "";
                citingPapersURL = "";
                found = true;
            } else {
                Document doc;
                try {
                    doc = changeIP(url, hasSearchBefore);
                } catch (IOException e) {
                    setTextState("There was a problem connecting to your previously used proxy.\nChanging to a different one");
                    doc = changeIP(url, false);
                }


                if (doc.text().contains("Sorry, we can't verify that you're not a robot")) {
                    //In case you been flags as a bot even before searching
                    setTextState("Google flagged your IP as a bot.\nChanging to a different one");
                    doc = changeIP(url, false);

                }

                requestCounter++;
                setTextState(String.valueOf("Number of requests: " + requestCounter));


                String text = "";
                String absLink = "";
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    text = link.text();
                    absLink = link.attr("abs:href");

                    if (text.contains("Cited by")) {
                        found = true;
                        break;
                    }
                }

                numOfCitations = text;
                citingPapersURL = absLink;
                System.out.println(!doc.toString().contains("1 result"));
                System.out.println(!doc.toString().contains("Showing the best result for this search"));

                if (!doc.toString().contains("1 result") && !doc.toString().contains("Showing the best result for this search")) {
                    setTextState("ERROR: There was more than 1 result found for your given query.\nPlease write the entire title and/or the authors");
                    numOfCitations = "There was more than 1 result found for your given query";
                }
                invalidAttempts++;
            }
        }


    }

    /**
     * Gets all the possible search results where the article is cited
     *
     * @return ArrayList with all the links
     */
    private ArrayList<String> getAllLinks() {
        ArrayList<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("=\\d*");
        Matcher matcher = pattern.matcher(citingPapersURL);
        String paperID = "";
        if (matcher.find()) {
            paperID = matcher.group();
            paperID = paperID.replace("=", "");
        }
        //Add 1-10 results
        list.add(citingPapersURL);
        for (int i = 10; i < 1000 + 1; i = i + 10) {
            String sb = "https://scholar.google.com/scholar?start=" + i + "&hl=en&oe=ASCII&as_sdt=5,39&sciodt=0,39&cites=" +
                    paperID + "&scipsc=";
            list.add(sb);

        }
        return list;

    }

    /**
     * Download a pdf file to a directory
     *
     * @param url URL to download file from
     * @throws IOException Unable to open link
     */
    private void downloadPDF(String url) throws IOException {
        File docDestFile = new File("./DownloadedPDFs/" + pdfCounter + ".pdf");
        URL urlObj = new URL(url);
        FileUtils.copyURLToFile(urlObj, docDestFile);

    }


    /**
     * Get number of papers that cite this article
     *
     * @return String
     */
    String getNumberOfCitations() {
        return numOfCitations;
    }


    /**
     * Change current IP, or continue using the last working one
     *
     * @param url             url that you are trying to connect
     * @param hasSearchBefore has the user click the search button before
     * @return Document
     * @throws IOException Unable to open file
     */
    private Document changeIP(String url, boolean hasSearchBefore) throws IOException {
        if (listOfIPs.isEmpty()) {
            //If there was a problem connecting to the proxy database, connect always using your own IP while the session is on
            Document d = null;
            try {
                d = Jsoup.connect(url).userAgent("Mozilla").get();


            } catch (IOException e) {
                setTextState("Could not connect, please check your internet connection");
            }
            return d;
        }
        if (hasSearchBefore && requestCounter <= 100 && !numOfCitations.isEmpty()) {

            //If has searched before and it worked, then use previous ip
            return Jsoup.connect(url).proxy(ipAndPort[0], Integer.valueOf(ipAndPort[1])).userAgent("Mozilla").get();
        }

        //Reset request counter
        requestCounter = 0;
        setTextState(String.valueOf("Number of requests: " + requestCounter));
        boolean connected = false;
        Document doc = null;
        boolean thereWasAnError = false;
        int attempt = 1;
        while (!connected) {
            ipAndPort = listOfIPs.get(new Random().nextInt(listOfIPs.size()));
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);

            try {
                if (thereWasAnError) {
                    setTextState("Attempt " + attempt + ": Failed to connect to Proxy, trying with a different one...");
                    attempt++;
                } else {
                    setTextState("Connecting to Proxy...");
                }
                doc = Jsoup.connect(url).proxy(ip, port).userAgent("Mozilla").get();
                connected = true;

            } catch (Exception e) {
                thereWasAnError = true;
            }
        }

        return doc;

    }

    //Gets a list of all available proxys from a website

    /**
     * Gets a list of proxys to use based on the website below.
     */
    private void getProxys() {
        listOfIPs = new ArrayList<>();
        Document doc;
        try {
            doc = Jsoup.connect("http://www.us-proxy.org/").userAgent("Chrome").timeout(5000).get();

            Elements table = doc.select("table");
            Elements rows = table.select("tr");

            Pattern ips = Pattern.compile("\\b(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\b");
            for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
                Element row = rows.get(i);
                Elements cols = row.select("td");
                boolean found = false;
                String[] array = new String[2];
                for (Element elt : cols) {
                    Matcher matcher = ips.matcher(elt.toString());
                    if (found) {
                        //Get Port number
                        String portNum = elt.toString();
                        portNum = portNum.replaceAll("</?td>", "");
                        array[1] = portNum;
                        listOfIPs.add(array);
                        array = new String[2];
                        found = false;
                    }
                    if (matcher.find()) {
                        //If an Ip is found, then the next element is the port number
                        found = true;
                        array[0] = matcher.group();
                    }
                }
            }
        } catch (IOException e) {
            setTextState("There was a problem accessing the Proxy Database. \nWe will try to connect you using your own IP.");
        }
    }


    /**
     * Downloads the number of pdf requested
     *
     * @param limit max number of pdfs to download
     * @throws Exception Problem downloading or reading a file
     */
    void getPDFs(int limit) throws Exception {

        pdfCounter = 0;
        //Go though all links
        ArrayList<String> list = getAllLinks();
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Please search of an author before downloading");
        }
        for (String currUrl : list) {
            if (pdfCounter >= limit) {
                setTextState("All requested results have been found");
                break;
            }
            timeToWait = getTimeToWait();
            setTextState("Waiting " + timeToWait + " seconds before going to the search results");
            Thread.sleep(timeToWait * 1000);
            setTextState("Downloading...");

            //Increase counter for every new google link
            Document citingPapers;
            if (requestCounter >= 100) {
                setTextState("Wait... Changing proxy because of amount of requests...");
                citingPapers = changeIP(currUrl, false);
            } else {
                try {

                    citingPapers = changeIP(currUrl, true);
                } catch (IOException e) {
                    setTextState("There was a problem connecting to your previously used proxy.\nChanging to a different one");
                    citingPapers = changeIP(currUrl, false);
                }
            }

            if (citingPapers.text().contains("Sorry, we can't verify that you're not a robot")) {
                //In case you been flagges as a bot even before searching
                setTextState("Google flagged your IP as a bot.\nChanging to a different one");
                citingPapers = changeIP(currUrl, false);

            }

            requestCounter++;

            setTextState(String.valueOf("Number of requests: " + requestCounter));
            Elements linksInsidePaper = citingPapers.select("a[href]");
            String text;
            String absLink;
            for (Element link : linksInsidePaper) {
                text = link.text();
                absLink = link.attr("abs:href");
                if (text.contains("PDF")) {
                    pdfCounter++;
                    try {
                        downloadPDF(absLink);
                    } catch (IOException e2) {
                        setTextState("This file could not be downloaded, skipping...");
                        pdfCounter--;
                    }
                    setTextState("Downloaded: " + String.valueOf(pdfCounter));

                    System.out.println(text);
                    System.out.println(absLink);
                    if (pdfCounter >= limit) {
                        break;
                    }
                }

            }
        }
    }

    /**
     * Notifies controller that data has changed
     */
    private void notifyAllObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }

    /**
     * Attaches an observer
     *
     * @param observer Controller that is going to observe
     */
    void attach(Observer observer) {
        observers.add(observer);
    }

    /**
     * Changes the state of the text. Notifies observer
     *
     * @param output String to output
     */
    private void setTextState(String output) {
        outputText = output;
        notifyAllObservers();
    }

    /**
     * Gets the latest set text
     *
     * @return String with the text
     */
    String getTextState() {
        return outputText;
    }

    /**
     * Generates a random time to wait before performing a task
     *
     * @return int that represents seconds
     */
    private int getTimeToWait() {
        if (listOfTimes == null) {
            listOfTimes = new Integer[5];
            for (int i = 0; i < listOfTimes.length; i++) {
                listOfTimes[i] = i + 2;
            }
        }
        int rnd = new Random().nextInt(listOfTimes.length);
        this.timeToWait = listOfTimes[rnd];
        return timeToWait;
    }


}