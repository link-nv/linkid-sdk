package net.link.safeonline.attribute.provider.service;

import java.util.List;
import net.link.safeonline.attribute.provider.exception.SubjectNotFoundException;


/**
 * LinkID Subject Identifier service.
 *
 * @author sgdesmet
 */
public interface IdentifierService {

    void addSubjectIdentifier(String attributeName, String subjectIdentifier, String userId);

    /**
     *
     * @param attributeName
     * @param subjectIdentifier
     * @return The subject id
     * @throws SubjectNotFoundException
     */
    String getSubject(String attributeName, String subjectIdentifier)
            throws SubjectNotFoundException;

    String findSubject(String attributeName, String subjectIdentifier);

    /**
     * Removes subject identifiers within the given domain for the given user that have a different identifier than the given identifier.
     */
    void removeOtherSubjectIdentifiers(String attributeName, String identifier, String userId);

    /**
     * Removes all the subject identifiers for the given subject.
     */
    void removeSubjectIdentifiers(String userId);

    /**
     * Remove specified subject identifier.
     */
    void removeSubjectIdentifier(String userId, String attributeName, String identifier);

    /**
     * Returns list of subject identifiers (attribute names) for the given subject.
     * @param userId
     * @return List with attribute names that act as subject identifiers
     */
    List<String> getSubjectIdentifiers(String userId);
}
