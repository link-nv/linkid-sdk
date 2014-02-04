package net.link.safeonline.sdk.api.ws.haws;

import net.link.safeonline.sdk.api.auth.LoginMode;
import net.link.safeonline.sdk.api.auth.StartPage;
import net.link.safeonline.sdk.api.haws.PullException;
import net.link.safeonline.sdk.api.haws.PushException;


/**
 * linkID HAWS authentication protocol WS client.
 * <p/>
 * Via this interface, service providers can push an authentication request to linkID and fetch the authentication response from it.
 */
public interface HawsServiceClient<Request, Response> {

    String push(Request request, String language, String theme, LoginMode loginMode, StartPage startPage)
            throws PushException;

    Response pull(String sessionId)
            throws PullException;
}
