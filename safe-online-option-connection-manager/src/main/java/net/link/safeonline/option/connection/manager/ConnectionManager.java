package net.link.safeonline.option.connection.manager;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class ConnectionManager extends JFrame {

	private static final long serialVersionUID = 1L;

	ConnectionManagerController connectionManagerController;

	/*
	 * GUI Components
	 */
	private JLabel label = new JLabel("Connection Manager");

	public ConnectionManager() {
		super();

		this.connectionManagerController = new ConnectionManagerController();

		this.add(label);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(20, 10);
		this.setLocation(50, 50);
		this.setVisible(true);
	}

}
