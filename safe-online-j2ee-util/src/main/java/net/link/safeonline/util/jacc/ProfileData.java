/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * 
 * @author mbillemo
 */
public class ProfileData {

	private static final long serialVersionUID = 1L;

	private static final Log LOG = LogFactory.getLog(ProfileData.class);

	/**
	 * The header used to identify the method signature for a profile entry.
	 */
	private static final String METHODSIG_HEADER = "X-Profiled-Method-";

	/**
	 * The header used to communicate the duration of the call
	 */
	private static final String DURATION_HEADER = "X-Profiled-Duration-";

	private Map<String, Long> measurements;

	private boolean locked = false;

	public ProfileData() {

		this.measurements = new HashMap<String, Long>();
	}

	/**
	 * Create a new {@link ProfileData} instance by parsing the given headers
	 * for profile data.
	 */
	public ProfileData(Map<String, List<String>> headers) {

		this();

		// Prepare the patterns to match our headers against.
		Pattern methodSigRegex = Pattern.compile(METHODSIG_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);
		Pattern completedRegex = Pattern.compile(DURATION_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);

		// We'll first collect data we care about in arrays.
		String[] methods = new String[headers.size()];
		Long[] timings = new Long[headers.size()];

		// Parse every header to see if it's a profiler header and extract
		// interesting data into the arrays.
		for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
			String header = headerEntry.getKey();
			if (header == null || headerEntry.getValue().isEmpty())
				continue;

			String value = headerEntry.getValue().get(0);
			Matcher methodMatcher = methodSigRegex.matcher(header);
			Matcher completedMatcher = completedRegex.matcher(header);

			try {
				if (methodMatcher.matches()) {
					int index = Integer.parseInt(methodMatcher.group(1));
					methods[index] = value;
				} else if (completedMatcher.matches()) {
					int index = Integer.parseInt(completedMatcher.group(1));
					timings[index] = Long.parseLong(value);
				}
			}

			catch (NumberFormatException e) {
				LOG.error("Couldn't correctly parse header data for: " + header
						+ ": " + value, e);
			}
		}

		// Now fill up our instance of ProfileData with the values we collected
		// in the arrays.
		for (int i = 0; i < headers.size(); ++i)
			if (null != methods[i])
				this.measurements.put(methods[i], timings[i]);
	}

	/**
	 * Create a map that links HTTP headers to data. You can use these headers
	 * to transport profile data over an HTTP connection.
	 */
	public Map<String, String> getHeaders() {

		Map<String, String> headers = new HashMap<String, String>();
		int entry = 0;

		for (String measurement : this.measurements.keySet()) {
			headers.put(METHODSIG_HEADER + entry, measurement);
			headers.put(DURATION_HEADER + entry, this.measurements.get(
					measurement).toString());

			entry++;
		}

		return headers;
	}

	public Map<String, Long> getMeasurements() {

		return this.measurements;
	}

	public void addMeasurement(String method, Long value)
			throws ProfileDataLockedException {

		if (this.locked)
			throw new ProfileDataLockedException();
		this.measurements.put(method, value);
	}

	public void clear() throws ProfileDataLockedException {

		if (this.locked)
			throw new ProfileDataLockedException();
		this.measurements.clear();
	}

	public boolean isLocked() {
		return this.locked;
	}

	public void lock() throws ProfileDataLockedException {
		if (this.locked)
			throw new ProfileDataLockedException();
		this.locked = true;
	}

	public void unlock() {
		this.locked = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		StringBuffer result = new StringBuffer();

		result.append("Measurements:\n");
		for (Map.Entry<String, Long> measurement : this.measurements.entrySet()) {
			result.append(measurement.getKey());
			result.append(":  ");
			result.append(measurement.getValue());
			result.append('\n');
		}

		return result.toString();
	}
}