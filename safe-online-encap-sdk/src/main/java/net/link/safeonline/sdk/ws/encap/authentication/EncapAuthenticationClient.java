package net.link.safeonline.sdk.ws.encap.authentication;

import java.rmi.RemoteException;


public interface EncapAuthenticationClient {

    boolean cancelSession(String sessionId)
            throws RemoteException;

    String challenge(String mobile, String orgId)
            throws RemoteException;

    boolean verifyOTP(String challengeId, String OTPValue)
            throws RemoteException;
}
