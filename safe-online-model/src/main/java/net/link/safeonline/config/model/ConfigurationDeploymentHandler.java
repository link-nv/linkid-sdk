package net.link.safeonline.config.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javassist.bytecode.ClassFile;
import net.link.safeonline.common.Configurable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.seam.deployment.AbstractDeploymentHandler;


public class ConfigurationDeploymentHandler extends AbstractDeploymentHandler {

    private static final Log    LOG  = LogFactory.getLog(ConfigurationDeploymentHandler.class);

    private static final String NAME = "net.link.safeonline.config.model.ConfigurationDeploymentHandler";

    private Set<Class<Object>>  classes;


    public ConfigurationDeploymentHandler() {

        super();
        this.classes = new HashSet<Class<Object>>();
    }

    public Set<Class<Object>> getClasses() {

        return Collections.unmodifiableSet(this.classes);
    }

    @SuppressWarnings("unchecked")
    public void handle(String name, ClassLoader classLoader) {

        if (name.endsWith(".class")) {
            String className = filenameToClassname(name);
            try {
                ClassFile classFile = getClassFile(name, classLoader);
                if (hasAnnotation(classFile, Configurable.class)) {
                    LOG.debug("handle configuration class: " + className);
                    this.classes.add((Class<Object>) classLoader.loadClass(className));
                }
            } catch (Exception e) {
                LOG.warn("error: " + e.getMessage());
            }
        }
    }

    public String getName() {

        return NAME;
    }

}
