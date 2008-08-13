/*
 *   Copyright 2007, Maarten Billemont
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.link.safeonline.audit;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * <h2>{@link TinySyslogger} - Tiny logger that writes to Syslog.</h2>
 * <p>
 * This implementation requires syslog to be listening for UDP messages on either the default syslog port (514) or a
 * port provided in code.<br>
 * Messages will be redirected as configured by your syslog configuration, so make sure you know how your syslog daemon
 * is configured to treat messages from the UDP source.
 * </p>
 * <p>
 * <i>Dec 5, 2007</i>
 * </p>
 * 
 * @author mbillemo
 */
public class TinySyslogger {

    private static final Log                                    LOG                 = LogFactory
                                                                                            .getLog(TinySyslogger.class);
    private static final String                                 DEFAULT_SYSLOG_HOST = "localhost";
    private static final int                                    DEFAULT_SYSLOG_PORT = 514;
    private static final Map<InetSocketAddress, DatagramSocket> sockets             = Collections
                                                                                            .synchronizedMap(new HashMap<InetSocketAddress, DatagramSocket>());

    private InetSocketAddress                                   syslog;
    private Facility                                            facility;


    /**
     * Sends events to the syslog daemon on localhost at port 514.
     * 
     * @param facility
     *            The syslog facility to send messages to. This defaults to {@link Facility#USER} if <code>null</code>
     *            is given.
     */
    public TinySyslogger(Facility facility) {

        this(facility, DEFAULT_SYSLOG_HOST, DEFAULT_SYSLOG_PORT);
    }

    /**
     * Sends events to the syslog daemon at port 514.
     * 
     * @param facility
     *            The syslog facility to send messages to. This defaults to {@link Facility#USER} if <code>null</code>
     *            is given.
     * @param host
     *            The IP address or hostname of the syslog daemon.
     */
    public TinySyslogger(Facility facility, String host) {

        this(facility, host, DEFAULT_SYSLOG_PORT);
    }

    /**
     * Sends events to the given syslog daemon.
     * 
     * @param facility
     *            The syslog facility to send messages to. This defaults to {@link Facility#USER} if <code>null</code>
     *            is given.
     * @param host
     *            The IP address or hostname of the syslog daemon.
     * @param port
     *            The UDP port on which the syslog daemon is listening.
     */
    public TinySyslogger(Facility facility, String host, int port) {

        setFacility(facility);
        setRemote(host, port);
    }

    /**
     * @param facility
     *            The syslog facility to send messages to. This defaults to {@link Facility#USER} if <code>null</code>
     *            is given.
     */
    public void setFacility(Facility facility) {

        if (facility == null)
            this.facility = Facility.USER;
        else
            this.facility = facility;
    }

    /**
     * @param host
     *            The IP address or hostname of the syslog daemon.
     * @param port
     *            The UDP port on which the syslog daemon is listening.
     * 
     * @return <code>false</code>: No local UDP socket could be created.
     */
    public boolean setRemote(String host, int port) {

        close();
        this.syslog = new InetSocketAddress(host, port);

        if (!sockets.containsKey(this.syslog))
            try {
                sockets.put(this.syslog, new DatagramSocket());
            } catch (SocketException e) {
                LOG.error("Couldn't create an UDP socket for communication with syslog on " + host + ":" + port, e);
                return false;
            }

        return true;
    }

    /**
     * Dispatch a message to syslog.
     */
    public void log(String message) {

        if (!sockets.containsKey(this.syslog)) {
            LOG.error("No syslog socket available; lost message:\n" + message);
            return;
        }

        send(message);
    }

    private void send(String message) {

        // Split at RFC 3164 limit of 1024 bytes.
        byte[] bytes = message.getBytes();
        if (bytes.length <= 1029)
            try {
                // Prepend the message with the facility and level.
                bytes = String.format("<%d> %s", this.facility.getId(), message).getBytes();

                // Create a packet for the message and dispatch it.
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, this.syslog);
                sockets.get(this.syslog).send(packet);
            }

            catch (IOException e) {
                LOG.error("Couldn't dispatch packet of " + bytes.length + " bytes to syslog at " + this.syslog, e);
            }

        else {
            int split = message.length() / 2;
            send(message.substring(0, split) + "...");
            send("..." + message.substring(split));
        }
    }

    /**
     * @{inheritDoc
     */
    public void close() {

        if (sockets.containsKey(this.syslog))
            sockets.remove(this.syslog).close();
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void finalize() throws Throwable {

        close();
    }


    public static enum Facility {

        /** Kernel messages */
        KERN(0),
        /** Random user-level messages */
        USER(1 << 3),
        /** Mail system */
        MAIL(2 << 3),
        /** System daemons */
        DAEMON(3 << 3),
        /** security/authorization messages */
        AUTH(4 << 3),
        /** messages generated internally by syslogd */
        SYSLOG(5 << 3),

        /** line printer subsystem */
        LPR(6 << 3),
        /** network news subsystem */
        NEWS(7 << 3),
        /** UUCP subsystem */
        UUCP(8 << 3),
        /** clock daemon */
        CRON(9 << 3),
        /** security/authorization messages (private) */
        AUTHPRIV(10 << 3),
        /** ftp daemon */
        FTP(11 << 3),

        // other codes through 15 reserved for system use
        /** reserved for local use */
        LOCAL0(16 << 3),
        /** reserved for local use */
        LOCAL1(17 << 3),
        /** reserved for local use */
        LOCAL2(18 << 3),
        /** reserved for local use */
        LOCAL3(19 << 3),
        /** reserved for local use */
        LOCAL4(20 << 3),
        /** reserved for local use */
        LOCAL5(21 << 3),
        /** reserved for local use */
        LOCAL6(22 << 3),
        /** reserved for local use */
        LOCAL7(23 << 3);

        private int id;


        private Facility(int id) {

            this.id = id;
        }

        public int getId() {

            return this.id;
        }
    }
}
