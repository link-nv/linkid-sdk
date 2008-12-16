/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.wicket.web;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.WebPage;


/**
 * <h2>{@link WicketPage}<br>
 * <sub>[in short] (TODO).</sub></h2>
 * 
 * <p>
 * [description / usage].
 * </p>
 * 
 * <p>
 * <i>Dec 15, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public class WicketPage extends WebPage {

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final String formatSpecifier = "%(\\d+\\$)?([-#+ 0,(\\<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])";
    private static Pattern      fsPattern       = Pattern.compile(formatSpecifier);


    /**
     * Note: You can use this method with a single argument, too. This will cause the first argument (format) to be evaluated as a
     * localization key.
     * 
     * @param format
     *            The format specification for the arguments. See {@link String#format(java.util.Locale, String, Object...)}. To that list,
     *            add the 'l' conversion parameter. This parameter first looks the arg data up as a localization key, then processes the
     *            result as though it was given with the 's' conversion parameter.
     * @param args
     *            The arguments that contain the data to fill into the format specifications.
     */
    public String localize(String format, Object... args) {

        if (args.length == 0)
            // Single argument invocation: format is localization key.
            return getLocalizer().getString(format, this);

        List<Object> localizationData = new ArrayList<Object>(args.length);
        StringBuffer newFormat = new StringBuffer(format);
        Matcher specifiers = fsPattern.matcher(format);

        int pos = 0, num = 0;
        while (specifiers.find(pos)) {
            if ("l".equalsIgnoreCase(specifiers.group(6))) {
                if ("L".equals(specifiers.group(6))) {
                    newFormat.setCharAt(specifiers.end(6) - 1, 'S');
                } else {
                    newFormat.setCharAt(specifiers.end(6) - 1, 's');
                }

                if (args[num] == null)
                    throw new NullPointerException(String.format("Key for localization must be String, got %s (arg: %d)", "null", num));
                if (!(args[num] instanceof String))
                    throw new IllegalArgumentException(String.format("Key for localization must be String, got %s (arg: %d)",
                            args[num].getClass(), num));

                localizationData.add(getLocalizer().getString((String) args[num], this));
            } else {
                localizationData.add(args[num]);
            }

            ++num;
            pos = specifiers.end();
        }

        return String.format(getLocale(), newFormat.toString(), localizationData.toArray());
    }
}
