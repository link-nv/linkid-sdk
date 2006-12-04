package net.link.safeonline.util.ee;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utils to ease the working with EJBs.
 * 
 * @author fcorneli
 * 
 */
public class EjbUtils {

	private static final Log LOG = LogFactory.getLog(EjbUtils.class);

	private EjbUtils() {
		// empty
	}

	/**
	 * Lookup an EJB within JNDI.
	 * 
	 * @param <Type>
	 * @param jndiName
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <Type> Type getEJB(String jndiName, Class<Type> type) {
		try {
			LOG.debug("ejb jndi lookup: " + jndiName);
			InitialContext initialContext = new InitialContext();
			Object obj = initialContext.lookup(jndiName);
			if (!type.isInstance(obj)) {
				throw new RuntimeException(jndiName + " is not a "
						+ type.getName() + " but a " + obj.getClass().getName());
			}
			Type instance = (Type) obj;
			return instance;
		} catch (NamingException e) {
			throw new RuntimeException("naming error: " + e.getMessage(), e);
		}
	}
}
