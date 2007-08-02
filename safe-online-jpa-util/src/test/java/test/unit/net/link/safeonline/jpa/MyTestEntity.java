/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package test.unit.net.link.safeonline.jpa;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import static test.unit.net.link.safeonline.jpa.MyTestEntity.QUERY_ALL;
import static test.unit.net.link.safeonline.jpa.MyTestEntity.QUERY_WHERE_NAME;
import static test.unit.net.link.safeonline.jpa.MyTestEntity.NAME_PARAM;
import static test.unit.net.link.safeonline.jpa.MyTestEntity.DELETE_ALL;

@Entity
@NamedQueries( {
		@NamedQuery(name = QUERY_ALL, query = "FROM MyTestEntity"),
		@NamedQuery(name = QUERY_WHERE_NAME, query = "FROM MyTestEntity AS mte WHERE mte.name = :"
				+ NAME_PARAM),
		@NamedQuery(name = DELETE_ALL, query = "DELETE FROM MyTestEntity") })
public class MyTestEntity implements Serializable {

	public static final String QUERY_ALL = "mte.all";

	public static final String QUERY_WHERE_NAME = "mte.name";

	public static final String DELETE_ALL = "mte.del.all";

	public static final String NAME_PARAM = "name";

	private static final long serialVersionUID = 1L;

	public MyTestEntity() {
		this(null);
	}

	public MyTestEntity(String name) {
		this.name = name;
	}

	private String name;

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public interface MyQueryTestInterface {

		@QueryMethod(QUERY_ALL)
		List<MyTestEntity> listAll();

		@QueryMethod(QUERY_WHERE_NAME)
		List<MyTestEntity> listAll(@QueryParam(NAME_PARAM)
		String name);

		@QueryMethod(QUERY_WHERE_NAME)
		MyTestEntity get(@QueryParam(NAME_PARAM)
		String name);

		@QueryMethod(value = QUERY_WHERE_NAME, nullable = true)
		MyTestEntity find(@QueryParam(NAME_PARAM)
		String name);

		@UpdateMethod(DELETE_ALL)
		void removeAll();
	}
}
