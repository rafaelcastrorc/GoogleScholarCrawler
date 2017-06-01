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
 */


public class Crawler extends Thread {
    private String numOfCitations = "";
    private String citingPapersURL = "";
    //Counts number of requests
    private int counter = 0;
    private List<Observer> observers = new ArrayList<>();
    private int pdfCounter;
    private String outputText = "";
    private Integer[] listOfTimes;
    private ArrayList<String[]> listOfIPs;

    private Integer timeToWait;

    Crawler() {
        getProxys();
    }
    void searchForArticle (String keyword, int no_of_results) throws IOException {
        //Replace space by + in the keyword as in the google search url
        keyword = keyword.replace(" ", "+");
        //Search google schoolar
        String url = "https://scholar.google.com/scholar?hl=en&q=" + keyword;
        Document doc = null;
        boolean connected = false;
        while (!connected) {
            try {
                doc = changeIP(url);
                connected = true;
            } catch (IOException e) {
                System.out.println("Problem connecting");
                setTextState("Failure. Connecting to a different proxy...", true);
            }
        }




        counter++;


            //doc = Jsoup.connect(url).userAgent("Mozilla").timeout(5000).get();
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

    //Downloads pdf to directory
    void downloadPDF (String url) throws IOException {
        File docDestFile = new File("./comparison2/" + pdfCounter + ".pdf");
        System.out.println(url);
        URL urlObj = new URL(url);
        FileUtils.copyURLToFile(urlObj, docDestFile);

    }

    //Gets the number of papers that cite this title
    String getNumberOfCitations() {
        return numOfCitations;
    }

    //Gets a random Ip to use as a proxy
    Document changeIP(String url) throws IOException {
        boolean connected = false;
        Document doc = null;
        while (!connected) {
            System.out.println("Assigning ip");
            String[] ipAndPort = listOfIPs.get(new Random().nextInt(listOfIPs.size()));
            String ip = ipAndPort[0];
            int port = Integer.valueOf(ipAndPort[1]);

            try {
                System.out.println("Trying to connect");
                 doc =  Jsoup.connect(url).proxy(ip, port).userAgent("Mozilla").get();
                connected = true;

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }


//        setTextState("Trying to connect to proxy: "+ ip, false);
//        URL urlObject = new URL(url);
//        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(ip, port));
//        HttpURLConnection uc = (HttpURLConnection)urlObject.openConnection(proxy);
//        uc.connect();
//        System.out.println("Connected!");
//        String line;
//        StringBuffer tmp = new StringBuffer();
//        BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
//        while ((line = in.readLine()) != null) {
//            tmp.append(line);
//        }
        return doc;

    }

    //Gets a list of all available proxys from a website
    private void getProxys(){
        listOfIPs = new ArrayList<>();
        Document doc = null;
        try {
            doc = Jsoup.connect("http://www.us-proxy.org/").userAgent("Chrome").timeout(5000).get();
        } catch (IOException e) {
            setTextState("There was a problem accessing the Proxy Database, you will be connected using your own IP.", false);
        }
        Elements table  = doc.select("table");
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
        System.out.println(listOfIPs.size());
    }


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




}