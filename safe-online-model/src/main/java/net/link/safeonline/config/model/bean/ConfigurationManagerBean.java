/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.config.model.bean;

import static net.link.safeonline.common.Configurable.defaultGroup;

import java.lang.reflect.Field;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import net.link.safeonline.common.Configurable;
import net.link.safeonline.config.dao.ConfigGroupDAO;
import net.link.safeonline.config.dao.ConfigItemDAO;
import net.link.safeonline.config.dao.ConfigItemValueDAO;
import net.link.safeonline.config.model.ConfigurationManager;
import net.link.safeonline.entity.config.ConfigGroupEntity;
import net.link.safeonline.entity.config.ConfigItemEntity;
import net.link.safeonline.entity.config.ConfigItemValueEntity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.LocalBinding;


@Stateless
@TransactionManagement(TransactionManagementType.BEAN)
@LocalBinding(jndiBinding = ConfigurationManager.JNDI_BINDING)
public class ConfigurationManagerBean implements ConfigurationManager {

    private static final Log   LOG = LogFactory.getLog(ConfigurationManagerBean.class);

    @Resource
    private UserTransaction    ut;

    @EJB(mappedName = ConfigGroupDAO.JNDI_BINDING)
    private ConfigGroupDAO     configGroupDAO;

    @EJB(mappedName = ConfigItemDAO.JNDI_BINDING)
    private ConfigItemDAO      configItemDAO;

    @EJB(mappedName = ConfigItemValueDAO.JNDI_BINDING)
    private ConfigItemValueDAO configItemValueDAO;


    public void addConfigurationValue(String group, String name, boolean multipleChoice, Object value) {

        String valueType = value.getClass().getName();

        ConfigGroupEntity configGroup = configGroupDAO.findConfigGroup(group);
        if (configGroup == null) {
            try {
                ut.begin();
            } catch (NotSupportedException e) {
                LOG.error("Already in a transaction and nested transactions not supported.", e);
            } catch (SystemException e) {
                LOG.error("Unexpected transaction creation error.", e);
            }

            LOG.debug("Adding configuration group: " + group);
            configGroup = configGroupDAO.addConfigGroup(group);

            try {
                ut.commit();
            }

            catch (RollbackException e) {
                LOG.debug("Couldn't add configuration group; retrying read in case another thread added it.");
                configGroup = configGroupDAO.findConfigGroup(group);
                if (configGroup == null)
                    throw new EJBException("Couldn't add configuration group but re-read didn't find an existing group.", e);
            }

            catch (HeuristicMixedException e) {
                LOG.error("A heuristic decision was made and some relevant updates have been committed, others rolled back.", e);
            } catch (HeuristicRollbackException e) {
                LOG.error("A heuristic decision was made and all relevant updates have been rolled back.", e);
            } catch (SecurityException e) {
                LOG.error("This thread cannot commit the active transaction.", e);
            } catch (IllegalStateException e) {
                LOG.error("Tried to commit a transaction but none was active.", e);
            } catch (SystemException e) {
                LOG.error("Unexpected transaction commit error.", e);
            }
        }

        ConfigItemEntity configItem = configItemDAO.findConfigItem(configGroup.getName(), name);
        if (configItem == null) {
            LOG.debug("Adding configuration item: " + name);
            configItem = configItemDAO.addConfigItem(name, valueType, multipleChoice, configGroup);
        }
        String stringValue = value.toString();
        LOG.debug("add item value: " + stringValue);

        List<ConfigItemValueEntity> configItemValues = configItemValueDAO.listConfigItemValues(configItem);
        if (null != configItemValues) {
            for (ConfigItemValueEntity configItemValue : configItemValues) {
                if (configItemValue.getValue().equals(value))
                    return;
            }
        }

        configItemValueDAO.addConfigItemValue(configItem, stringValue);

    }

    public Object getConfigurationValue(String group, String name) {

        ConfigGroupEntity configGroup = configGroupDAO.findConfigGroup(group);
        if (null == configGroup)
            return null;

        ConfigItemEntity configItem = configItemDAO.findConfigItem(configGroup.getName(), name);
        if (null == configItem)
            return null;

        return configItem.getValue();

    }

    public void removeConfigurationValue(String group, String name, Object value) {

        ConfigGroupEntity configGroup = configGroupDAO.findConfigGroup(group);
        if (null == configGroup)
            return;

        ConfigItemEntity configItem = configItemDAO.findConfigItem(configGroup.getName(), name);
        if (null == configItem)
            return;

        String stringValue = value.toString();
        for (ConfigItemValueEntity configItemValue : configItem.getValues()) {
            if (configItemValue.getValue().equals(stringValue)) {
                LOG.debug("remove item value: " + stringValue);
                configItemValueDAO.removeConfigItemValue(configItemValue);
            }
        }
    }

