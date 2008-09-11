package net.link.safeonline.option.connection.manager;

import java.awt.EventQueue;

import javax.swing.UIManager;

public class Main {

	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		Runnable runner = new Runnable() {

			public void run() {

				new ConnectionManager();
			}
		};
		EventQueue.invokeLater(runner);
	}

}
