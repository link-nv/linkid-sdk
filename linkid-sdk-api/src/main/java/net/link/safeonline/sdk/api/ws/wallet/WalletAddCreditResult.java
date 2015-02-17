package net.link.safeonline.sdk.api.ws.wallet;

import java.io.Serializable;
import java.util.List;


/**
 * Wallet add credit result. Contains list of unknown users and a list of users who were not enrolled.
 * If both lists are empty, all went ok, else ones not in 2 lists were ok
 * <p/>
 * Created by wvdhaute
 * Date: 17/02/15
 * Time: 15:21
 */
public class WalletAddCreditResult implements Serializable {

    private final List<String> unknownUsers;
    private final List<String> notEnrolledUsers;

    public WalletAddCreditResult(final List<String> unknownUsers, final List<String> notEnrolledUsers) {

        this.unknownUsers = unknownUsers;
        this.notEnrolledUsers = notEnrolledUsers;
    }

    public List<String> getUnknownUsers() {

        return unknownUsers;
    }

    public List<String> getNotEnrolledUsers() {

        return notEnrolledUsers;
    }
}
