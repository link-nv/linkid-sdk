package net.link.safeonline.sdk.example;

import java.io.*;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import net.link.safeonline.sdk.api.attribute.AttributeSDK;
import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.RequestConstants;
import net.link.safeonline.sdk.auth.filter.LoginManager;
import net.link.safeonline.sdk.auth.protocol.AuthnProtocolResponseContext;
import net.link.safeonline.sdk.auth.protocol.ProtocolManager;
import net.link.safeonline.sdk.configuration.WebConfig;
import net.link.util.error.ValidationFailedException;
import net.link.util.servlet.ErrorMessage;
import net.link.util.servlet.ServletUtils;


/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 28/02/12
 * Time: 13:09
 * To change this template use File | Settings | File Templates.
 */
public class HelloWorld extends HttpServlet {
    
    //an attribute
    public static final String SURNAME_ATTRIBUTE = "profile.givenName";
    
    public static final String LOGIN_JS_LOCATION = "http://192.168.5.3:8080/linkid-auth/resources/common/js/linkid.login-min.js";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession currentSession = request.getSession();


        //log out?
        if (request.getParameter( "logout" ) != null && request.getParameter( "logout" ).equals( "true" )){
            currentSession.invalidate();
        }

        boolean authenticated = LoginManager.isAuthenticated( currentSession );
        Map<String, List<AttributeSDK<Serializable>>> attributes = LoginManager.findAttributes( currentSession );
        String userName;
        if (attributes != null && attributes.containsKey( SURNAME_ATTRIBUTE )){
            userName = (String)attributes.get(SURNAME_ATTRIBUTE).get(0).getValue();
        } else {
            userName = "unknown user";
        }
        
        response.setContentType( "text/html" );
        PrintWriter out = response.getWriter();
        out.println( "<html>" );
        out.println( "<head>" );
        out.println( "<script type=\"text/javascript\" id=\"linkid-login-script\" src=\"" + LOGIN_JS_LOCATION +"\"></script>");
        out.println( "</head>" );
        out.println( "<body>" );
        if (!authenticated){
            out.println( "<div><a href=\"/startlogin?return_uri=/helloworld\">Simple Login</a></div>" ); //startlogin path is defined in web.xml!
            out.println( "<div><a href=\"#\" class=\"linkid-login\" login-mode=\"framed\" redirect-to-on-complete=\"/helloworld\">Framed Login</a></div>");
            out.println( "<div><a href=\"#\" class=\"linkid-login\" login-mode=\"framed_no_breakframe\" redirect-to-on-complete=\"/helloworld\">Framed (no breakframe) Login</a></div>");
            out.println( "<div><a href=\"#\" class=\"linkid-login\" login-mode=\"popup\" redirect-to-on-complete=\"/helloworld\">Popup Login</a></div>");
            out.println( "<div><a href=\"#\" class=\"linkid-login\" login-mode=\"popup\" redirect-to-on-complete=\"/helloworld\">Popup Login (no close, but will be closed by loginscript on this page)</a></div>");
        }
        else{
            out.println( "<a href=\"/startlogout\">Single sign-on logout</a>" );
            out.println( "<a href=\"/helloworld?logout=true\">Simple logout</a>" );
        }
        out.println( "<p>Hello  " + userName + " </p>" );
        out.println( "<p>Authenticated:  " + authenticated + " </p>" );
        out.println( "</body>" );
        out.println( "</html>" );
    }

}
