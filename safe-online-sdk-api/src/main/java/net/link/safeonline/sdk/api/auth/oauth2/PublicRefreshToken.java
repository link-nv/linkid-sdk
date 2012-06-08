package net.link.safeonline.sdk.api.auth.oauth2;

import java.util.Date;


/**
 * TODO description
 * <p/>
 * Date: 08/06/12
 * Time: 10:22
 *
 * @author: sgdesmet
 */
public class PublicRefreshToken {

    private long id;

    private String deviceSpecificName;

    private Date expirationDate;

    private Boolean invalidated;

    private long applicationId;

    private long clientConfigurationId;

    private String clientConfigurationFriendlyName;

    private String clientAccessEntityId;

    public PublicRefreshToken() {

    }

    public PublicRefreshToken(final long id, final String deviceSpecificName, final Date expirationDate, final Boolean invalidated,
                              final long applicationId, final long clientConfigurationId, final String clientConfigurationFriendlyName,
                              final String clientAccessEntityId) {

        this.id = id;
        this.deviceSpecificName = deviceSpecificName;
        this.expirationDate = expirationDate;
        this.invalidated = invalidated;
        this.applicationId = applicationId;
        this.clientConfigurationId = clientConfigurationId;
        this.clientConfigurationFriendlyName = clientConfigurationFriendlyName;
        this.clientAccessEntityId = clientAccessEntityId;
    }

    public long getId() {

        return id;
    }

    public void setId(final long id) {

        this.id = id;
    }

    public String getDeviceSpecificName() {

        return deviceSpecificName;
    }

    public void setDeviceSpecificName(final String deviceSpecificName) {

        this.deviceSpecificName = deviceSpecificName;
    }

    public Date getExpirationDate() {

        return expirationDate;
    }

    public void setExpirationDate(final Date expirationDate) {

        this.expirationDate = expirationDate;
    }

    public Boolean getInvalidated() {

        return invalidated;
    }

    public void setInvalidated(final Boolean invalidated) {

        this.invalidated = invalidated;
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
