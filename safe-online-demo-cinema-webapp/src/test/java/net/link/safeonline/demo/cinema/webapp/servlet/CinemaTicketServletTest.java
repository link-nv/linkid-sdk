/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.cinema.webapp.servlet;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import net.link.safeonline.demo.cinema.service.InitializationService;
import net.link.safeonline.demo.cinema.service.TicketService;
import net.link.safeonline.demo.cinema.webapp.servlet.CinemaTicketServlet;
import net.link.safeonline.test.util.JndiTestUtils;
import net.link.safeonline.test.util.ServletTestManager;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class CinemaTicketServletTest extends TestCase {

    private static final Log   LOG = LogFactory.getLog(CinemaTicketServletTest.class);

    private ServletTestManager servletTestManager;

    private String             servletLocation;

    private JndiTestUtils      jndiTestUtils;

    private TicketService      mockTicketService;


    @Override
    protected void setUp() throws Exception {

        super.setUp();

        this.jndiTestUtils = new JndiTestUtils();
        this.jndiTestUtils.setUp();
        this.mockTicketService = createMock(TicketService.class);
        this.jndiTestUtils.bindComponent(TicketService.JNDI_BINDING, this.mockTicketService);

        this.servletTestManager = new ServletTestManager();
        this.servletTestManager.setUp(CinemaTicketServlet.class);

        this.servletLocation = this.servletTestManager.getServletLocation();
    }

    @Override
    protected void tearDown() throws Exception {

        this.servletTestManager.tearDown();
        this.jndiTestUtils.tearDown();
        super.tearDown();
    }

    public void testDoGetWithValidPass() throws Exception {

        // Setup
        String testNrn = UUID.randomUUID().toString(); // User's NRN.
        int filmId = 0; // The index of the film in the initialization service.
        int theatreThatPlaysFilmId = 0; // The i'th theatre that plays the film.
        long testTime = new Date().getTime() / 1000; // Time of user's ticket check *IN SECONDS: UNIX TIMESTAMP*.
        Date testDate = new Date(testTime * 1000); // Date of user's ticket check (IN MILLISECONDS).

        // Resolve film & theatre names.
        String testFilm = InitializationService.filmNames[filmId];
        String testTheatre = InitializationService.theatreNames[InitializationService.filmTheatres[filmId][theatreThatPlaysFilmId]];

        // Setup Mock.
        expect(this.mockTicketService.isValid(testNrn, testDate, testTheatre, testFilm)).andReturn(true);
        replay(this.mockTicketService);

        // Validate ticket.
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.servletLocation);
        getMethod.setQueryString(new NameValuePair[] { new NameValuePair(CinemaTicketServlet.NRN, testNrn),
                new NameValuePair(CinemaTicketServlet.TIME, Long.toString(testTime)),
                new NameValuePair(CinemaTicketServlet.FILM, testFilm), new NameValuePair(CinemaTicketServlet.THEATRE, testTheatre) });
        int result = httpClient.executeMethod(getMethod);

        // Verify result.
        LOG.debug("result: " + result);
        verify(this.mockTicketService);
        assertEquals(HttpServletResponse.SC_OK, result);
    }

    public void testDoGetWithInvalidPass() throws Exception {

        // Setup
        String testNrn = UUID.randomUUID().toString(); // User's NRN.
        int filmId = 0; // The index of the film in the initialization service.
        int theatreThatPlaysFilmId = 0; // The i'th theatre that plays the film.
        long testTime = new Date().getTime() / 1000; // Time of user's ticket check *IN SECONDS: UNIX TIMESTAMP*.
        Date testDate = new Date(testTime * 1000); // Date of user's ticket check (IN MILLISECONDS).

        // Resolve film & theatre names.
        String testFilm = InitializationService.filmNames[filmId];
        String testTheatre = InitializationService.theatreNames[InitializationService.filmTheatres[filmId][theatreThatPlaysFilmId]];

        // Setup Mock.
        expect(this.mockTicketService.isValid(testNrn, testDate, testTheatre, testFilm)).andReturn(false);
        replay(this.mockTicketService);

        // Validate ticket.
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.servletLocation);
        getMethod.setQueryString(new NameValuePair[] { new NameValuePair(CinemaTicketServlet.NRN, testNrn),
                new NameValuePair(CinemaTicketServlet.TIME, Long.toString(testTime)),
                new NameValuePair(CinemaTicketServlet.FILM, testFilm), new NameValuePair(CinemaTicketServlet.THEATRE, testTheatre) });
        int result = httpClient.executeMethod(getMethod);

        // Verify result.
        LOG.debug("result: " + result);
        verify(this.mockTicketService);
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, result);
    }

    public void testDoGetWithoutValidParams() throws Exception {

        // Setup Mock.
        replay(this.mockTicketService);

        // Validate ticket.
        HttpClient httpClient = new HttpClient();
        GetMethod getMethod = new GetMethod(this.servletLocation);
        int result = httpClient.executeMethod(getMethod);

        // Verify Result.
        LOG.debug("result: " + result);
        verify(this.mockTicketService);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, result);
    }
}
