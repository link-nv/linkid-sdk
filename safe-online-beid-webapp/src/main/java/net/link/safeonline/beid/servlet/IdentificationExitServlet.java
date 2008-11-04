/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.beid.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.link.safeonline.util.servlet.AbstractInjectionServlet;
import net.link.safeonline.util.servlet.annotation.In;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This servlet redirects back to the calling web application.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationExitServlet extends AbstractInjectionServlet {

    private static final long serialVersionUID = 1L;

    private static final Log  LOG              = LogFactory.getLog(IdentificationExitServlet.class);

    @In("target")
    private String            target;

    @In(IdentificationDataServlet.NAME_SESSION_ATTRIBUTE)
    private String            name;

    @In(IdentificationDataServlet.FIRST_NAME_SESSION_ATTRIBUTE)
    private String            firstname;

    @In(IdentificationDataServlet.NATIONALITY_SESSION_ATTRIBUTE)
    private String            nationality;

    @In(IdentificationDataServlet.SEX_SESSION_ATTRIBUTE)
    private String            sex;

    @In(IdentificationDataServlet.CITY_SESSION_ATTRIBUTE)
    private String            city;

    @In(IdentificationDataServlet.ZIP_SESSION_ATTRIBUTE)
    private String            zip;

    @In(IdentificationDataServlet.DOB_SESSION_ATTRIBUTE)
    private String            dob;

    @In(IdentificationDataServlet.STREET_SESSION_ATTRIBUTE)
    private String            street;

    @In(IdentificationDataServlet.HOUSE_NR_SESSION_ATTRIBUTE)
    private String            houseNr;

    @In(IdentificationDataServlet.HASHED_NATIONAL_NUMBER_SESSION_ATTRIBUTE)
    private String            hashedNationalNumber;


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

    private void invoke(@SuppressWarnings("unused") HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        LOG.debug("target: " + this.target);
        LOG.debug("name: " + this.name);
        LOG.debug("first name: " + this.firstname);
        LOG.debug("nationality: " + this.nationality);
        LOG.debug("sex: " + this.sex);
        LOG.debug("street: " + this.street);
        LOG.debug("house number: " + this.houseNr);
        LOG.debug("city: " + this.city);
        LOG.debug("zip: " + this.zip);
        LOG.debug("dob: " + this.dob);
        LOG.debug("hashed national number: " + this.hashedNationalNumber);

        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        {
            writer.println("<body onload=\"document.myform.submit();\">");
            {
                writer.println("<form name=\"myform\" action=\"" + this.target + "\" method=\"POST\">");
                {
                    addField(writer, "name", this.name);
                    addField(writer, "firstname", this.firstname);
                    addField(writer, "nationality", this.nationality);
                    addField(writer, "sex", this.sex);
                    addField(writer, "street", this.street);
                    addField(writer, "houseNr", this.houseNr);
                    addField(writer, "city", this.city);
                    addField(writer, "zip", this.zip);
                    addField(writer, "dob", this.dob);
                    addField(writer, "hnnr", this.hashedNationalNumber);
                }
                writer.println("</form>");
            }
            writer.println("</body>");
        }
        writer.println("</html>");
    }

    private void addField(PrintWriter writer, String fieldName, String fieldValue) {

        writer.println("<input type=\"hidden\" name=\"" + fieldName + "\" value=\"" + fieldValue + "\"/>");
    }
}
