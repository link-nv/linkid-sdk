/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.model.subject;

import net.link.safeonline.authentication.exception.AlreadySubscribedException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.dao.SubscriptionDAO;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.SubscriptionEntity;
import net.link.safeonline.entity.SubscriptionOwnerType;
import net.link.safeonline.model.application.Application;

/**
 * Domain Model class for Subject.
 * 
 * @author fcorneli
 * 
 */
public class Subject {

	private final SubjectEntity entity;

	private final SubjectContext context;

	/**
	 * Main constructor.
	 * 
	 * @param context
	 * @param entity
	 */
	public Subject(SubjectContext context, SubjectEntity entity) {
		this.context = context;
		this.entity = entity;
	}

	public SubjectEntity getSubjectEntity() {
		return this.entity;
	}

	/**
	 * Subscribes this subject on the given application.
	 * 
	 * @param application
	 * @throws PermissionDeniedException
	 * @throws AlreadySubscribedException
	 */
	public void subscribe(Application application)
			throws PermissionDeniedException, AlreadySubscribedException {

		application.checkUserSubscriptionPermission();
		checkAlreadySubscribed(application);

		this.context.getSubscriptionDAO().addSubscription(
				SubscriptionOwnerType.SUBJECT, this.entity,
				application.getEntity());
	}

	private void checkAlreadySubscribed(Application application)
			throws AlreadySubscribedException {
		if (isSubscribed(application)) {
			throw new AlreadySubscribedException();
		}
	}

	/**
	 * Unsubscribes this subject from the given application.
	 * 
	 * @param application
	 * @throws SubscriptionNotFoundException
	 * @throws PermissionDeniedException
	 */
	public void unsubscribe(Application application)
			throws SubscriptionNotFoundException, PermissionDeniedException {
		SubscriptionDAO subscriptionDAO = this.context.getSubscriptionDAO();
		ApplicationEntity applicationEntity = application.getEntity();
		SubscriptionEntity subscription = subscriptionDAO.findSubscription(
				this.entity, applicationEntity);
		if (null == subscription) {
			throw new SubscriptionNotFoundException();
		}
		if (!SubscriptionOwnerType.SUBJECT.equals(subscription
				.getSubscriptionOwnerType())) {
			throw new PermissionDeniedException();
		}
		subscriptionDAO.removeSubscription(this.entity, applicationEntity);
	}

	/**
	 * Checks whether this subject is subscribed onto the given application.
	 * 
	 * @param application
	 * @return
	 */
	public boolean isSubscribed(Application application) {
		SubscriptionEntity subscription = this.context.getSubscriptionDAO()
				.findSubscription(this.entity, application.getEntity());
		return null != subscription;
	}
}
