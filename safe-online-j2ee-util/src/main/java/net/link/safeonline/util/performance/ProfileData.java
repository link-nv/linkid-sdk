/*
 * SafeOnline project.
 *
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.performance;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
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

	/**
	 * The measurement string for the time the request took. This is the
	 * difference of time between the start and end of the request in
	 * milliseconds.
	 */
	public static final String REQUEST_DELTA_TIME = "RequestTime";

	/**
	 * The measurement string for the amount of free memory at the end of the
	 * request in bytes.
	 */
	public static final String REQUEST_START_FREE = "StartFreeMemory";

	/**
	 * The time at which the request was made in seconds since the UNIX Epoch.
	 */
	public static final String REQUEST_START_TIME = "StartTime";

	/**
	 * The measurement string for the memory the request used. This is the
	 * difference of memory between the start and end of the request in bytes.
	 */
	public static final String REQUEST_END_FREE = "EndFreeMemory";

	/**
	 * The header used to communicate the duration of the call
	 */
	private static final String DURATION_HEADER = "X-Profiled-Duration-";
	private static final Log LOG = LogFactory.getLog(ProfileData.class);

	/**
	 * The header used to identify the method signature for a profile entry.
	 */
	private static final String METHODSIG_HEADER = "X-Profiled-Method-";

	/**
	 * A list of measurement keys that have a special meaning in the request.
	 * (Meaning, they are not names of method signatures.)
	 */
	private static List<String> requestKeys = new ArrayList<String>();

	private static final long serialVersionUID = 1L;
	static {
		requestKeys.add(REQUEST_START_TIME);
		requestKeys.add(REQUEST_DELTA_TIME);
		requestKeys.add(REQUEST_START_FREE);
		requestKeys.add(REQUEST_END_FREE);
	}

	/**
	 * Compress the generic form of the method's signature. Trim off throws
	 * declarations.<br />
	 * java.lang.method -> j~l~method
	 */
	public static String compressSignature(String signature) {

		String compressed = signature.replaceAll("(\\w)\\w{2,}\\.", "$1~");
		return compressed.replaceFirst(" throws [^\\(\\)]*", "");
	}

	public static boolean isRequestKey(String key) {

		return requestKeys.contains(key);
	}

	private boolean locked = false;

	private Map<String, Long> measurements;

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

	public void addMeasurement(Method method, Long value)
			throws ProfileDataLockedException {

		this.addMeasurement(compressSignature(method.toGenericString()), value);
	}

	public synchronized void addMeasurement(String method, Long value)
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

		return Collections.unmodifiableMap(headers);
	}

	public Map<String, Long> getMeasurements() {

		return Collections.unmodifiableMap(this.measurements);
	}

	public long getMeasurement(String key) {

		Long measurement = this.measurements.get(key);
		return measurement == null ? 0 : measurement;
	}

	public boolean isLocked() {

		return this.locked;
	}

	public void lock() throws ProfileDataLockedException {

		if (this.locked)
			throw new ProfileDataLockedException();
		this.locked = true;
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

	public void unlock() {

		this.locked = false;
	}
}
