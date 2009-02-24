package net.link.safeonline.authentication.service.bean;

import java.lang.reflect.Array;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import net.link.safeonline.audit.ResourceAuditLogger;
import net.link.safeonline.audit.SecurityAuditLogger;
import net.link.safeonline.authentication.exception.AttributeTypeNotFoundException;
import net.link.safeonline.authentication.exception.AttributeUnavailableException;
import net.link.safeonline.authentication.exception.DatatypeMismatchException;
import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.authentication.exception.PermissionDeniedException;
import net.link.safeonline.authentication.exception.SafeOnlineResourceException;
import net.link.safeonline.authentication.exception.SubjectNotFoundException;
import net.link.safeonline.authentication.service.ProxyAttributeService;
import net.link.safeonline.authentication.service.ProxyAttributeServiceRemote;
import net.link.safeonline.dao.AttributeCacheDAO;
import net.link.safeonline.dao.AttributeDAO;
import net.link.safeonline.dao.AttributeTypeDAO;
import net.link.safeonline.data.CompoundAttributeDO;
import net.link.safeonline.entity.AttributeCacheEntity;
import net.link.safeonline.entity.AttributeEntity;
import net.link.safeonline.entity.AttributeTypeEntity;
import net.link.safeonline.entity.CompoundedAttributeTypeMemberEntity;
import net.link.safeonline.entity.DatatypeType;
import net.link.safeonline.entity.NodeMappingEntity;
import net.link.safeonline.entity.SubjectEntity;
import net.link.safeonline.entity.audit.ResourceLevelType;
import net.link.safeonline.entity.audit.ResourceNameType;
import net.link.safeonline.entity.audit.SecurityThreatType;
import net.link.safeonline.keystore.SafeOnlineNodeKeyStore;
import net.link.safeonline.model.bean.AttributeManagerLWBean;
import net.link.safeonline.osgi.OSGIService;
import net.link.safeonline.osgi.OSGIStartable;
import net.link.safeonline.osgi.OSGIConstants.OSGIServiceType;
import net.link.safeonline.osgi.exception.AttributeNotFoundException;
import net.link.safeonline.osgi.plugin.PluginAttributeService;
import net.link.safeonline.sdk.exception.RequestDeniedException;
import net.link.safeonline.sdk.ws.CompoundBuilder;
import net.link.safeonline.sdk.ws.attrib.AttributeClient;
import net.link.safeonline.sdk.ws.attrib.AttributeClientImpl;
import net.link.safeonline.sdk.ws.data.DataClient;
import net.link.safeonline.sdk.ws.data.DataClientImpl;
import net.link.safeonline.sdk.ws.exception.WSClientTransportException;
import net.link.safeonline.service.AttributeTypeService;
import net.link.safeonline.service.NodeMappingService;
import net.link.safeonline.service.SubjectService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;
import org.jboss.annotation.ejb.RemoteBinding;


@Stateless
@LocalBinding(jndiBinding = ProxyAttributeService.JNDI_BINDING)
@RemoteBinding(jndiBinding = ProxyAttributeServiceRemote.JNDI_BINDING)
public class ProxyAttributeServiceBean implements ProxyAttributeService, ProxyAttributeServiceRemote {

    private static final Log       LOG = LogFactory.getLog(ProxyAttributeServiceBean.class);

    @EJB(mappedName = AttributeTypeDAO.JNDI_BINDING)
    private AttributeTypeDAO       attributeTypeDAO;

    @EJB(mappedName = AttributeDAO.JNDI_BINDING)
    private AttributeDAO           attributeDAO;

    @EJB(mappedName = AttributeCacheDAO.JNDI_BINDING)
    private AttributeCacheDAO      attributeCacheDAO;

    @EJB(mappedName = SubjectService.JNDI_BINDING)
    private SubjectService         subjectService;

    @EJB(mappedName = OSGIStartable.JNDI_BINDING)
    private OSGIStartable          osgiStartable;

    @EJB(mappedName = ResourceAuditLogger.JNDI_BINDING)
    private ResourceAuditLogger    resourceAuditLogger;

    @EJB(mappedName = SecurityAuditLogger.JNDI_BINDING)
    private SecurityAuditLogger    securityAuditLogger;

    @EJB(mappedName = NodeMappingService.JNDI_BINDING)
    private NodeMappingService     nodeMappingService;

