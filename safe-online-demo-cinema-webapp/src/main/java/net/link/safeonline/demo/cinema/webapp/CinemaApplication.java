package net.link.safeonline.demo.cinema.webapp;

import java.lang.reflect.Field;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import org.wicketstuff.javaee.naming.IJndiNamingStrategy;
import org.wicketstuff.javaee.naming.StandardJndiNamingStrategy;

public class CinemaApplication extends WebApplication {

    static final Log LOG = LogFactory.getLog(CinemaApplication.class);


    @Override
    protected void init() {

        addComponentInstantiationListener(new JavaEEComponentInjector(this,
                new IJndiNamingStrategy() {

                    private static final long                serialVersionUID = 1L;
                    private final StandardJndiNamingStrategy defaultStrategy  = new StandardJndiNamingStrategy();


                    @SuppressWarnings("unchecked")
                    public String calculateName(String ejbName, Class ejbType) {

                        try {
                            Field bindingField = ejbType
                                    .getDeclaredField("BINDING");
                            Object binding = bindingField.get(null);

                            LOG.debug("Resolved '" + ejbName + "' type '"
                                    + ejbType.getCanonicalName() + "' to: "
                                    + binding);

                            if (binding != null)
                                return binding.toString();
                        } catch (SecurityException e) {
                            LOG.warn(
                                    "No access to fields when trying to resolve '"
                                            + ejbName + "' type '"
                                            + ejbType.getCanonicalName() + "'",
                                    e);
                        } catch (NoSuchFieldException e) {
                            LOG.warn(
                                    "No field called 'BINDING' when trying to resolve '"
                                            + ejbName + "' type '"
                                            + ejbType.getCanonicalName() + "'",
                                    e);
                        } catch (IllegalArgumentException e) {
                            LOG.warn(
                                    "No valid instance of EJB when trying to resolve '"
                                            + ejbName + "' type '"
                                            + ejbType.getCanonicalName() + "'",
                                    e);
                        } catch (IllegalAccessException e) {
                            LOG.warn(
                                    "No access to 'BINDING' when trying to resolve '"
                                            + ejbName + "' type '"
                                            + ejbType.getCanonicalName() + "'",
                                    e);
                        }

                        return this.defaultStrategy.calculateName(ejbName,
                                ejbType);
                    }
                }));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<? extends Page<?>> getHomePage() {

        return LoginPage.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Session newSession(Request request, Response response) {

        return new CinemaSession(request);
    }

}
