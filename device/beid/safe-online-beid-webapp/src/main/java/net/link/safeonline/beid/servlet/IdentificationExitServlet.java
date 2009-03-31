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

import net.link.safeonline.model.node.util.AbstractNodeInjectionServlet;
import net.link.safeonline.util.servlet.annotation.In;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * This servlet redirects back to the calling web application.
 * 
 * @author fcorneli
 * 
 */
public class IdentificationExitServlet extends AbstractNodeInjectionServlet {

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

        LOG.debug("target: " + target);
        LOG.debug("name: " + name);
        LOG.debug("first name: " + firstname);
        LOG.debug("nationality: " + nationality);
        LOG.debug("sex: " + sex);
        LOG.debug("street: " + street);
        LOG.debug("house number: " + houseNr);
        LOG.debug("city: " + city);
        LOG.debug("zip: " + zip);
        LOG.debug("dob: " + dob);
        LOG.debug("hashed national number: " + hashedNationalNumber);

        PrintWriter writer = response.getWriter();
        writer.println("<html>");
        {
            writer.println("<body onload='document.myform.submit();'>");
            {
                writer.println("<form name='myform' action='" + target + "' method='POST'>");
                {
                    addField(writer, "name", name);
                    addField(writer, "firstname", firstname);
                    addField(writer, "nationality", nationality);
                    addField(writer, "sex", sex);
                    addField(writer, "street", street);
                    addField(writer, "houseNr", houseNr);
                    addField(writer, "city", city);
                    addField(writer, "zip", zip);
                    addField(writer, "dob", dob);
                    addField(writer, "hnnr", hashedNationalNumber);
                }
                writer.println("</form>");
            }
            writer.println("</body>");
        }
        writer.println("</html>");
    }

    private void addField(PrintWriter writer, String fieldName, String fieldValue) {

        writer.println("<input type='hidden' name='" + fieldName + "' value='" + fieldValue + "' />");
    }
}
