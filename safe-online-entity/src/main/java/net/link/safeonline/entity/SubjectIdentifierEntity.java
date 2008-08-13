/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.SubjectIdentifierEntity.DELETE_WHERE_IDENTIFIER;
import static net.link.safeonline.entity.SubjectIdentifierEntity.DELETE_WHERE_OTHER_IDENTIFIERS;
import static net.link.safeonline.entity.SubjectIdentifierEntity.DELETE_WHERE_SUBJECT;
import static net.link.safeonline.entity.SubjectIdentifierEntity.GET_WHERE_SUBJECT;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;


/**
 * Subject Identifier entity. This entity allows us to unambiguously map from an identifier within a certain domain to
 * its subject. For example, within the domain of Belgian eID, we will map from the SHA-1 of the encoded authentication
 * certificate to a subject.
 *
 * @author fcorneli
 *
 */
@Entity
@Table(name = "subject_identifier")
@NamedQueries( {
        @NamedQuery(name = DELETE_WHERE_OTHER_IDENTIFIERS, query = "DELETE FROM SubjectIdentifierEntity AS subjectIdentifier "
                + "WHERE subjectIdentifier.pk.domain = :domain AND "
                + "subjectIdentifier.subject = :subject AND "
                + "subjectIdentifier.pk.identifier <> :identifier"),
        @NamedQuery(name = DELETE_WHERE_SUBJECT, query = "DELETE FROM SubjectIdentifierEntity AS subjectIdentifier "
                + "WHERE subjectIdentifier.subject = :subject"),
        @NamedQuery(name = DELETE_WHERE_IDENTIFIER, query = "DELETE FROM SubjectIdentifierEntity AS subjectIdentifier "
                + "WHERE subjectIdentifier.subject = :subject AND " + "subjectIdentifier.pk.domain = :domain AND "
                + "subjectIdentifier.pk.identifier = :identifier"),
        @NamedQuery(name = GET_WHERE_SUBJECT, query = "FROM SubjectIdentifierEntity AS subjectIdentifier "
                + "WHERE subjectIdentifier.subject = :subject") })
public class SubjectIdentifierEntity implements Serializable {

    private static final long   serialVersionUID               = 1L;

    public static final String  DELETE_WHERE_OTHER_IDENTIFIERS = "sie.del";

    public static final String  DELETE_WHERE_SUBJECT           = "sie.del.subject";

    public static final String  DELETE_WHERE_IDENTIFIER        = "sie.del.id";

    public static final String  GET_WHERE_SUBJECT              = "sie.subject";

    private SubjectIdentifierPK pk;

    private SubjectEntity       subject;


    public SubjectIdentifierEntity() {

        // empty
    }

    public SubjectIdentifierEntity(String domain, String identifier, SubjectEntity subject) {

        this.pk = new SubjectIdentifierPK(domain, identifier);
        this.subject = subject;
    }

    @EmbeddedId
    public SubjectIdentifierPK getPk() {

        return this.pk;
    }

    public void setPk(SubjectIdentifierPK pk) {

        this.pk = pk;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "subject")
    public SubjectEntity getSubject() {

        return this.subject;
    }

    public void setSubject(SubjectEntity subject) {

        this.subject = subject;
    }


    public interface QueryInterface {

        @QueryMethod(GET_WHERE_SUBJECT)
        List<SubjectIdentifierEntity> getWhereSubject(@QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(DELETE_WHERE_OTHER_IDENTIFIERS)
        int deleteWhereOtherIdentifiers(@QueryParam("domain") String domain,
                @QueryParam("identifier") String identifier, @QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(DELETE_WHERE_SUBJECT)
        void deleteWhereSubject(@QueryParam("subject") SubjectEntity subject);

        @UpdateMethod(DELETE_WHERE_IDENTIFIER)
        void deleteWhereIdentifier(@QueryParam("subject") SubjectEntity subject, @QueryParam("domain") String domain,
                @QueryParam("identifier") String identifier);
    }
}
