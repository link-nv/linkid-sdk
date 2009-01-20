/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.applet;

import java.applet.AppletContext;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.link.safeonline.applet.exception.NoHelpdeskConfiguredException;
import net.link.safeonline.shared.Signer;
import net.link.safeonline.shared.helpdesk.HelpdeskCodes;
import net.link.safeonline.shared.helpdesk.LogLevelType;
import net.link.safeonline.shared.statement.IdentityProvider;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The base class for both the identity and the authentication applet.
 * 
 * @author fcorneli
 */
public abstract class AppletBase extends JApplet implements ActionListener, AppletView, AppletHelpdesk, RuntimeContext, StatementProvider {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(AppletBase.class);

    JTextArea                 outputArea;

    JLabel                    infoLabel;

    InfoLevel                 infoLevel;

    JProgressBar              progressBar;

    private JPanel            cards;

    private JButton           detailsButton;

    JButton                   retryButton;

    JButton                   helpButton;

    JButton                   tryAnotherDeviceButton;

    Thread                    thread;

    List<HelpdeskEvent>       helpdeskEvents;


    private class HelpdeskEvent {

        private String       message;

        private LogLevelType logLevel;


        public HelpdeskEvent(String message, LogLevelType logLevel) {

            this.message = message;
            this.logLevel = logLevel;
        }

        public String getMessage() {

            return message;
        }

        public LogLevelType getLogLevel() {

            return logLevel;
        }
    }

    private static enum State {
        HIDE,
        SHOW
    }


    private State          state = State.HIDE;

    private ResourceBundle messages;

    AppletController       appletController;


    protected AppletBase() {

        appletController = new AppletControl();
    }

    protected AppletBase(AppletController appletController) {

        this.appletController = appletController;
    }

    @Override
    public void init() {

        Locale locale = getLocale();
        messages = ResourceBundle.getBundle("net.link.safeonline.applet.AppletMessages", locale);

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {

                    setupScreen();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("could not setup the GUI");
        }
        initAppletController();

        helpdeskEvents = new LinkedList<HelpdeskEvent>();
    }

    void initAppletController() {

        appletController.init(this, this, this);
        thread = new Thread(appletController);
        thread.start();
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    void setupScreen() {

        setLayout(new BorderLayout());
        Container container = getContentPane();

        JPanel infoPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(infoPanel, BoxLayout.PAGE_AXIS);
        infoPanel.setLayout(boxLayout);
        container.add(infoPanel, BorderLayout.NORTH);
        progressBar = new JProgressBar(SwingConstants.HORIZONTAL);
        progressBar.setIndeterminate(true);
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoLabel = new JLabel();
        Font font = infoLabel.getFont();
        font = font.deriveFont((float) 16);
        infoLabel.setFont(font);
        infoLabel.setText(messages.getString("starting"));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        detailsButton = new JButton();
        detailsButton.addActionListener(this);
        retryButton = new JButton(messages.getString("retryAction"));
        retryButton.setVisible(false);
        Font helpFont = font.deriveFont((float) 10);
        helpButton = new JButton(messages.getString("helpAction"));
        helpButton.setFont(helpFont);
        helpButton.setForeground(Color.red);
        helpButton.setVisible(false);
        tryAnotherDeviceButton = new JButton(messages.getString("tryAnotherDeviceAction"));
        tryAnotherDeviceButton.setFont(helpFont);
        tryAnotherDeviceButton.setVisible(false);
        textPanel.add(infoLabel);
        buttonPanel.add(detailsButton);
        buttonPanel.add(retryButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(tryAnotherDeviceButton);
        infoPanel.add(textPanel);
        infoPanel.add(progressBar);
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(buttonPanel);
        infoPanel.add(Box.createVerticalStrut(10));

        cards = new JPanel(new CardLayout());
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        cards.add(scrollPane, "details");
        JPanel emptyPanel = new JPanel();
        cards.add(emptyPanel, "empty");
        container.add(cards, BorderLayout.CENTER);

        // syncing state
        actionPerformed(null);
        actionPerformed(null);

        retryButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                appletController.abort();
                thread.interrupt();
                retryButton.setVisible(false);
                helpButton.setVisible(false);
                tryAnotherDeviceButton.setVisible(false);
                AppletBase.this.validate();
                AppletBase.this.repaint();
                initAppletController();
            }
        });

        helpButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                try {
                    logHelpdesk();
                } catch (IOException e1) {
                    SwingUtilities.invokeLater(new Runnable() {

                        public void run() {

                            outputArea.append("Error sending helpdesk log\n");
                        }
                    });
                }
                redirectToHelp();
            }
        });

        tryAnotherDeviceButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                redirectToTryAnotherDevice();
            }
        });

        // background color
        String c = getParameter("bgcolor");
        Color color = null;
        try {
            color = Color.decode(c);
        } catch (Exception e) {
            color = Color.WHITE;
        }
        iterateBackground(container.getComponents(), color);

    }

    public void outputDetailMessage(final String message) {

        try {
            addHelpdeskEvent(message, LogLevelType.INFO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         * We used to have invokeAndWait here, but this sometimes causes a deadlock between: RunnableQueue-0 and AWT-EventQueue-0.
         */
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                outputArea.append(message + "\n");
            }
        });
    }

    public void outputInfoMessage(final InfoLevel messageInfoLevel, final String message) {

        try {
            switch (messageInfoLevel) {
                case NORMAL:
                    addHelpdeskEvent(message, LogLevelType.INFO);
                break;
                case ERROR:
                    addHelpdeskEvent(message, LogLevelType.ERROR);
                break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                infoLabel.setText(message);
                if (infoLevel != messageInfoLevel) {
                    infoLevel = messageInfoLevel;
                    switch (messageInfoLevel) {
                        case NORMAL:
                            infoLabel.setForeground(Color.BLACK);
                            progressBar.setIndeterminate(true);
                        break;
                        case ERROR:
                            infoLabel.setForeground(Color.RED);
                            progressBar.setIndeterminate(false);
                            retryButton.setVisible(true);
                            helpButton.setVisible(true);
                            tryAnotherDeviceButton.setVisible(true);
                            AppletBase.this.validate();
                            AppletBase.this.repaint();
                        break;
                    }
                }
            }
        });
    }

    public void addHelpdeskEvent(String message, LogLevelType logLevel)
            throws IOException {

        LOG.debug("add helpdesk event: " + message);
        helpdeskEvents.add(new HelpdeskEvent(message, logLevel));
    }

    public boolean logHelpdesk()
            throws IOException {

        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = prepareHelpdeskConnection();
        } catch (NoHelpdeskConfiguredException e) {
            return false;
        }

        int idx = 0;
        for (HelpdeskEvent event : helpdeskEvents) {
            httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_ADD + idx, Integer.toString(idx));
            httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_ADD_MESSAGE + idx, event.getMessage());
            httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_ADD_LEVEL + idx, event.getLogLevel().toString());
            idx++;
        }

        return sendHelpdeskStatement(httpURLConnection);
    }

    public boolean clearHelpdesk()
            throws IOException {

        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = prepareHelpdeskConnection();
        } catch (NoHelpdeskConfiguredException e) {
            return true;
        }
        httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_CLEAR, "");
        return sendHelpdeskStatement(httpURLConnection);
    }

    public Long persistHelpdesk()
            throws IOException {

        HttpURLConnection httpURLConnection;
        try {
            httpURLConnection = prepareHelpdeskConnection();
        } catch (NoHelpdeskConfiguredException e) {
            return new Long(-1);
        }

        httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_PERSIST, "");

        if (!sendHelpdeskStatement(httpURLConnection))
            return new Long(-1);

        String helpdeskPersistId = httpURLConnection.getHeaderField(HelpdeskCodes.HELPDESK_PERSIST_RETURN_ID);
        if (null == helpdeskPersistId)
            return new Long(-1);
        return Long.parseLong(helpdeskPersistId);
    }

    private HttpURLConnection prepareHelpdeskConnection()
            throws IOException, NoHelpdeskConfiguredException {

        URL documentBase = getDocumentBase();
        String servletPath = getParameter("HelpdeskEventPath");
        if (null == servletPath)
            throw new NoHelpdeskConfiguredException();
        URL url = AppletControl.transformUrl(documentBase, servletPath);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestProperty(HelpdeskCodes.HELPDESK_START, "");
        return httpURLConnection;
    }

    private boolean sendHelpdeskStatement(HttpURLConnection httpURLConnection)
            throws IOException {

        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setAllowUserInteraction(false);
        httpURLConnection.setRequestProperty("Content-type", "application/octet-stream");
        httpURLConnection.connect();

        httpURLConnection.disconnect();

        int responseCode = httpURLConnection.getResponseCode();
        if (200 == responseCode)
            return true;
        return false;
    }

    public abstract byte[] createStatement(Signer signer, IdentityProvider identityProvider);

    private void setButtonLabel(JButton button) {

        if (state == State.HIDE) {
            button.setText(messages.getString("showDetails"));
        } else {
            button.setText(messages.getString("hideDetails"));
        }
    }

    public void actionPerformed(ActionEvent e) {

        CardLayout cl = (CardLayout) cards.getLayout();
        if (state == State.HIDE) {
            cl.show(cards, "details");
            state = State.SHOW;
            setButtonLabel(detailsButton);
        } else {
            cl.show(cards, "empty");
            state = State.HIDE;
            setButtonLabel(detailsButton);
        }
    }

    void redirectToHelp() {

        URL documentBase = getDocumentBase();
        String targetPath = getParameter("HelpPath");
        URL target = AppletControl.transformUrl(documentBase, targetPath);
        showDocument(target);
    }

    void redirectToTryAnotherDevice() {

        URL documentBase = getDocumentBase();
        String targetPath = getParameter("TargetPath") + "?cacheid=" + Math.random() * 1000000;
        URL target = AppletControl.transformUrl(documentBase, targetPath);
        showDocument(target);
    }

    private void iterateBackground(Component[] components, Color color) {

        for (Component comp : components) {
            if (comp instanceof Container) {
                Container container = (Container) comp;
                iterateBackground(container.getComponents(), color);
            }
            comp.setBackground(color);
        }
    }

    public Log getLog() {

        Log log = new AppletLog(this);
        return log;
    }

    public void showDocument(URL url) {

        AppletContext appletContext = getAppletContext();
        appletContext.showDocument(url);
    }

    @Override
    public Locale getLocale() {

        String language = getParameter("Language");
        if (null != language) {
            Locale locale = new Locale(language);
            return locale;
        }
        return super.getLocale();
    }

    @Override
    public String getParameter(String name) {

        return super.getParameter(name);
    }
}
