/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.data.ws;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import liberty.dst._2006_08.ref.CreateResponseType;
import liberty.dst._2006_08.ref.CreateType;
import liberty.dst._2006_08.ref.DataServicePort;
import liberty.dst._2006_08.ref.DeleteResponseType;
import liberty.dst._2006_08.ref.DeleteType;
import liberty.dst._2006_08.ref.ModifyResponseType;
import liberty.dst._2006_08.ref.ModifyType;
import liberty.dst._2006_08.ref.QueryResponseType;
import liberty.dst._2006_08.ref.QueryType;

public class DataServicePortImpl implements DataServicePort {

	private static final Log LOG = LogFactory.getLog(DataServicePortImpl.class);

	public CreateResponseType create(CreateType request) {
		LOG.debug("create");
		return null;
	}

	public DeleteResponseType delete(DeleteType request) {
		LOG.debug("delete");
		return null;
	}

	public ModifyResponseType modify(ModifyType request) {
		LOG.debug("modify");
		return null;
	}

	public QueryResponseType query(QueryType request) {
		LOG.debug("query");
		return null;
	}
}
