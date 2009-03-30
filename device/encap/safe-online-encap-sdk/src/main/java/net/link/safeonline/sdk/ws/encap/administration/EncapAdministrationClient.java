package net.link.safeonline.sdk.ws.encap.administration;

import java.rmi.RemoteException;


public interface EncapAdministrationClient {

    boolean lock(String mobile, String orgId)
            throws RemoteException;

    boolean unLock(String mobile, String orgId)
            throws RemoteException;

    boolean remove(String mobile, String orgId)
            throws RemoteException;

    String showStatus(String mobile, String orgId)
            throws RemoteException;

}
