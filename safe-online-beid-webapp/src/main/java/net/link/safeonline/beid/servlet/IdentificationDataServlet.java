/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.Out;
import net.link.safeonline.util.servlet.annotation.RequestParameter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Servlet that processes the data that comes from the client-side identification applet.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationDataServlet extends AbstractInjectionServlet {

    private static final long   serialVersionUID                         = 1L;

    private static final Log    LOG                                      = LogFactory.getLog(IdentificationDataServlet.class);

    public static final String  NAME_SESSION_ATTRIBUTE                   = "id-name";

    @RequestParameter("name")
    @Out(NAME_SESSION_ATTRIBUTE)
    private String              name;

    public static final String  FIRST_NAME_SESSION_ATTRIBUTE             = "id-firstname";

    @RequestParameter("firstname")
    @Out(FIRST_NAME_SESSION_ATTRIBUTE)
    private String              firstName;

    public static final String  DOB_SESSION_ATTRIBUTE                    = "id-dob";

    @RequestParameter("dob")
    @Out(DOB_SESSION_ATTRIBUTE)
    private String              dob;

    public static final String  NATIONALITY_SESSION_ATTRIBUTE            = "id-nationality";

    @RequestParameter("nationality")
    @Out(NATIONALITY_SESSION_ATTRIBUTE)
    private String              nationality;

    public static final String  SEX_SESSION_ATTRIBUTE                    = "id-sex";

    @RequestParameter("sex")
    @Out(SEX_SESSION_ATTRIBUTE)
    private String              sex;

    @RequestParameter("street")
    private String              street;

    public static final String  STREET_SESSION_ATTRIBUTE                 = "id-street";

    public static final String  HOUSE_NR_SESSION_ATTRIBUTE               = "id-housenr";

    @Out(STREET_SESSION_ATTRIBUTE)
    private String              outStreet;

    @Out(HOUSE_NR_SESSION_ATTRIBUTE)
    private String              houseNumber;

    public static final String  CITY_SESSION_ATTRIBUTE                   = "id-city";

    @RequestParameter("city")
    @Out(CITY_SESSION_ATTRIBUTE)
    private String              city;

    public static final String  ZIP_SESSION_ATTRIBUTE                    = "id-zip";

    @RequestParameter("zip")
    @Out(ZIP_SESSION_ATTRIBUTE)
    private String              zip;

    @RequestParameter("nnr")
    private String              nationalNumber;

    public static final String  HASHED_NATIONAL_NUMBER_SESSION_ATTRIBUTE = "id-hash-nnr";

    @Out(HASHED_NATIONAL_NUMBER_SESSION_ATTRIBUTE)
    private String              hashedNationalNumber;

    private static final String NATIONAL_NUMBER_SEED                     = "00cd4de51be6d556f98f40a1a69f7bcbd4fb75c1";


    @Override
    protected void invokeGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    @Override
    protected void invokePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        invoke(request, response);
    }

    private void invoke(@SuppressWarnings("unused") HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {

        LOG.debug("name: " + name);
        LOG.debug("first name: " + firstName);
        LOG.debug("dob: " + dob);
        LOG.debug("nationality: " + nationality);
        LOG.debug("sex: " + sex);
        LOG.debug("street: " + street);
        LOG.debug("city: " + city);
        LOG.debug("zip: " + zip);

        hashedNationalNumber = DigestUtils.shaHex(nationalNumber + NATIONAL_NUMBER_SEED);
        LOG.debug("hashed national number: " + hashedNationalNumber);

        int digitIdx = getFirstDigitIndex(street);
        outStreet = street.substring(0, digitIdx).trim();
        houseNumber = street.substring(digitIdx);
        LOG.debug("street: " + outStreet);
        LOG.debug("house number: " + houseNumber);
    }

    private int getFirstDigitIndex(String str) {

        char[] chars = str.toCharArray();
        int length = chars.length;
        for (int idx = 0; idx < length; idx++) {
            if (Character.isDigit(chars[idx]))
                return idx;
        }
        return -1;
    }
}
