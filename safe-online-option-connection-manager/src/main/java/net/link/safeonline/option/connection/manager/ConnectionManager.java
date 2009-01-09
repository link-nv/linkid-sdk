package net.link.safeonline.option.connection.manager;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;


public class ConnectionManager extends JFrame {

    private static final long   serialVersionUID = 1L;

    ConnectionManagerController connectionManagerController;

    /*
     * GUI Components
     */
    private JLabel              label            = new JLabel("Connection Manager");


    public ConnectionManager() {

        super();

        ImageIcon icon = createImageIcon("/net/link/safeonline/option/connection/manager/images/globetrotter.png",
                "globetrotter screenshot");

        label = new JLabel(icon);

        this.add(label);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(380, 250);
        this.setLocation(50, 50);
        setVisible(true);

        try {
            String port = JOptionPane.showInputDialog(this, "The serial port of your Option device:", "Serial port for Option Device.",
                    JOptionPane.QUESTION_MESSAGE, null, null, "/dev/tty.GTM HSDPA Control").toString();
            connectionManagerController = new ConnectionManagerController(port);
        } catch (Exception e) {
            this.add(new JLabel("Exception occurred"));
        }
    }

    protected ImageIcon createImageIcon(String path, String description) {

        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null)
            return new ImageIcon(imgURL, description);
        System.err.println("Couldn't find file: " + path);
        return null;

    }

}
