package net.link.safeonline.attribute.provider.service;

/**
 * Provides functionality towards attribute providers concerning subject specific information.
 */
public interface SubjectService {

    /**
     * @param userId          subject's user ID
     * @param applicationName the application name
     *
     * @return the # of authentications the subject has successfully completed for the application.
     */
    int getAuthentications(String userId, String applicationName);
}