    @EJB(mappedName = AttributeTypeService.JNDI_BINDING)
    private AttributeTypeService   attributeTypeService;

    private AttributeManagerLWBean attributeManager;


    @PostConstruct
    public void postConstructCallback() {

        /*
         * By injecting the attribute DAO of this session bean in the attribute manager we are sure that the attribute manager (a
         * lightweight bean) will live within the same transaction and security context as this identity service EJB3 session bean.
         */
        LOG.debug("postConstruct");
        attributeManager = new AttributeManagerLWBean(attributeDAO, attributeTypeDAO);
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object findAttributeValue(String userId, String attributeName)
            throws PermissionDeniedException, AttributeTypeNotFoundException, AttributeUnavailableException, SubjectNotFoundException {

        AttributeTypeEntity attributeType = attributeTypeDAO.getAttributeType(attributeName);
        SubjectEntity subject = subjectService.getSubject(userId);

        return findAttributeValue(subject, attributeType);
    }

    /**
     * {@inheritDoc}
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public Object findAttributeValue(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws PermissionDeniedException, AttributeUnavailableException, SubjectNotFoundException, AttributeTypeNotFoundException {

        if (attributeTypeService.isLocal(attributeType))
            return findLocalAttribute(subject, attributeType);

        // Not local, check the attribute cache.
        Object value = findCachedAttributeValue(subject, attributeType);
        if (null != value)
            return value;

        if (attributeType.isExternal()) {
            value = findExternalAttributeValue(subject, attributeType);
            cacheAttributeValue(value, subject, attributeType);
            return value;
        }

        try {
            value = findRemoteAttribute(subject, attributeType);
            cacheAttributeValue(value, subject, attributeType);
            return value;
        } catch (NodeNotFoundException e) {
            String message = "node " + attributeType.getName() + " not found attribute " + attributeType.getName();
            securityAuditLogger.addSecurityAudit(SecurityThreatType.DECEPTION, message);
            throw new PermissionDeniedException(message);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException, PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("create attribute " + attributeType.getName() + " for " + subject.getUserId());

        if (attributeTypeService.isLocal(attributeType)) {
            createLocalAttribute(subject, attributeType, attributeValue);
        } else if (attributeType.isExternal())
            throw new PermissionDeniedException("External attribute creation not supported");
        else {
            createRemoteAttribute(subject, attributeType, attributeValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException, net.link.safeonline.authentication.exception.AttributeNotFoundException,
            PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("set attribute " + attributeType.getName() + " for " + subject.getUserId());

        if (attributeTypeService.isLocal(attributeType)) {
            setLocalAttribute(subject, attributeType, attributeValue);
        } else if (attributeType.isExternal())
            throw new PermissionDeniedException("External attribute modification not supported");
        else {
            setRemoteAttribute(subject, attributeType, attributeValue);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId,
                                     Map<String, Object> memberValues)
            throws net.link.safeonline.authentication.exception.AttributeNotFoundException, AttributeTypeNotFoundException,
            DatatypeMismatchException, PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("set compound attribute " + attributeType.getName() + " for " + subject.getUserId());

        if (attributeTypeService.isLocal(attributeType)) {
            setLocalCompoundAttribute(subject, attributeType, attributeId, memberValues);
        } else if (attributeType.isExternal())
            throw new PermissionDeniedException("External attribute modification not supported");
        else {
            setRemoteCompoundAttribute(subject, attributeType, attributeId, memberValues);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeAttribute(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws net.link.safeonline.authentication.exception.AttributeNotFoundException, PermissionDeniedException,
            SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("remove attribute " + attributeType.getName() + " for " + subject.getUserId());

        if (attributeTypeService.isLocal(attributeType)) {
            attributeManager.removeAttribute(attributeType, subject);
        } else if (attributeType.isExternal())
            throw new PermissionDeniedException("External attribute removal not supported");
        else {
            removeRemoteAttribute(subject, attributeType);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void removeCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId)
            throws PermissionDeniedException, net.link.safeonline.authentication.exception.AttributeNotFoundException,
            SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("remove compound attribute " + attributeType.getName() + " for " + subject.getUserId());

        if (attributeTypeService.isLocal(attributeType)) {
            attributeManager.removeCompoundAttribute(attributeType, subject, attributeId);
        } else if (attributeType.isExternal())
            throw new PermissionDeniedException("External compound attribute removal not supported");
        else {
            removeRemoteCompoundAttribute(subject, attributeType, attributeId);
        }
    }

    /**
     * Removes a remote compound attribute for the specified subject.
     */
    private void removeRemoteCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId)
            throws SubjectNotFoundException, NodeNotFoundException, PermissionDeniedException {

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        PrivateKeyEntry nodeKeyEntry = SafeOnlineNodeKeyStore.getPrivateKeyEntry();
        DataClient dataClient = new DataClientImpl(attributeType.getLocation().getLocation(),
                (X509Certificate) nodeKeyEntry.getCertificate(), nodeKeyEntry.getPrivateKey());
        try {
            dataClient.removeAttribute(nodeMapping.getId(), attributeType.getName(), attributeId);
        } catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to modify attribute of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    /**
     * Removes a remote attribute for the specified subject.
     */
    private void removeRemoteAttribute(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws PermissionDeniedException, SubjectNotFoundException, NodeNotFoundException {

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        PrivateKeyEntry nodeKeyEntry = SafeOnlineNodeKeyStore.getPrivateKeyEntry();
        DataClient dataClient = new DataClientImpl(attributeType.getLocation().getLocation(),
                (X509Certificate) nodeKeyEntry.getCertificate(), nodeKeyEntry.getPrivateKey());
        try {
            dataClient.removeAttribute(nodeMapping.getId(), attributeType.getName(), null);
        } catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to modify attribute of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        }
    }

    /**
     * Sets a local compound attribute for the specified subject and attribute type with the specified member values.
     */
    private void setLocalCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId,
                                           Map<String, Object> memberValues)
            throws net.link.safeonline.authentication.exception.AttributeNotFoundException, AttributeTypeNotFoundException,
            DatatypeMismatchException {

        // attributeId is the global UUID of the record, while AttributeIndex is the local database Id of the attribute record.
        AttributeEntity compoundAttribute = getCompoundAttribute(subject, attributeType, attributeId);

        long attributeIdx = compoundAttribute.getAttributeIndex();
        LOG.debug("attribute idx: " + attributeIdx);

        for (Map.Entry<String, Object> memberValue : memberValues.entrySet()) {
            AttributeTypeEntity memberAttributeType = attributeTypeDAO.getAttributeType(memberValue.getKey());
            AttributeEntity memberAttribute = attributeDAO.getAttribute(memberAttributeType, subject, attributeIdx);
            setAttributeValue(memberAttribute, memberValue.getValue());
        }
    }

