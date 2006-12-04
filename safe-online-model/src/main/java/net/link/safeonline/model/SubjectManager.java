package net.link.safeonline.model;

import javax.ejb.Local;

import net.link.safeonline.entity.SubjectEntity;

@Local
public interface SubjectManager {

	SubjectEntity getCallerSubject();
}
