/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.dao;

import java.security.cert.X509Certificate;
import java.util.List;

import javax.ejb.Local;

import net.link.safeonline.authentication.exception.NodeNotFoundException;
import net.link.safeonline.entity.OlasEntity;

@Local
public interface OlasDAO {

	OlasEntity addNode(String name, String location, X509Certificate certificate);

	List<OlasEntity> listNodes();

	OlasEntity findNode(String name);

	OlasEntity getNode(String name) throws NodeNotFoundException;

	void removeNode(OlasEntity node);
}
