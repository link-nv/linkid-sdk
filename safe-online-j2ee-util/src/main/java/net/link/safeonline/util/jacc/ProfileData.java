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

import net.link.safeonline.util.filter.ProfileStats;

/**
 * 
 * 
 * @author lhunath
 */
public class ProfileData extends HashMap<String, Long> {

	private static ProfileData instance;

	private static final Log LOG = LogFactory.getLog(ProfileData.class);

	/**
	 * Just a string that is used to identify the profiler's data.<br>
	 * <br>
	 * It is used by the {@link ProfileInterceptor} to identify its lock onto
	 * the {@link javax.interceptor.InvocationContext} and by both the
	 * {@link ProfileInterceptor} and
	 * {@link net.link.safeonline.util.webapp.filter.ProfileFilter} to
	 * communicate {@link ProfileData} over the JACC Context.
	 */
	public static final String KEY = ProfileData.class.getName();

	/**
	 * The header used to identify the method for a profile entry.
	 */
	private static final String METHOD_HEADER = "X-Profiled-Method-";

	/**
	 * The header used to identify the time it took for a profile entry's method
	 * to execute.
	 */
	private static final String TIMING_HEADER = "X-Profiled-Timing-";

	private Map<ProfileStats, Long> statistics;

	private boolean enabled;

	/**
	 * Retrieve the {@link ProfileData} singleton. If it hasn't been created
	 * yet, create it and register it with the JACC context.
	 */
	public static ProfileData getProfileData() {

		if (instance == null) {
			instance = new ProfileData();
			new BasicPolicyHandler<ProfileData>()
					.put(ProfileData.KEY, instance);
		}

		return instance;
	}

	/**
	 * Create a new {@link ProfileData} instance from scratch.
	 */
	private ProfileData() {

		super();

		statistics = new HashMap<ProfileStats, Long>();
	}

	/**
	 * Create a new {@link ProfileData} instance by parsing the given headers
	 * for profile data.
	 */
	public ProfileData(Map<String, List<String>> headers) {

		this();

		// Prepare the patterns to match our headers against.
		Pattern methodRegex = Pattern.compile(METHOD_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);
		Pattern timingRegex = Pattern.compile(TIMING_HEADER + "(\\d+)",
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
			Matcher methodMatcher = methodRegex.matcher(header);
			Matcher timingMatcher = timingRegex.matcher(header);
			ProfileStats statistic = ProfileStats.getStatFor(header);

			try {
				if (null != statistic)
					statistics.put(statistic, Long.parseLong(value));

				else if (methodMatcher.matches()) {
					int index = Integer.parseInt(methodMatcher.group(1));
					methods[index] = value;
				}

				else if (timingMatcher.matches()) {
					int index = Integer.parseInt(timingMatcher.group(1));
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
			put(methods[i], timings[i]);
	}

	/**
	 * Create a map that links HTTP headers to data. You can use these headers
	 * to transport profile data over an HTTP connection.
	 */
	public Map<String, String> getHeaders() {

		Map<String, String> headers = new HashMap<String, String>();
		int entry = 0;

		for (Map.Entry<String, Long> profileEntry : entrySet()) {
			String method = profileEntry.getKey();
			String timing = String.valueOf(profileEntry.getValue());

			headers.put(METHOD_HEADER + entry, method);
			headers.put(TIMING_HEADER + entry, timing);

			entry++;
		}

		for (Map.Entry<ProfileStats, Long> statisticEntry : statistics
				.entrySet()) {
			String statistic = statisticEntry.getKey().getHeader();
			String timing = String.valueOf(statisticEntry.getValue());

			headers.put(statistic, timing);
		}

		return headers;
	}

	/**
	 * Add the given time to the given method. If no time is known for the
	 * method so far, the given time will be assigned to it.
	 */
	public void add(String method, long time) {

		Long value = get(method);
		if (null != value)
			time += value;

		put(method, time);
	}

	/**
	 * Retrieve the value for the given statistic.
	 */
	public Long getStatistic(ProfileStats statistic) {

		return statistics.get(statistic);
	}

	/**
	 * Assign a value to the given statistic.
	 */
	public void setStatistic(ProfileStats statistic, Long value) {

		statistics.put(statistic, value);
	}

	/**
	 * Enable the profiler. This also clears any existing profiling data.
	 * 
	 * @see ProfileData#clear()
	 */
	public void start() {

		enabled = true;
		clear();
	}

	/**
	 * Disable the profiler. This prevents further requests in the same
	 * Application Server thread not intended for profiling from performing
	 * unnecessary tasks.
	 */
	public void stop() {

		enabled = false;
	}

	/**
	 * Check to see if the profiler has been enabled.
	 */
	public boolean isEnabled() {

		return enabled;
	}

	/**
	 * Resets the {@link ProfileData} collected so far. Call this whenever you
	 * start profiling to prevent previous profiling runs from having their
	 * results merged with this run's data.
	 */
	@Override
	public void clear() {

		super.clear();
		statistics.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		StringBuffer result = new StringBuffer();

		result.append("Statistics:\n");
		for (Map.Entry<ProfileStats, Long> statistic : statistics.entrySet()) {
			result.append(statistic.getKey().getDescription());
			result.append(":  ");
			result.append(statistic.getValue());
			result.append('\n');
		}

		result.append("\nCalls:\n");
		for (Map.Entry<String, Long> call : entrySet()) {
			result.append(call.getKey());
			result.append(":  ");
			result.append(call.getValue());
			result.append('\n');
		}

		return result.toString();
	}
}
