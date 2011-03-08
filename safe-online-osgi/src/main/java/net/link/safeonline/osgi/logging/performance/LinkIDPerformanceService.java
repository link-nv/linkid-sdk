/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.osgi.logging.performance;

import java.util.Map;


/**
 * <h2>{@link LinkIDPerformanceService}<br>
 * <sub>linkID Audit Service API.</sub></h2>
 *
 * <p>
 * linkID Performance Service API. This service should be used if OSGi plugins wish to retrieve performance info like number of
 * authentications, ... . See {@link PerformanceMeter} for available information.
 * </p>
 *
 * <p>
 * <i>Nov 12, 2009</i>
 * </p>
 *
 * @author wvdhaute
 */
public interface LinkIDPerformanceService {

    /**
     * Request the specific {@link PerformanceMeter}.
     *
     * @return counter for specified meter.
     */
    public Long getCounter(PerformanceMeter performanceMeter);

    /**
     * Returns all {@link PerformanceMeter}s
     *
     * @return all performance meters.
     */
    public Map<PerformanceMeter, Long> getCounters();
}
