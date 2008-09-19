package net.link.safeonline.option.connection.manager;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class ConnectionManager extends JFrame {

    private static final long   serialVersionUID = 1L;

    ConnectionManagerController connectionManagerController;

    /*
     * GUI Components
     */
    private JLabel              label            = new JLabel("Connection Manager");


    public ConnectionManager() {

        super();

        try {
            this.connectionManagerController = new ConnectionManagerController();
        } catch (Exception e) {
            this.add(new JLabel("Exception occurred"));
        }

        ImageIcon icon = createImageIcon("/net/link/safeonline/option/connection/manager/images/globetrotter.png",
                "globetrotter screenshot");

        this.label = new JLabel(icon);

        this.add(this.label);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(380, 250);
        this.setLocation(50, 50);
        setVisible(true);
    }

    protected ImageIcon createImageIcon(String path, String description) {

        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null)
            return new ImageIcon(imgURL, description);
        System.err.println("Couldn't find file: " + path);
        return null;

    }

}
