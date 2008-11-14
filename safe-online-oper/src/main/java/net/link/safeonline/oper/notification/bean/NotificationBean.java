/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.oper.notification.bean;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ejb.Remove;
import javax.ejb.Stateful;
import javax.faces.application.FacesMessage;
import javax.faces.model.SelectItem;
import javax.interceptor.Interceptors;

import net.link.safeonline.authentication.exception.EndpointReferenceNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SubscriptionNotFoundException;
import net.link.safeonline.authentication.service.ApplicationService;
import net.link.safeonline.authentication.service.NodeService;
import net.link.safeonline.ctrl.error.ErrorMessageInterceptor;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.NodeEntity;
import net.link.safeonline.entity.notification.EndpointReferenceEntity;
import net.link.safeonline.entity.notification.NotificationProducerSubscriptionEntity;
import net.link.safeonline.notification.service.NotificationSubscriptionService;
import net.link.safeonline.oper.OperatorConstants;
import net.link.safeonline.oper.notification.Notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.security.SecurityDomain;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Destroy;
import org.jboss.seam.annotations.Factory;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Out;
import org.jboss.seam.annotations.datamodel.DataModel;
import org.jboss.seam.annotations.datamodel.DataModelSelection;
import org.jboss.seam.faces.FacesMessages;


@Name("operNotification")
@Stateful
@LocalBinding(jndiBinding = Notification.JNDI_BINDING)
@SecurityDomain(OperatorConstants.SAFE_ONLINE_OPER_SECURITY_DOMAIN)
@Interceptors(ErrorMessageInterceptor.class)
public class NotificationBean implements Notification {

    private static final Log                             LOG                         = LogFactory.getLog(NotificationBean.class);

    private static final String                          OPER_TOPIC_LIST_NAME        = "operTopicList";

    private static final String                          OPER_SUBSCRIPTION_LIST_NAME = "operSubscriptionList";

    private static final String                          OPER_CONSUMERS_LIST_NAME    = "consumers";

    @In(create = true)
    FacesMessages                                        facesMessages;

    @EJB(mappedName = NotificationSubscriptionService.JNDI_BINDING)
    private NotificationSubscriptionService              notificationSubscriptionService;

    @EJB(mappedName = ApplicationService.JNDI_BINDING)
    private ApplicationService                           applicationService;

    @EJB(mappedName = NodeService.JNDI_BINDING)
    private NodeService                                  nodeService;

    private String                                       address;

    private String                                       consumer;

    @SuppressWarnings("unused")
    @DataModel(OPER_TOPIC_LIST_NAME)
    private List<NotificationProducerSubscriptionEntity> topicList;

    @Out(value = "selectedTopic", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    @DataModelSelection(OPER_TOPIC_LIST_NAME)
    private NotificationProducerSubscriptionEntity       selectedTopic;

    @SuppressWarnings("unused")
    @DataModel(OPER_SUBSCRIPTION_LIST_NAME)
    private Set<EndpointReferenceEntity>                 subscriptionList;

    @Out(value = "selectedSubscription", required = false, scope = ScopeType.SESSION)
    @In(required = false)
    @DataModelSelection(OPER_SUBSCRIPTION_LIST_NAME)
    private EndpointReferenceEntity                      selectedSubscription;


    @Remove
    @Destroy
    public void destroyCallback() {

        this.address = null;
    }

    @Factory(OPER_TOPIC_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void topicListFactory() {

        LOG.debug("topic list factory");
        this.topicList = this.notificationSubscriptionService.listTopics();
    }

    @Factory(OPER_SUBSCRIPTION_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void subscriptionListFactory()
            throws SubscriptionNotFoundException {

        LOG.debug("subscription list factory for topic: " + this.selectedTopic);
        this.subscriptionList = this.notificationSubscriptionService.listSubscriptions(this.selectedTopic.getTopic());
    }

    @Factory(OPER_CONSUMERS_LIST_NAME)
    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public List<SelectItem> consumerListFactory() {

        LOG.debug("consumer list factory");
        List<SelectItem> consumerList = new LinkedList<SelectItem>();
        List<ApplicationEntity> applications = this.applicationService.listApplications();
        for (ApplicationEntity application : applications) {
            consumerList.add(new SelectItem(application.getName()));
        }
        List<NodeEntity> nodes = this.nodeService.listNodes();
        for (NodeEntity node : nodes) {
            consumerList.add(new SelectItem(node.getName()));
        }
        return consumerList;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String view() {

        LOG.debug("view subscriptions on topic: " + this.selectedTopic.getTopic());
        return "view";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String add() {

        LOG.debug("add subscription");
        return "add";
    }

    public String addSubscription()
            throws SubscriptionNotFoundException {

        LOG.debug("add subscription for consumer " + this.consumer);
        try {
            this.notificationSubscriptionService.addSubscription(this.selectedTopic.getTopic(), this.address, this.consumer);
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(), this.consumer);
            return null;
        }
        subscriptionListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String remove()
            throws SubscriptionNotFoundException {

        LOG.debug("remove subscription for topic " + this.selectedTopic.getTopic());
        try {
            this.notificationSubscriptionService.removeSubscription(this.selectedTopic.getTopic(), this.selectedSubscription);
        } catch (EndpointReferenceNotFoundException e) {
            LOG.debug("endpoint not found: " + this.selectedSubscription.getName());
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, "errorConsumerNotFound",
                    this.selectedSubscription.getName());
            return null;
        } catch (PermissionDeniedException e) {
            LOG.debug("permission denied: " + e.getMessage());
            this.facesMessages.addFromResourceBundle(FacesMessage.SEVERITY_ERROR, e.getResourceMessage(),
                    this.selectedSubscription.getName());
            return null;
        }
        subscriptionListFactory();
        return "success";
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getAddress() {

        return this.address;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setAddress(String address) {

        this.address = address;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public String getConsumer() {

        return this.consumer;
    }

    @RolesAllowed(OperatorConstants.OPERATOR_ROLE)
    public void setConsumer(String consumer) {

        this.consumer = consumer;
    }

}