    public void configure(Object object) {

        LOG.debug("Configuring: " + object.getClass().getName());

        try {
            Configurable generalConfigurable = object.getClass().getAnnotation(Configurable.class);
            String group = generalConfigurable.group();

            Field[] fields = object.getClass().getDeclaredFields();
            for (Field field : fields) {
                Configurable configurable = field.getAnnotation(Configurable.class);
                if (null == configurable) {
                    continue;
                }

                if (!configurable.group().equals(defaultGroup)) {
                    group = configurable.group();
                }
                ConfigGroupEntity configGroup = configGroupDAO.findConfigGroup(group);
                if (configGroup == null) {
                    try {
                        ut.begin();
                    } catch (NotSupportedException e) {
                        LOG.error("Already in a transaction and nested transactions not supported.", e);
                    } catch (SystemException e) {
                        LOG.error("Unexpected transaction creation error.", e);
                    }

                    LOG.debug("Adding configuration group: " + group);
                    configGroup = configGroupDAO.addConfigGroup(group);

                    try {
                        ut.commit();
                    }

                    catch (RollbackException e) {
                        LOG.debug("Couldn't add configuration group; retrying read in case another thread added it.");
                        configGroup = configGroupDAO.findConfigGroup(group);
                        if (configGroup == null)
                            throw new EJBException("Couldn't add configuration group but re-read didn't find an existing group.", e);
                    }

                    catch (HeuristicMixedException e) {
                        LOG.error("A heuristic decision was made and some relevant updates have been committed, others rolled back.", e);
                    } catch (HeuristicRollbackException e) {
                        LOG.error("A heuristic decision was made and all relevant updates have been rolled back.", e);
                    } catch (SecurityException e) {
                        LOG.error("This thread cannot commit the active transaction.", e);
                    } catch (IllegalStateException e) {
                        LOG.error("Tried to commit a transaction but none was active.", e);
                    } catch (SystemException e) {
                        LOG.error("Unexpected transaction commit error.", e);
                    }
                }

                String name = configurable.name();
                if (name == null || name == "") {
                    name = field.getName();
                }

                boolean multipleChoice = configurable.multipleChoice();

                ConfigItemEntity configItem = configItemDAO.findConfigItem(configGroup.getName(), name);
                field.setAccessible(true);
                if (configItem == null) {
                    LOG.debug("Adding configuration item: " + name);
                    String valueType = object.getClass().getName();
                    Object value = field.get(object);
                    configItem = configItemDAO.addConfigItem(name, valueType, multipleChoice, configGroup);
                    if (null != value) {
                        String stringValue = value.toString();
                        LOG.debug("add item value: " + stringValue);
                        configItemValueDAO.addConfigItemValue(configItem, stringValue);
                    }
                } else {
                    configItem.setConfigGroup(configGroup);
                    setValue(configItem, field, object);
                }
            }
        } catch (Exception e) {
            throw new EJBException("Failed to configure bean", e);
        }
    }

    private void setValue(ConfigItemEntity configItem, Field field, Object object)
            throws IllegalArgumentException, IllegalAccessException {

        Class<?> fieldType = field.getType();
        Object value;
        if (null == configItem.getValue()) {
            LOG.debug("Failed to configure field " + field.getName() + ": configuration item value is null");
            return;
        }
        if (String.class.equals(fieldType)) {
            value = configItem.getValue();
        } else if (Integer.class.equals(fieldType)) {
            try {
                value = Integer.parseInt(configItem.getValue());
            } catch (NumberFormatException e) {
                LOG.error("invalid integer value for config item: " + configItem.getName());
                /*
                 * In case the value is not OK, we continue and let the bean use its initial value as is.
                 */
                return;
            }
        } else if (Double.class.equals(fieldType)) {
            try {
                value = Double.parseDouble(configItem.getValue());
            } catch (NumberFormatException e) {
                LOG.error("invalid double value for config item: " + configItem.getName());
                return;
            }
        } else if (Long.class.equals(fieldType)) {
            try {
                value = Long.parseLong(configItem.getValue());
            } catch (NumberFormatException e) {
                LOG.error("invalid long value for config item: " + configItem.getName());
                return;
            }
        } else if (Boolean.class.equals(fieldType)) {
            value = Boolean.parseBoolean(configItem.getValue());
        } else {
            LOG.error("unsupported field type: " + fieldType.getName());
            return;
        }
        LOG.debug("Configuring field: " + field.getName() + "; value: " + value);
        field.setAccessible(true);
        field.set(object, value);
    }

}
