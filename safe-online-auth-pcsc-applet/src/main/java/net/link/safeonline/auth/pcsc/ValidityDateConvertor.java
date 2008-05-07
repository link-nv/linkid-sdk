/*
 * SafeOnline project.
 * 
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;

public class ValidityDateConvertor implements Convertor<Date> {

    public Date convert(byte[] value) throws ConvertorException {
		int day = Integer.parseInt(new String(Arrays.copyOfRange(value, 0, 2)));
		int month = Integer
				.parseInt(new String(Arrays.copyOfRange(value, 3, 5)));
		int year = Integer
				.parseInt(new String(Arrays.copyOfRange(value, 6, 10)));
		GregorianCalendar calendar = new GregorianCalendar(year, month, day);
		Date date = calendar.getTime();
		return date;
	}
}