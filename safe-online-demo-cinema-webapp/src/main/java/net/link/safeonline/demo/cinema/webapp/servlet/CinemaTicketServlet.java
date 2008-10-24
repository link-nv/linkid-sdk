/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.webapp.servlet;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.util.ee.EjbUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.thoughtworks.xstream.XStream;


public class CinemaTicketServlet extends HttpServlet {

    private static final long  serialVersionUID = 1L;
    private static final Log   LOG              = LogFactory.getLog(CinemaTicketServlet.class);

    public static final String NRN              = "nrn";
    public static final String TIME             = "time";
    public static final String FILM             = "film";
    public static final String THEATRE          = "theatre";

    private TicketService      ticketService;


    @Override
    public void init(ServletConfig config) throws ServletException {

        LOG.debug("init");
        super.init(config);

        this.ticketService = EjbUtils.getEJB(TicketService.BINDING, TicketService.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOG.debug("do get");

        String nrn = request.getParameter(NRN);
        String time = request.getParameter(TIME);
        String film = request.getParameter(FILM);
        String theatre = request.getParameter(THEATRE);
        Date date;

        // Validate parameters.
        if (null == nrn) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No National Register Number passed as parameter.");
            return;
        }
        if (null == time) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No Time passed as parameter.");
            return;
        }
        try {
            date = new Date(Long.valueOf(time) * 1000);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Time parameter does not contain a valid timestamp: " + time);
            return;
        }

        // Feed the log.
        LOG.debug("---- Start Cinema Ticket Validation:");
        LOG.debug("NRN: " + nrn);
        LOG.debug("Time: " + date);
        LOG.debug("Film: " + film);
        LOG.debug("Theatre: " + theatre);
        LOG.debug("----");

        // Retrieve all tickets for user at time (optionally, in theatre).
        if (film == null) {
            List<CinemaTicketEntity> tickets;
            if (theatre == null) {
                tickets = this.ticketService.getTickets(nrn, date);
            } else {
                tickets = this.ticketService.getTickets(nrn, date, theatre);
            }

            // Feed the log.
            LOG.debug("Found " + tickets.size() + " tickets:");
            LOG.debug(tickets);

            XStream xstream = new XStream();
            xstream.toXML(tickets, response.getWriter());
        }

        // Check whether there is a ticket for user at time in theatre for film.
        else {
            if (!this.ticketService.isValid(nrn, date, theatre, film)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The user has no tickets for this time and day.");
            }
        }

        LOG.debug("---- End Cinema Ticket Validation.");
    }
}
