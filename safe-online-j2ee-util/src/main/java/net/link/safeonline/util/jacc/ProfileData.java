/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.util.jacc;

import java.util.HashMap;
import java.util.LinkedList;
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
public class ProfileData extends LinkedList<Call> {

	private static BasicPolicyHandler<ProfileData> handler;

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
	 * The header used to identify the method signature for a profile entry.
	 */
	private static final String METHODSIG_HEADER = "X-Profiled-Method-";

	/**
	 * The header used to identify the time at which the method call was
	 * initiated.
	 */
	private static final String INITIATED_HEADER = "X-Profiled-Initiated-";

	/**
	 * The header used to identify the time at which the method was completed.
	 */
	private static final String DURATION_HEADER = "X-Profiled-Duration-";

	private Map<ProfileStats, Long> statistics;

	private boolean enabled;

	/**
	 * Retrieve the {@link ProfileData} registered with the active JACC Context
	 * (the context for the active thread). If we haven't created a handler yet,
	 * then do so. If we haven't registered a {@link ProfileData} with the
	 * handler yet for the current context, do so as well.
	 */
	public static ProfileData getProfileData() {

		if (null == handler)
			handler = new BasicPolicyHandler<ProfileData>();

		if (!handler.supports(KEY))
			handler.register(KEY, new ProfileData());

		return handler.getContext(KEY);
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
		Pattern methodSigRegex = Pattern.compile(METHODSIG_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);
		Pattern initiatedRegex = Pattern.compile(INITIATED_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);
		Pattern completedRegex = Pattern.compile(DURATION_HEADER + "(\\d+)",
				Pattern.CASE_INSENSITIVE);

		// We'll first collect data we care about in arrays.
		String[] methods = new String[headers.size()];
		Long[] initiated = new Long[headers.size()];
		Long[] completed = new Long[headers.size()];

		// Parse every header to see if it's a profiler header and extract
		// interesting data into the arrays.
		for (Map.Entry<String, List<String>> headerEntry : headers.entrySet()) {
			String header = headerEntry.getKey();
			if (header == null || headerEntry.getValue().isEmpty())
				continue;

			String value = headerEntry.getValue().get(0);
			Matcher methodMatcher = methodSigRegex.matcher(header);
			Matcher initiatedMatcher = initiatedRegex.matcher(header);
			Matcher completedMatcher = completedRegex.matcher(header);
			ProfileStats statistic = ProfileStats.getStatFor(header);

			try {
				if (null != statistic)
					statistics.put(statistic, Long.parseLong(value));

				else if (methodMatcher.matches()) {
					int index = Integer.parseInt(methodMatcher.group(1));
					methods[index] = value;
				}

				else if (initiatedMatcher.matches()) {
					int index = Integer.parseInt(initiatedMatcher.group(1));
					initiated[index] = Long.parseLong(value);
				}

				else if (completedMatcher.matches()) {
					int index = Integer.parseInt(completedMatcher.group(1));
					completed[index] = Long.parseLong(value);
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
				add(new Call(methods[i], initiated[i], completed[i]));
	}

	/**
	 * Create a map that links HTTP headers to data. You can use these headers
	 * to transport profile data over an HTTP connection.
	 */
	public Map<String, String> getHeaders() {

		Map<String, String> headers = new HashMap<String, String>();
		int entry = 0;

		for (Call call : this) {
			String methodSig = call.getSignature();
			String initiated = String.valueOf(call.getInitiated().getTime());
			String duration = String.valueOf(call.getDuration());

			headers.put(METHODSIG_HEADER + entry, methodSig);
			headers.put(INITIATED_HEADER + entry, initiated);
			headers.put(DURATION_HEADER + entry, duration);

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

		result.append("\nCalls (" + size() + "):\n");
		for (Call call : this)
			result.append(String.format("[%s] Spent %d ms in '%s'.%n", call
					.getInitiated().toString(), call.getDuration(), call
					.getSignature()));

		return result.toString();
	}
}