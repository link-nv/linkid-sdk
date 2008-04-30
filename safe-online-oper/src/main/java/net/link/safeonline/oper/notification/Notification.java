/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.notification;

import java.util.List;

import javax.ejb.Local;
import javax.faces.model.SelectItem;

@Local
public interface Notification {

	void destroyCallback();

	void topicListFactory();

	void subscriptionListFactory();

	List<SelectItem> consumerListFactory();

	String view();

	String add();

	String addSubscription();

	String remove();

	String getAddress();

	void setAddress(String address);

	String getConsumer();

	void setConsumer(String consumer);

}
