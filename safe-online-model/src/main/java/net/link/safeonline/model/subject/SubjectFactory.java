/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.subject;

import net.link.safeonline.entity.SubjectEntity;


/**
 * Factory for domain model Subject objects.
 *
 * @author fcorneli
 *
 */
public class SubjectFactory {

    private SubjectFactory() {

        // empty
    }

    /**
     * Gives back the subject object corresponding with the caller principal.
     *
     * @param subjectContext
     */
    public static Subject getCallerSubject(SubjectContext subjectContext) {

        SubjectEntity subjectEntity = subjectContext.getSubjectManager().getCallerSubject();
        Subject subject = new Subject(subjectContext, subjectEntity);
        return subject;
    }
}
