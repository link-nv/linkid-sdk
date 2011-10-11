package net.link.safeonline.attribute.provider.service;

/**
 * Created by IntelliJ IDEA.
 * User: sgdesmet
 * Date: 04/10/11
 * Time: 11:12
 * To change this template use File | Settings | File Templates.
 */
public interface EmailService {

    public void sendEmail(String to, String subject, String message);
    
}