    private AttributeEntity getCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId)
            throws net.link.safeonline.authentication.exception.AttributeNotFoundException {

        return attributeManager.getCompoundWhere(subject, attributeType, null, attributeId);
    }

    private void setRemoteCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, String attributeId,
                                            Map<String, Object> memberValues)
            throws SubjectNotFoundException, NodeNotFoundException, PermissionDeniedException,
            net.link.safeonline.authentication.exception.AttributeNotFoundException {

        // create compound attribute map to pass to data-ws
        CompoundBuilder compoundBuilder = new CompoundBuilder(memberValues.getClass());
        compoundBuilder.setCompoundId(attributeId);
        for (Entry<String, Object> memberValueEntry : memberValues.entrySet()) {
            compoundBuilder.setCompoundProperty(memberValueEntry.getKey(), memberValueEntry.getValue());
        }

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        PrivateKeyEntry nodeKeyEntry = SafeOnlineNodeKeyStore.getPrivateKeyEntry();
        DataClient dataClient = new DataClientImpl(attributeType.getLocation().getLocation(),
                (X509Certificate) nodeKeyEntry.getCertificate(), nodeKeyEntry.getPrivateKey());
        try {
            dataClient.setAttributeValue(nodeMapping.getId(), attributeType.getName(), compoundBuilder.getCompound());
        } catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to modify attribute of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        } catch (net.link.safeonline.sdk.exception.AttributeNotFoundException e) {
            throw new net.link.safeonline.authentication.exception.AttributeNotFoundException();
        }

    }

    /**
     * Sets a local attribute for the specified subject and attribute type with the specified value.
     */
    private void setLocalAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException, net.link.safeonline.authentication.exception.AttributeNotFoundException {

        if (attributeType.isMultivalued()) {
            setMultivaluedAttribute(attributeValue, attributeType, subject);
        } else {
            setSinglevaluedAttribute(attributeValue, attributeType, subject);
        }
    }

    private void setSinglevaluedAttribute(Object attributeValue, AttributeTypeEntity attributeType, SubjectEntity subject)
            throws DatatypeMismatchException, net.link.safeonline.authentication.exception.AttributeNotFoundException {

        AttributeEntity attribute = attributeDAO.getAttribute(attributeType, subject);

        if (null == attributeValue) {
            // In case the attribute value is null we cannot extract the reflection class type. But actually we don't care. Just clear all.
            attribute.clearValues();
            return;
        }

        setAttributeValue(attribute, attributeValue);
    }

    private void setMultivaluedAttribute(Object attributeValue, AttributeTypeEntity attributeType, SubjectEntity subject)
            throws DatatypeMismatchException, net.link.safeonline.authentication.exception.AttributeNotFoundException {

        List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, attributeType);
        if (attributes.isEmpty())
            // can only update existing multivalued attributes, not create them.
            throw new net.link.safeonline.authentication.exception.AttributeNotFoundException();

        if (null == attributeValue) {
            /*
             * In this case we remove all but one, which we set with a null value.
             */
            Iterator<AttributeEntity> iterator = attributes.iterator();
            AttributeEntity attribute = iterator.next();
            attribute.clearValues();
            while (iterator.hasNext()) {
                attribute = iterator.next();
                attributeDAO.removeAttribute(attribute);
            }
        } else {
            if (false == attributeValue.getClass().isArray())
                throw new DatatypeMismatchException();

            int newSize = Array.getLength(attributeValue);
            Iterator<AttributeEntity> iterator = attributes.iterator();
            for (int idx = 0; idx < newSize; idx++) {
                Object value = Array.get(attributeValue, idx);
                AttributeEntity attribute;
                if (iterator.hasNext()) {
                    attribute = iterator.next();
                } else {
                    attribute = attributeDAO.addAttribute(attributeType, subject);
                }
                setAttributeValue(attribute, value);
            }
            while (iterator.hasNext()) {
                AttributeEntity attribute = iterator.next();
                attributeDAO.removeAttribute(attribute);
            }
        }
    }

    /**
     * Sets a remote attribute, using the data-ws.
     */
    private void setRemoteAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws SubjectNotFoundException, NodeNotFoundException, PermissionDeniedException,
            net.link.safeonline.authentication.exception.AttributeNotFoundException {

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        PrivateKeyEntry nodeKeyEntry = SafeOnlineNodeKeyStore.getPrivateKeyEntry();
        DataClient dataClient = new DataClientImpl(attributeType.getLocation().getLocation(),
                (X509Certificate) nodeKeyEntry.getCertificate(), nodeKeyEntry.getPrivateKey());
        try {
            dataClient.setAttributeValue(nodeMapping.getId(), attributeType.getName(), attributeValue);
        } catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to create attribute of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        } catch (net.link.safeonline.sdk.exception.AttributeNotFoundException e) {
            throw new net.link.safeonline.authentication.exception.AttributeNotFoundException();
        }

    }

    /**
     * Creates a local attribute for the specified subject and attribute type with the specified value.
     */
    @SuppressWarnings("unchecked")
    private void createLocalAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws DatatypeMismatchException {

        if (null == attributeValue) {
            attributeDAO.addAttribute(attributeType, subject);
            return;
        }

        if (attributeValue instanceof Map) {
            Map<String, Object> memberValues = (Map<String, Object>) attributeValue;
            createCompoundAttribute(subject, attributeType, memberValues);
            return;
        }

        Class attributeValueClass = attributeValue.getClass();
        if (attributeType.isMultivalued()) {
            if (false == attributeValueClass.isArray())
                throw new DatatypeMismatchException();

            int size = Array.getLength(attributeValue);
            for (int idx = 0; idx < size; idx++) {
                Object value = Array.get(attributeValue, idx);
                AttributeEntity attribute = attributeDAO.addAttribute(attributeType, subject);
                setAttributeValue(attribute, value);
            }
        } else {
            /*
             * Single-valued attribute.
             */
            AttributeEntity attribute = attributeDAO.addAttribute(attributeType, subject);
            setAttributeValue(attribute, attributeValue);
        }

        return;
    }

    /**
     * Creates a remote attribute, using the data-ws
     */
    private void createRemoteAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Object attributeValue)
            throws SubjectNotFoundException, NodeNotFoundException, PermissionDeniedException {

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        PrivateKeyEntry nodeKeyEntry = SafeOnlineNodeKeyStore.getPrivateKeyEntry();
        DataClient dataClient = new DataClientImpl(attributeType.getLocation().getLocation(),
                (X509Certificate) nodeKeyEntry.getCertificate(), nodeKeyEntry.getPrivateKey());
        try {
            dataClient.createAttribute(nodeMapping.getId(), attributeType.getName(), attributeValue);
        } catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to create attribute of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        }

    }

    private void createCompoundAttribute(SubjectEntity subject, AttributeTypeEntity attributeType, Map<String, Object> memberValues) {

        CompoundAttributeDO compoundAttribute = attributeManager.newCompound(attributeType, subject);
        List<CompoundedAttributeTypeMemberEntity> memberTypes = attributeType.getMembers();
        for (CompoundedAttributeTypeMemberEntity memberType : memberTypes) {
            AttributeTypeEntity memberAttributeType = memberType.getMember();
            Object attributeValue = memberValues.get(memberAttributeType.getName());

            compoundAttribute.addAttribute(memberAttributeType, attributeValue);
        }
    }

    private void setAttributeValue(AttributeEntity attribute, Object value)
            throws DatatypeMismatchException {

        try {
            attribute.setValue(value);
        } catch (ClassCastException e) {
            throw new DatatypeMismatchException();
        }
    }

    /**
     * Caches the attribute if a positive caching timeout is specified.
     */
    @SuppressWarnings("unchecked")
    private void cacheAttributeValue(Object value, SubjectEntity subject, AttributeTypeEntity attributeType) {

        if (attributeType.getAttributeCacheTimeoutMillis() <= 0)
            return;

        if (null == value)
            return;

        DatatypeType datatype = attributeType.getType();
        if (attributeType.isMultivalued()) {
            switch (datatype) {
                case STRING: {
                    String[] values = (String[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        attribute.setStringValue(values[idx]);
                    }
                    return;
                }
                case BOOLEAN: {
                    Boolean[] values = (Boolean[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        attribute.setBooleanValue(values[idx]);
                    }
                    return;
                }
                case INTEGER: {
                    Integer[] values = (Integer[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        attribute.setIntegerValue(values[idx]);
                    }
                    return;
                }
                case DOUBLE: {
                    Double[] values = (Double[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        attribute.setDoubleValue(values[idx]);
                    }
                    return;
                }
                case DATE: {
                    Date[] values = (Date[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        attribute.setDateValue(values[idx]);
                    }
                    return;
                }
                case COMPOUNDED: {
                    Map[] values = (Map[]) value;
                    for (int idx = 0; idx < values.length; idx++) {
                        AttributeCacheEntity compoundAttribute = attributeCacheDAO.addAttribute(attributeType, subject, idx);
                        List<AttributeCacheEntity> memberAttributes = new LinkedList<AttributeCacheEntity>();
                        for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
                            AttributeTypeEntity memberAttributeType = member.getMember();
                            Object memberValue = values[idx].get(memberAttributeType.getName());
                            // check member attribute cache entry present, if so update entry date
                            AttributeCacheEntity memberAttribute = attributeCacheDAO.findAttribute(subject, memberAttributeType, idx);
                            if (null != memberAttribute) {
                                memberAttribute.setEntryDate(new Date(System.currentTimeMillis()));
                            } else {
                                memberAttribute = attributeCacheDAO.addAttribute(memberAttributeType, subject, idx);
                            }
                            if (null != memberValue) {
                                memberAttribute.setValue(memberValue);
                            }
                            memberAttributes.add(memberAttribute);
                        }
                        compoundAttribute.setMembers(memberAttributes);
                    }
                    return;
                }
                default:
                    throw new EJBException("datatype not supported: " + datatype);
            }
        }

        /*
         * Single-valued attribute.
         */
        AttributeCacheEntity attribute = attributeCacheDAO.addAttribute(attributeType, subject, 0);
        attribute.setValue(value);
    }

    /**
     * Find an external attribute value using the OSGi plugin specified in the attribute type.
     * 
     * @throws AttributeUnavailableException
     * @throws AttributeTypeNotFoundException
     * @throws PermissionDeniedException
     * @throws SubjectNotFoundException
     */
    @SuppressWarnings("unchecked")
    private Object findExternalAttributeValue(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws AttributeUnavailableException, AttributeTypeNotFoundException, SubjectNotFoundException {

        LOG.debug("find external attribute " + attributeType.getName() + " for " + subject.getUserId());
        try {
            OSGIService osgiService = osgiStartable.getService(attributeType.getPluginName(), OSGIServiceType.PLUGIN_SERVICE);
            PluginAttributeService pluginAttributeService = (PluginAttributeService) osgiService.getService();
            Object value = pluginAttributeService.getAttribute(subject.getUserId(), attributeType.getName(),
                    attributeType.getPluginConfiguration());
            osgiService.ungetService();

            DatatypeType datatype = attributeType.getType();
            if (attributeType.isMultivalued()) {

                switch (datatype) {
                    case STRING:
                        return ((List<String>) value).toArray(new String[] {});
                    case BOOLEAN:
                        return ((List<Boolean>) value).toArray(new Boolean[] {});
                    case COMPOUNDED:
                        return ((List<Map>) value).toArray(new Map[] {});
                    case DATE:
                        return ((List<Date>) value).toArray(new Date[] {});
                    case DOUBLE:
                        return ((List<Double>) value).toArray(new Double[] {});
                    case INTEGER:
                        return ((List<Integer>) value).toArray(new Integer[] {});
                }
            }

            if (value instanceof List) {
                List<?> values = (List<?>) value;
                return values.toArray();
            }
            return value;
        } catch (AttributeNotFoundException e) {
            LOG.debug("external attribute " + attributeType.getName() + " not found for " + subject.getUserId() + " ( plugin="
                    + attributeType.getPluginName() + " )");
            return null;
        } catch (net.link.safeonline.osgi.exception.AttributeTypeNotFoundException e) {
            throw new AttributeTypeNotFoundException();
        } catch (SafeOnlineResourceException e) {
            throw new AttributeUnavailableException();
        } catch (net.link.safeonline.osgi.exception.AttributeUnavailableException e) {
            throw new AttributeUnavailableException();
        } catch (net.link.safeonline.osgi.exception.SubjectNotFoundException e) {
            throw new SubjectNotFoundException();
        } catch (Exception e) {
            LOG.debug("unexpected exception: " + e.getClass().getName());
            throw new AttributeUnavailableException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(final List<?> c) {

        return c.toArray((T[]) Array.newInstance(c.iterator().next().getClass(), 0));
    }

    /**
     * Find local attribute.
     * 
     * @param subject
     * @param attributeType
     * @return attribute value
     */
    private Object findLocalAttribute(SubjectEntity subject, AttributeTypeEntity attributeType) {

        LOG.debug("find local attribute " + attributeType.getName() + " for " + subject.getUserId());

        // filter out the empty attributes
        List<AttributeEntity> attributes = attributeDAO.listAttributes(subject, attributeType);
        List<AttributeEntity> nonEmptyAttributes = new LinkedList<AttributeEntity>();
        for (AttributeEntity attribute : attributes)
            if (attribute.getAttributeType().isCompounded()) {
                nonEmptyAttributes.add(attribute);
            } else if (!attribute.isEmpty()) {
                nonEmptyAttributes.add(attribute);
            }

        LOG.debug("found " + nonEmptyAttributes.size());

        if (nonEmptyAttributes.isEmpty())
            return null;

        return getValueFromLocal(nonEmptyAttributes, attributeType, subject);
    }

    /**
     * Find possible valid cached attribute, if not found or not valid anymore, returns null.
     * 
     * @param subject
     * @param attributeType
     * @return cached attribute value or null if not found or invalid.
     */
    private Object findCachedAttributeValue(SubjectEntity subject, AttributeTypeEntity attributeType) {

        LOG.debug("find cached attribute " + attributeType.getName() + " for " + subject.getUserId());
        long currentTime = System.currentTimeMillis();

        List<AttributeCacheEntity> attributes = attributeCacheDAO.listAttributes(subject, attributeType);
        if (null == attributes || attributes.isEmpty())
            return null;

        // check expiration date
        for (int idx = 0; idx < attributes.size(); idx++) {
            if (currentTime - attributes.get(idx).getEntryDate().getTime() > attributeType.getAttributeCacheTimeoutMillis()) {
                // expired
                attributeCacheDAO.removeAttributes(subject, attributeType);
                return null;
            }
            if (attributeType.isCompounded()) {
                for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
                    AttributeTypeEntity memberAttributeType = member.getMember();
                    AttributeCacheEntity memberAttribute = attributeCacheDAO.findAttribute(subject, memberAttributeType, idx);
                    if (null == memberAttribute) {
                        attributeCacheDAO.removeAttributes(subject, attributeType);
                        return null;
                    }
                    if (currentTime - memberAttribute.getEntryDate().getTime() > memberAttributeType.getAttributeCacheTimeoutMillis()) {
                        // expired
                        attributeCacheDAO.removeAttributes(subject, attributeType);
                        return null;
                    }

                }
            }
        }

        List<AttributeCacheEntity> nonEmptyAttributes = new LinkedList<AttributeCacheEntity>();
        for (AttributeCacheEntity attribute : attributes)
            if (attribute.getAttributeType().isCompounded()) {
                nonEmptyAttributes.add(attribute);
            } else if (!attribute.isEmpty()) {
                nonEmptyAttributes.add(attribute);
            }

        LOG.debug("found " + nonEmptyAttributes.size() + " cached attributes of type " + attributeType.getName());

        if (nonEmptyAttributes.isEmpty())
            return null;

        return getValueFromCache(nonEmptyAttributes, attributeType, subject);
    }

    /**
     * Find remote attribute.
     * 
     * @param subjectId
     * @param attributeType
     * @return attribute value
     * @throws NodeNotFoundException
     * @throws SubjectNotFoundException
     * @throws SafeOnlineResourceException
     */
    private Object findRemoteAttribute(SubjectEntity subject, AttributeTypeEntity attributeType)
            throws PermissionDeniedException, AttributeUnavailableException, SubjectNotFoundException, NodeNotFoundException {

        LOG.debug("find remote attribute " + attributeType.getName() + " for " + subject.getUserId());

        NodeMappingEntity nodeMapping = nodeMappingService.getNodeMapping(subject.getUserId(), attributeType.getLocation().getName());

        SafeOnlineNodeKeyStore nodeKeyStore = new SafeOnlineNodeKeyStore();
        AttributeClient attributeClient = new AttributeClientImpl(attributeType.getLocation().getLocation(), nodeKeyStore.getCertificate(),
                nodeKeyStore.getPrivateKey());

        DatatypeType datatype = attributeType.getType();
        Class<?> attributeClass;

        if (attributeType.isMultivalued()) {
            switch (datatype) {
                case STRING:
                    attributeClass = String[].class;
                break;
                case BOOLEAN:
                    attributeClass = Boolean[].class;
                break;
                case INTEGER:
                    attributeClass = Integer[].class;
                break;
                case DOUBLE:
                    attributeClass = Double[].class;
                break;
                case DATE:
                    attributeClass = Date[].class;
                break;
                case COMPOUNDED:
                    attributeClass = Map[].class;
                break;
                default:
                    throw new EJBException("datatype not supported: " + datatype);
            }
        } else {
            switch (datatype) {
                case STRING:
                    attributeClass = String.class;
                break;
                case BOOLEAN:
                    attributeClass = Boolean.class;
                break;
                case INTEGER:
                    attributeClass = Integer.class;
                break;
                case DOUBLE:
                    attributeClass = Double.class;
                break;
                case DATE:
                    attributeClass = Date.class;
                break;
                case COMPOUNDED:
                    attributeClass = Map.class;
                break;
                default:
                    throw new EJBException("datatype not supported: " + datatype);
            }
        }

        try {
            return attributeClient.getAttributeValue(nodeMapping.getId(), attributeType.getName(), attributeClass);
        }

        catch (WSClientTransportException e) {
            resourceAuditLogger.addResourceAudit(ResourceNameType.WS, ResourceLevelType.RESOURCE_UNAVAILABLE, e.getLocation(),
                    "Failed to get attribute value of type " + attributeType.getName() + " for subject " + subject.getUserId());
            throw new PermissionDeniedException(e.getMessage());
        } catch (RequestDeniedException e) {
            throw new PermissionDeniedException(e.getMessage());
        } catch (net.link.safeonline.sdk.exception.AttributeNotFoundException e) {
            return null;
        } catch (net.link.safeonline.sdk.exception.AttributeUnavailableException e) {
            throw new AttributeUnavailableException();
        }
    }

    /**
     * Returns attribute value object for local attributes.
     */
    @SuppressWarnings("unchecked")
    private Object getValueFromLocal(List<AttributeEntity> attributes, AttributeTypeEntity attributeType, SubjectEntity subject) {

        DatatypeType datatype = attributeType.getType();
        if (attributeType.isMultivalued()) {
            switch (datatype) {
                case STRING: {
                    String[] values = new String[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getStringValue();
                    }
                    return values;
                }
                case BOOLEAN: {
                    Boolean[] values = new Boolean[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getBooleanValue();
                    }
                    return values;
                }
                case INTEGER: {
                    Integer[] values = new Integer[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getIntegerValue();
                    }
                    return values;
                }
                case DOUBLE: {
                    Double[] values = new Double[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getDoubleValue();
                    }
                    return values;
                }
                case DATE: {
                    Date[] values = new Date[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getDateValue();
                    }
                    return values;
                }
                case COMPOUNDED: {
                    Map[] values = new Map[attributes.size()];
                    // add parent entry in maps
                    for (int idx = 0; idx < attributes.size(); idx++) {
                        AttributeEntity parentAttribute = attributeDAO.findAttribute(subject, attributeType, idx);
                        Map<String, Object> memberMap = values[idx];
                        if (null == memberMap) {
                            memberMap = new HashMap<String, Object>();
                            values[idx] = memberMap;
                        }
                        memberMap.put(attributeType.getName(), parentAttribute.getStringValue());
                    }
                    // now add member entries in maps
                    for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
                        AttributeTypeEntity memberAttributeType = member.getMember();
                        for (int idx = 0; idx < attributes.size(); idx++) {
                            AttributeEntity attribute = attributeDAO.findAttribute(subject, memberAttributeType, idx);
                            Map<String, Object> memberMap = values[idx];
                            Object memberValue;
                            if (null != attribute) {
                                memberValue = attribute.getValue();
                            } else {
                                memberValue = null;
                            }
                            memberMap.put(memberAttributeType.getName(), memberValue);
                        }
                    }
                    return values;
                }
                default:
                    throw new EJBException("datatype not supported: " + datatype);
            }
        }

        /*
         * Single-valued attribute.
         */
        if (attributes.isEmpty())
            return null;

        AttributeEntity attribute = attributes.get(0);
        return attribute.getValue();
    }

    /**
     * Returns attribute value object for local cached attributes
     */
    @SuppressWarnings("unchecked")
    private Object getValueFromCache(List<AttributeCacheEntity> attributes, AttributeTypeEntity attributeType, SubjectEntity subject) {

        DatatypeType datatype = attributeType.getType();
        if (attributeType.isMultivalued()) {
            switch (datatype) {
                case STRING: {
                    String[] values = new String[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getStringValue();
                    }
                    return values;
                }
                case BOOLEAN: {
                    Boolean[] values = new Boolean[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getBooleanValue();
                    }
                    return values;
                }
                case INTEGER: {
                    Integer[] values = new Integer[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getIntegerValue();
                    }
                    return values;
                }
                case DOUBLE: {
                    Double[] values = new Double[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getDoubleValue();
                    }
                    return values;
                }
                case DATE: {
                    Date[] values = new Date[attributes.size()];
                    for (int idx = 0; idx < values.length; idx++) {
                        values[idx] = attributes.get(idx).getDateValue();
                    }
                    return values;
                }
                case COMPOUNDED: {
                    Map[] values = new Map[attributes.size()];
                    for (CompoundedAttributeTypeMemberEntity member : attributeType.getMembers()) {
                        AttributeTypeEntity memberAttributeType = member.getMember();
                        for (int idx = 0; idx < attributes.size(); idx++) {
                            AttributeCacheEntity attribute = attributeCacheDAO.findAttribute(subject, memberAttributeType, idx);
                            Map<String, Object> memberMap = values[idx];
                            if (null == memberMap) {
                                memberMap = new HashMap<String, Object>();
                                values[idx] = memberMap;
                            }
                            Object memberValue;
                            if (null != attribute) {
                                memberValue = attribute.getValue();
                            } else {
                                memberValue = null;
                            }
                            memberMap.put(memberAttributeType.getName(), memberValue);
                        }
                    }
                    return values;
                }
                default:
                    throw new EJBException("datatype not supported: " + datatype);
            }
        }

        /*
         * Single-valued attribute.
         */
        if (attributes.isEmpty())
            return null;

        AttributeCacheEntity attribute = attributes.get(0);
        return attribute.getValue();

    }
}
