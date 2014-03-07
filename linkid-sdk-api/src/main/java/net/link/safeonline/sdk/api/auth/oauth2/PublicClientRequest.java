/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.api.auth.oauth2;

import java.io.Serializable;
import java.util.Date;


/**
 * <p/>
 * Date: 08/06/12
 * Time: 10:22
 *
 * @author sgdesmet
 */
public class PublicClientRequest implements Serializable {

    private String clientAccessEntityId;

    private String deviceSpecificName;

    private long applicationId;

    private long clientConfigurationId;

    private String clientConfigurationFriendlyName;

    private Date created;

    public PublicClientRequest() {

    }

    public PublicClientRequest(final String clientAccessEntityId, final String deviceSpecificName, final long applicationId, final long clientConfigurationId,
                               final String clientConfigurationFriendlyName, Date created) {

        this.clientAccessEntityId = clientAccessEntityId;
        this.deviceSpecificName = deviceSpecificName;
        this.applicationId = applicationId;
        this.clientConfigurationId = clientConfigurationId;
        this.clientConfigurationFriendlyName = clientConfigurationFriendlyName;
        this.created = created;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(final Date created) {

        this.created = created;
    }

    public String getDeviceSpecificName() {

        return deviceSpecificName;
    }

    public void setDeviceSpecificName(final String deviceSpecificName) {

        this.deviceSpecificName = deviceSpecificName;
    }

    public long getApplicationId() {

        return applicationId;
    }

    public void setApplicationId(final long applicationId) {

        this.applicationId = applicationId;
    }

    public long getClientConfigurationId() {

        return clientConfigurationId;
    }

    public void setClientConfigurationId(final long clientConfigurationId) {

        this.clientConfigurationId = clientConfigurationId;
    }

    public String getClientAccessEntityId() {

        return clientAccessEntityId;
    }

    public void setClientAccessEntityId(final String clientAccessEntityId) {

        this.clientAccessEntityId = clientAccessEntityId;
    }

    public String getClientConfigurationFriendlyName() {

        return clientConfigurationFriendlyName;
    }

    public void setClientConfigurationFriendlyName(final String clientConfigurationFriendlyName) {

        this.clientConfigurationFriendlyName = clientConfigurationFriendlyName;
    }
}
