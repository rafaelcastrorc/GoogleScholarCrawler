import javax.swing.*;

/**
 * Created by rafaelcastro on 6/1/17.
 */
public class Main {

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        crawler.start();
        View view = new View(crawler);
        JFrame frame = new JFrame("View");
        frame.setContentPane(view.viewPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        Controller controller = new Controller(crawler, view, true);
//        controller.getTitle("FD, a bZIP Protein Mediating Signals from the Floral Pathway Integrator FT at the Shoot Apex");
//        controller.getNumOfResults(1);
//        controller.search();

    }
}
