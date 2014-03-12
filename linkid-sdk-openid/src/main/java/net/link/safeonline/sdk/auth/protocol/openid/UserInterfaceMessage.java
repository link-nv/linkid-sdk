/*
 * linkID project.
 *
 * Copyright 2006-2014 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.sdk.auth.protocol.openid;

import com.google.inject.internal.AbstractIterator;
import org.openid4java.message.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * OpenID User Interface Extension v1.0
 * <p/>
 *
 * @author Wim Vandenhaute
 * @see <a
 *      href="http://svn.openid.net/repos/specifications/user_interface/1.0/trunk/openid-user-interface-extension-1_0.html">
 *      OpenID User Interface Extension v1.0</a>
 */
public class UserInterfaceMessage implements MessageExtension,
		MessageExtensionFactory, Iterable<String> {

	public static final String OPENID_NS_UI = "http://specs.openid.net/extensions/ui/1.0";

	public static final String LANGUAGE_PREFIX = "lang";
    public static final String MODE_PREFIX = "mode";
    public static final String ICON_PREFIX = "icon";


    public enum UiMode {
        POPUP( "popup" ), FRAMED("x-framed");

        private String mode;

        private UiMode(String mode) {

            this.mode = mode;
        }

        public String getMode() {

            return mode;
        }
    }

	private ParameterList parameters;

	public UserInterfaceMessage() {

		parameters = new ParameterList();
	}

	public UserInterfaceMessage(ParameterList parameterList) {

		parameters = parameterList;
	}

	/**
	 * Set the comma seperated list of preferred languages
	 *
	 * @param languageString
	 *            Comma seperated list of preferred languages
	 */
	public void setLanguages(String languageString) {

		parameters.set(new Parameter(LANGUAGE_PREFIX, languageString));
	}

	/**
	 * Set the list of preferred languages
	 *
	 * @param languages
	 *            list of preferred languages
	 */
	public void setLanguages(List<String> languages) {

		if (null == languages) {
			return;
		}

		String languageString = "";
		for (String language : languages) {
			languageString += language + ",";
		}
		if (languages.size() > 1) {
			// strip last ','
			languageString = languageString.substring(languageString
					.lastIndexOf(','));
		}

		setLanguages(languageString);
	}

	/**
	 * @return list of preferred languages. Empty list returned if none.
	 */
	public List<String> getLanguages() {

		String languageString = this.parameters
				.getParameterValue(UserInterfaceMessage.LANGUAGE_PREFIX);

		if (null == languageString) {
			return new LinkedList<String>();
		}

		String[] languages = languageString.split(",");
		return Arrays.asList(languages);
	}

    public void setUiMode(UiMode mode){
        parameters.set(new Parameter(MODE_PREFIX, mode.getMode()));
    }

    public UiMode getUiMode(){
        String mode = parameters.getParameterValue( MODE_PREFIX );
        for (UiMode value : UiMode.values()){
            if (value.getMode().equals( mode ))
                return value;
        }
        return null;
    }

    public void setIcon(boolean icon){
        parameters.set(new Parameter(ICON_PREFIX, Boolean.toString( icon )));
    }

    public boolean getIcon(){
        String value = parameters.getParameterValue( ICON_PREFIX );
        if (value == null || value.trim().equals( "" ))
            return false;
        return Boolean.getBoolean( value );
    }

	/**
	 * {@inheritDoc}
	 */
	public String getTypeUri() {

		return OPENID_NS_UI;
	}

	/**
	 * {@inheritDoc}
	 */
	public ParameterList getParameters() {

		return parameters;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setParameters(ParameterList params) {

		parameters = params;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean providesIdentifier() {

		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean signRequired() {

		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public MessageExtension getExtension(ParameterList parameterList,
			boolean isRequest) throws MessageException {

		return new UserInterfaceMessage(parameterList);
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<String> iterator() {

		return new AbstractIterator<String>() {

			@SuppressWarnings({ "unchecked" })
			private Iterator<Parameter> source = parameters.getParameters()
					.iterator();

			@Override
			protected String computeNext() {

				while (source.hasNext()) {
					Parameter param = source.next();
					String paramName = param.getKey();
					String paramValue = param.getValue();

					if (paramName.startsWith(LANGUAGE_PREFIX)) {
						return paramValue;
					}
				}

				return endOfData();
			}
		};
	}

}
