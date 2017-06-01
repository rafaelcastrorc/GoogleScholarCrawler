import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.Seconds;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by rafaelcastro on 5/31/17.
 */


public class Crawler extends Thread {
    private String numOfCitations = "";
    private String citingPapersURL = "";
    //Counts number of requests
    private int counter = 0;
    private List<Observer> observers = new ArrayList<Observer>();
    private int pdfCounter;
    private String outputText = "";
    private Integer[] listOfTimes;
    private Integer timeToWait;


    void searchForArticle (String keyword, int no_of_results) throws IOException {

        //Replace space by + in the keyword as in the google search url
        keyword = keyword.replace(" ", "+");
        //Search google schoolar
        String url = "https://scholar.google.com/scholar?hl=en&q=" + keyword;
        counter++;
        System.out.println(url);
        //Connect to the url and obain HTML response
        Document doc = null;
        try {

            doc = Jsoup.connect(url).userAgent("Mozilla").timeout(5000).get();
            String text = "";
            String absLink = "";
            Elements links  = doc.select("a[href]");
            for (Element link : links) {
                text = link.text();
                absLink = link.attr("abs:href");

                if (text.contains("Cited by")) {
                    break;
                }
            }
            numOfCitations = text;
            citingPapersURL = absLink;
            System.out.println(text);
            System.out.println(absLink);

        } catch (IOException e) {
            throw new IOException("There was a problem connecting to Google Schoolar. Make sure that you have internet connection. ");
        }




    }

    //Gets all the possible search results where the article is cited
    private ArrayList<String> getAllLinks() {
        ArrayList<String> list = new ArrayList<>();
        Pattern pattern = Pattern.compile("=\\d*");
        Matcher matcher = pattern.matcher(citingPapersURL);
        String paperID = "";
        if (matcher.find()) {
            paperID = matcher.group();
            paperID = paperID.replace("=", "");
        }
        list.add(citingPapersURL);
        for (int i = 10; i <1000 + 1; i = i + 10) {
            StringBuilder sb = new StringBuilder();
            sb.append("https://scholar.google.com/scholar?start=130&hl=en&oe=ASCII&as_sdt=5,39&sciodt=0,39&cites=").
                    append(paperID).append("&scipsc=");
            list.add(sb.toString());

        }
        return list;

    }

    void downloadPDF (String url) throws IOException {
        File docDestFile = new File("./comparison2/" + pdfCounter + ".pdf");
        System.out.println(url);
        URL urlObj = new URL(url);
        FileUtils.copyURLToFile(urlObj, docDestFile);

       // InputStream in = urlObj.openStream();
       // System.out.println(in);
      //  Files.copy(in, Paths.get(String.valueOf(pdfCounter)+".pdf"), StandardCopyOption.REPLACE_EXISTING);
      //  in.close();

    }

    String getNumberOfCitations() {
        return numOfCitations;
    }

    void changeIP() {


    }

    void getProxys() {}


    public void getPDFs(int limit) throws Exception {

        pdfCounter = 0;
        //Go though all links
        ArrayList<String> list = getAllLinks();
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Please search of an author before downloading");
        }
        for (String currUrl : list) {
            if (pdfCounter >= limit) {
                setTextState("All requested results have been found", false);
                break;
            }
            Thread.sleep(timeToWait * 1000);
                //Increase counter for every new google link
                counter++;
                Document citingPapers = Jsoup.connect(currUrl).userAgent("Chrome").timeout(5000).get();
                Elements linksInsidePaper = citingPapers.select("a[href]");
                String text = "";
                String absLink = "";
                for (Element link : linksInsidePaper) {
                    text = link.text();
                    absLink = link.attr("abs:href");
                    if (text.contains("PDF")) {
                        pdfCounter++;
                        setState(pdfCounter);
                        downloadPDF(absLink);
                        System.out.println(text);
                        System.out.println(absLink);
                        if (pdfCounter >= limit) {
                            break;
                        }
                    }

                }

        }
    }

    protected void notifyAllObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }


    protected void setState(int pdfCounter) {
        this.pdfCounter = pdfCounter;
        notifyAllObservers();
    }

    protected int getPDFDownloadState() {
        return pdfCounter;
    }


     void attach(Observer observer) {
        observers.add(observer);
    }

     void setTextState(String output, boolean sameLine) {
        StringBuilder sb = new StringBuilder();
        if (sameLine == false) {
            sb.append(this.outputText).append("\n").append(output);
        }
        else sb.append(this.outputText).append(" ").append(output);
        outputText = sb.toString();
        notifyAllObservers();
    }

     String getTextState() {
        return outputText;
    }

    //select time at random
    int getTimeToWait(){
        if (listOfTimes == null) {
            listOfTimes = new Integer[10];
            for (int i =0; i <listOfTimes.length; i++) {
                listOfTimes[i] = i + 1;
            }
        }
        int rnd = new Random().nextInt(listOfTimes.length);
        this.timeToWait = listOfTimes[rnd];
        return timeToWait;
    }

    String getUserAgent() {
        return null;
    }



}