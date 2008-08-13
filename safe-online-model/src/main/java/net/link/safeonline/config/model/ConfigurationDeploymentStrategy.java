package net.link.safeonline.config.model;

import java.util.Set;

import org.jboss.seam.deployment.DeploymentStrategy;


public class ConfigurationDeploymentStrategy extends DeploymentStrategy {

    public static final String             HANDLERS_KEY   = "net.link.safeonline.config.model.deploymentHandlers";

    /**
     * The files used to search for configuration classes
     */
    public static final String[]           RESOURCE_NAMES = { "config.properties" };

    private ClassLoader                    classLoader;

    private ConfigurationDeploymentHandler configurationDeploymentHandler;


    public ConfigurationDeploymentStrategy() {

        this.classLoader = Thread.currentThread().getContextClassLoader();
        this.configurationDeploymentHandler = new ConfigurationDeploymentHandler();
        getDeploymentHandlers().put(this.configurationDeploymentHandler.getName(), this.configurationDeploymentHandler);
    }

    @Override
    public ClassLoader getClassLoader() {

        return this.classLoader;
    }

    @Override
    protected String getDeploymentHandlersKey() {

        return HANDLERS_KEY;
    }

    @Override
    public void scan() {

        getScanner().scanResources(RESOURCE_NAMES);
    }

    public Set<Class<Object>> getScannedConfigurationClasses() {

        return this.configurationDeploymentHandler.getClasses();
    }

}
