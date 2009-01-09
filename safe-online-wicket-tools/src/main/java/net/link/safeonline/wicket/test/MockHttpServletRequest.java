/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/**
 * <h2>{@link MockHttpServletRequest}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Oct 8, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class MockHttpServletRequest implements HttpServletRequest {

    private HttpServletRequest base;
    private String             requestURI;
    private Method             method;


    enum Method {
        POST,
        GET
    }


    public MockHttpServletRequest(HttpServletRequest base, Method method) {

        this(base, base.getRequestURI(), method);
    }

    public MockHttpServletRequest(HttpServletRequest base, String requestURI, Method method) {

        this.base = base;
        this.method = method;
        this.requestURI = requestURI;
    }

    /**
     * {@inheritDoc}
     */
    public String getAuthType() {

        return base.getAuthType();
    }

    /**
     * {@inheritDoc}
     */
    public String getContextPath() {

        return base.getContextPath();
    }

    /**
     * {@inheritDoc}
     */
    public Cookie[] getCookies() {

        return base.getCookies();
    }

    /**
     * {@inheritDoc}
     */
    public long getDateHeader(String header) {

        return base.getDateHeader(header);
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(String name) {

        return base.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {

        return base.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {

        return base.getHeaders(name);
    }

    /**
     * {@inheritDoc}
     */
    public int getIntHeader(String name) {

        return base.getIntHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getMethod() {

        return method.name();
    }

    /**
     * {@inheritDoc}
     */
    public String getPathInfo() {

        return base.getPathInfo();
    }

    /**
     * {@inheritDoc}
     */
    public String getPathTranslated() {

        return base.getPathTranslated();
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {

        return base.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteUser() {

        return base.getRemoteUser();
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestURI() {

        return requestURI;
    }

    /**
     * {@inheritDoc}
     */
    public StringBuffer getRequestURL() {

        return new StringBuffer(requestURI);
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestedSessionId() {

        return base.getRequestedSessionId();
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {

        return base.getServletPath();
    }

    /**
     * {@inheritDoc}
     */
    public HttpSession getSession() {

        return base.getSession();
    }

    /**
     * {@inheritDoc}
     */
    public HttpSession getSession(boolean create) {

        return base.getSession(create);
    }

    /**
     * {@inheritDoc}
     */
    public Principal getUserPrincipal() {

        return base.getUserPrincipal();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdFromCookie() {

        return base.isRequestedSessionIdFromCookie();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdFromURL() {

        return base.isRequestedSessionIdFromURL();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdValid() {

        return base.isRequestedSessionIdValid();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUserInRole(String role) {

        return base.isUserInRole(role);
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String name) {

        return base.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {

        return base.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    public String getCharacterEncoding() {

        return base.getCharacterEncoding();
    }

    /**
     * {@inheritDoc}
     */
    public int getContentLength() {

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    public String getContentType() {

        return base.getContentType();
    }

    /**
     * {@inheritDoc}
     */
    public ServletInputStream getInputStream()
            throws IOException {

        throw new IOException();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalAddr() {

        return base.getLocalAddr();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {

        return base.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    public int getLocalPort() {

        return base.getLocalPort();
    }

    /**
     * {@inheritDoc}
     */
    public Locale getLocale() {

        return base.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {

        return base.getLocales();
    }

    /**
     * {@inheritDoc}
     */
    public String getParameter(String name) {

        return base.getParameter(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {

        return base.getParameterMap();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {

        return base.getParameterNames();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameterValues(String name) {

        return base.getParameterValues(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocol() {

        return base.getProtocol();
    }

    /**
     * {@inheritDoc}
     */
    public BufferedReader getReader()
            throws IOException {

        throw new IOException();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteAddr() {

        return base.getRemoteAddr();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteHost() {

        return base.getRemoteHost();
    }

    /**
     * {@inheritDoc}
     */
    public int getRemotePort() {

        return base.getRemotePort();
    }

    /**
     * {@inheritDoc}
     */
    public RequestDispatcher getRequestDispatcher(String name) {

        return base.getRequestDispatcher(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getScheme() {

        return base.getScheme();
    }

    /**
     * {@inheritDoc}
     */
    public String getServerName() {

        return base.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    public int getServerPort() {

        return base.getServerPort();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSecure() {

        return base.isSecure();
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(String name) {

        base.removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String name, Object value) {

        base.setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void setCharacterEncoding(String encoding)
            throws UnsupportedEncodingException {

        base.setCharacterEncoding(encoding);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {

        return base.isRequestedSessionIdFromUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String getRealPath(String name) {

        return base.getRealPath(name);
    }
}
