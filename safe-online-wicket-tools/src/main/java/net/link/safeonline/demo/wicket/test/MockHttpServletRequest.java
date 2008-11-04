/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.wicket.test;

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

        return this.base.getAuthType();
    }

    /**
     * {@inheritDoc}
     */
    public String getContextPath() {

        return this.base.getContextPath();
    }

    /**
     * {@inheritDoc}
     */
    public Cookie[] getCookies() {

        return this.base.getCookies();
    }

    /**
     * {@inheritDoc}
     */
    public long getDateHeader(String header) {

        return this.base.getDateHeader(header);
    }

    /**
     * {@inheritDoc}
     */
    public String getHeader(String name) {

        return this.base.getHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getHeaderNames() {

        return this.base.getHeaderNames();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getHeaders(String name) {

        return this.base.getHeaders(name);
    }

    /**
     * {@inheritDoc}
     */
    public int getIntHeader(String name) {

        return this.base.getIntHeader(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getMethod() {

        return this.method.name();
    }

    /**
     * {@inheritDoc}
     */
    public String getPathInfo() {

        return this.base.getPathInfo();
    }

    /**
     * {@inheritDoc}
     */
    public String getPathTranslated() {

        return this.base.getPathTranslated();
    }

    /**
     * {@inheritDoc}
     */
    public String getQueryString() {

        return this.base.getQueryString();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteUser() {

        return this.base.getRemoteUser();
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestURI() {

        return this.requestURI;
    }

    /**
     * {@inheritDoc}
     */
    public StringBuffer getRequestURL() {

        return new StringBuffer(this.requestURI);
    }

    /**
     * {@inheritDoc}
     */
    public String getRequestedSessionId() {

        return this.base.getRequestedSessionId();
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {

        return this.base.getServletPath();
    }

    /**
     * {@inheritDoc}
     */
    public HttpSession getSession() {

        return this.base.getSession();
    }

    /**
     * {@inheritDoc}
     */
    public HttpSession getSession(boolean create) {

        return this.base.getSession(create);
    }

    /**
     * {@inheritDoc}
     */
    public Principal getUserPrincipal() {

        return this.base.getUserPrincipal();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdFromCookie() {

        return this.base.isRequestedSessionIdFromCookie();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdFromURL() {

        return this.base.isRequestedSessionIdFromURL();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isRequestedSessionIdValid() {

        return this.base.isRequestedSessionIdValid();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isUserInRole(String role) {

        return this.base.isUserInRole(role);
    }

    /**
     * {@inheritDoc}
     */
    public Object getAttribute(String name) {

        return this.base.getAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getAttributeNames() {

        return this.base.getAttributeNames();
    }

    /**
     * {@inheritDoc}
     */
    public String getCharacterEncoding() {

        return this.base.getCharacterEncoding();
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

        return this.base.getContentType();
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

        return this.base.getLocalAddr();
    }

    /**
     * {@inheritDoc}
     */
    public String getLocalName() {

        return this.base.getLocalName();
    }

    /**
     * {@inheritDoc}
     */
    public int getLocalPort() {

        return this.base.getLocalPort();
    }

    /**
     * {@inheritDoc}
     */
    public Locale getLocale() {

        return this.base.getLocale();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getLocales() {

        return this.base.getLocales();
    }

    /**
     * {@inheritDoc}
     */
    public String getParameter(String name) {

        return this.base.getParameter(name);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Map getParameterMap() {

        return this.base.getParameterMap();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    public Enumeration getParameterNames() {

        return this.base.getParameterNames();
    }

    /**
     * {@inheritDoc}
     */
    public String[] getParameterValues(String name) {

        return this.base.getParameterValues(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getProtocol() {

        return this.base.getProtocol();
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

        return this.base.getRemoteAddr();
    }

    /**
     * {@inheritDoc}
     */
    public String getRemoteHost() {

        return this.base.getRemoteHost();
    }

    /**
     * {@inheritDoc}
     */
    public int getRemotePort() {

        return this.base.getRemotePort();
    }

    /**
     * {@inheritDoc}
     */
    public RequestDispatcher getRequestDispatcher(String name) {

        return this.base.getRequestDispatcher(name);
    }

    /**
     * {@inheritDoc}
     */
    public String getScheme() {

        return this.base.getScheme();
    }

    /**
     * {@inheritDoc}
     */
    public String getServerName() {

        return this.base.getServerName();
    }

    /**
     * {@inheritDoc}
     */
    public int getServerPort() {

        return this.base.getServerPort();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSecure() {

        return this.base.isSecure();
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(String name) {

        this.base.removeAttribute(name);
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(String name, Object value) {

        this.base.setAttribute(name, value);
    }

    /**
     * {@inheritDoc}
     */
    public void setCharacterEncoding(String encoding)
            throws UnsupportedEncodingException {

        this.base.setCharacterEncoding(encoding);
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public boolean isRequestedSessionIdFromUrl() {

        return this.base.isRequestedSessionIdFromUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Deprecated
    @SuppressWarnings("deprecation")
    public String getRealPath(String name) {

        return this.base.getRealPath(name);
    }
}
