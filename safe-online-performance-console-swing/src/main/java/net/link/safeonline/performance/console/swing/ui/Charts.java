/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.performance.console.swing.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.Scrollable;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import net.link.safeonline.performance.console.swing.data.ConsoleAgent;
import net.link.safeonline.performance.console.swing.data.ConsoleData;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jgroups.Address;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.looks.Options;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.tools.Executable;

/**
 * @author mbillemo
 *
 */
public class Charts extends WindowAdapter {

	static final Log LOG = LogFactory.getLog(Charts.class);

	private static Charts instance;
	private JTabbedPane agents;
	private JFrame frame;

	private Charts() {

		// Tabs.
		this.agents = new JTabbedPane();
		this.agents.setBorder(Borders.DLU4_BORDER);
		this.agents.putClientProperty(Options.EMBEDDED_TABS_KEY, true);

		// Frame.
		this.frame = new JFrame("Performance Testing Charts");
		this.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		this.frame.setContentPane(this.agents);
		this.frame.addWindowListener(this);
	}

	private void addTab(Address agent, List<byte[]> chartList) {

		String tabTitle = String.format("%s (x%d)", agent.toString(),
				ConsoleData.getInstance().getWorkers());
		AgentCharts agentCharts = new AgentCharts(chartList);
		this.agents.addTab(tabTitle, new JScrollPane(agentCharts));
	}

	/**
	 * @{inheritDoc}
	 */
	@Override
	public void windowClosed(WindowEvent e) {

		this.frame = null;
		instance = null;
	}

	public static void display(Collection<ConsoleAgent> agents) {

		if (instance == null)
			instance = new Charts();

		for (ConsoleAgent agent : agents)
			instance.addTab(agent.getAddress(), agent.getCharts());

		instance.show();
	}

	private void show() {

		this.frame.pack();
		this.frame.setLocationRelativeTo(null);
		this.frame.setVisible(true);
	}

	private static class AgentCharts extends JPanel implements Scrollable,
			ActionListener {

		private static final long serialVersionUID = 1L;
		private static final int INCREMENT = 50;
		private JButton pdf;
		private List<byte[]> charts;

		public AgentCharts(List<byte[]> chartList) {

			FormLayout layout = new FormLayout("p");
			DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
			builder.setDefaultDialogBorder();

			builder.append(this.pdf = new JButton(
					"Save as and open a PDF version ..."));
			this.pdf.addActionListener(this);

			this.charts = chartList;
			for (byte[] chart : chartList)
				builder.append(new JLabel(new ImageIcon(chart)));
		}

		/**
		 * @{inheritDoc}
		 */
		public Dimension getPreferredScrollableViewportSize() {

			return getPreferredSize();
		}

		/**
		 * @{inheritDoc}
		 */
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {

			return INCREMENT * 10;
		}

		/**
		 * @{inheritDoc}
		 */
		public boolean getScrollableTracksViewportHeight() {

			return false;
		}

		/**
		 * @{inheritDoc}
		 */
		public boolean getScrollableTracksViewportWidth() {

			return false;
		}

		/**
		 * @{inheritDoc}
		 */
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {

			return INCREMENT;
		}

		/**
		 * {@inheritDoc}
		 */
		public void actionPerformed(ActionEvent e) {

			if (this.pdf.equals(e.getSource())) {
				JFileChooser pdfChooser = new JFileChooser();
				pdfChooser.addChoosableFileFilter(new FileFilter() {
					@Override
					public boolean accept(File f) {

						return f.isFile() && f.canWrite()
								&& f.getName().endsWith(".pdf");
					}

					@Override
					public String getDescription() {

						return "PDF File";
					}
				});

				if (pdfChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
					File pdfFile = pdfChooser.getSelectedFile();

					try {
						float width = 0, height = 0;
						for (Component c : getComponents()) {
							width = Math.max(width, c.getWidth());
							height = Math.max(height, c.getHeight());
						}

						Document pdfDocument = new Document(
								new com.lowagie.text.Rectangle(width + 100,
										height + 100), 50, 50, 50, 50);
						PdfWriter.getInstance(pdfDocument,
								new FileOutputStream(pdfFile));

						BaseFont font = BaseFont.createFont(
								BaseFont.COURIER_BOLD, BaseFont.WINANSI, false);

						pdfDocument.open();
						Cell title = new Cell(new Phrase(
								"Safe Online:  Performance Testing", new Font(
										font, 20f)));
						title.setHorizontalAlignment(Element.ALIGN_CENTER);
						title.setVerticalAlignment(Element.ALIGN_MIDDLE);
						title.setBorder(0);

						Table front = new Table(1);
						front.setBorder(0);
						front.setOffset(height / 3);
						front.addCell(title);
						pdfDocument.add(front);
						pdfDocument.newPage();

						for (byte[] chart : this.charts)
							pdfDocument.add(Image.getInstance(chart));
						pdfDocument.close();

						Executable.openDocument(pdfFile);
					} catch (FileNotFoundException error) {
						LOG.error("File not found: " + pdfFile, error);
					} catch (DocumentException error) {
						LOG.error("Couldn't create a document writer.", error);
					} catch (IOException error) {
						LOG.error("Couldn't handle image data.", error);
					}
				}
			}
		}
	}
}
