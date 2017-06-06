import javax.swing.*;

/**
 * Created by rafaelcastro on 6/1/17.
 * Use it to run the program
 */
public class Main {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.start();
        View view = new View();

        //Sets everything for the GUI to show
        JFrame frame = new JFrame("Crawler");
        frame.setContentPane(view.viewPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Controller controller = new Controller(crawler, view, true);
    }


}
