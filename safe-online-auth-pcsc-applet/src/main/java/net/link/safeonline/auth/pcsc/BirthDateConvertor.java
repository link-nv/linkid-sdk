/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;


public class BirthDateConvertor implements Convertor<Date> {

    private static final String MONTHS[][] = { { "JAN", "FEV", "MARS", "AVR", "MAI", "JUIN", "JUIL", "AOUT", "SEPT", "OCT", "NOV", "DEC" },
            { "JAN", "FEB", "MAAR", "APR", "MEI", "JUN", "JUL", "AUG", "SEP", "OKT", "NOV", "DEC" },
            { "JAN", "FEB", "M\u00C4R", "APR", "MAI", "JUN", "JUL", "AUG", "SEP", "OKT", "NOV", "DEZ" } };


    public Date convert(byte[] value)
            throws ConvertorException {

        String strValue = new String(value, Charset.forName("UTF-8"));
        StringTokenizer stringTokenizer = new StringTokenizer(strValue, " .");
        int day = Integer.parseInt(stringTokenizer.nextToken());
        String monthStr = stringTokenizer.nextToken();
        Integer month = null;
        outer: for (String[] months : MONTHS) {
            for (int idx = 0; idx < months.length; idx++) {
                if (months[idx].equals(monthStr)) {
                    month = idx;
                    break outer;
                }
            }
        }
        if (null == month)
            throw new ConvertorException("could not parse month: " + monthStr);
        int year = Integer.parseInt(stringTokenizer.nextToken());
        GregorianCalendar calendar = new GregorianCalendar(year, month, day);
        Date birthDate = calendar.getTime();
        return birthDate;
    }
}
