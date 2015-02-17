package net.link.safeonline.sdk.api.ws.wallet;

import java.io.Serializable;
import java.util.List;


/**
 * Wallet enroll result. Contains list of unknown users and a list of users who were already enrolled.
 * If both lists are empty, all went ok, else ones not in 2 lists were ok
 * <p/>
 * Created by wvdhaute
 * Date: 17/02/15
 * Time: 15:21
 */
public class WalletEnrollResult implements Serializable {

    private final List<String> unknownUsers;
    private final List<String> alreadyEnrolledUsers;

    public WalletEnrollResult(final List<String> unknownUsers, final List<String> alreadyEnrolledUsers) {

        this.unknownUsers = unknownUsers;
        this.alreadyEnrolledUsers = alreadyEnrolledUsers;
    }

    public List<String> getUnknownUsers() {

        return unknownUsers;
    }

    public List<String> getAlreadyEnrolledUsers() {

        return alreadyEnrolledUsers;
    }
}
