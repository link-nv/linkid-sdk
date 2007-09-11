/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity.helpdesk;

import static net.link.safeonline.entity.helpdesk.HelpdeskContextEntity.QUERY_LIST_ALL;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import net.link.safeonline.jpa.annotation.QueryMethod;

@Entity
@Table(name = "helpdesk_context")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "SELECT context FROM HelpdeskContextEntity as context") })
public class HelpdeskContextEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "hdcon.all";

	private Long id;

	private String location;

	public HelpdeskContextEntity() {
	}

	public HelpdeskContextEntity(String location) {
		this.location = location;
	}

	// used by unit tests
	public HelpdeskContextEntity(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLocation() {
		return this.location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_LIST_ALL)
		List<HelpdeskContextEntity> listContexts();
	}

}