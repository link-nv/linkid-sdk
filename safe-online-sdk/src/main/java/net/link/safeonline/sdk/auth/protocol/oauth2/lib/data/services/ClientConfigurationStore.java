package net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.services;

import net.link.safeonline.sdk.auth.protocol.oauth2.lib.data.objects.ClientConfiguration;
import net.link.safeonline.sdk.auth.protocol.oauth2.lib.exceptions.ClientNotFoundException;


/**
 * TODO description
 * <p/>
 * Date: 19/03/12
 * Time: 14:39
 *
 * @author: sgdesmet
 */
public interface ClientConfigurationStore {
    
    public ClientConfiguration getClient(String client_id) throws ClientNotFoundException;
    
    public boolean containsClient(String client_id);

}
