/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.HistoryEntity.QUERY_DELETE_WHERE_OLDER;
import static net.link.safeonline.entity.HistoryEntity.QUERY_WHERE_SUBJECT;
import static net.link.safeonline.entity.HistoryEntity.DELETE_ALL;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

@Entity
@Table(name = "hist")
@NamedQueries( {
		@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT history "
				+ "FROM HistoryEntity AS history "
				+ "WHERE history.subject = :subject "
				+ "ORDER BY history.when DESC"),
		@NamedQuery(name = QUERY_DELETE_WHERE_OLDER, query = "DELETE "
				+ "FROM HistoryEntity AS history "
				+ "WHERE history.when < :ageLimit"),
		@NamedQuery(name = DELETE_ALL, query = "DELETE FROM HistoryEntity AS history "
				+ "WHERE history.subject = :subject") })
public class HistoryEntity implements Serializable {

	public static final String QUERY_WHERE_SUBJECT = "hist.subject";

	public static final String QUERY_DELETE_WHERE_OLDER = "hist.old";

	public static final String DELETE_ALL = "hist.del";

	private static final long serialVersionUID = 1L;

	private long id;

	private SubjectEntity subject;

	private HistoryEventType event;

	private String application;

	private String info;

	private Date when;

	@ManyToOne(optional = false)
	public SubjectEntity getSubject() {
		return subject;
	}

	public void setSubject(SubjectEntity subject) {
		this.subject = subject;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public HistoryEntity() {
		// empty
	}

	public HistoryEntity(Date when, SubjectEntity subject,
			HistoryEventType event, String application, String info) {
		this.subject = subject;
		this.event = event;
		this.application = application;
		this.when = when;
		this.info = info;
	}

	@Column(name = "histevent", nullable = false)
	public HistoryEventType getEvent() {
		return event;
	}

	public void setEvent(HistoryEventType event) {
		this.event = event;
	}

	@Column(name = "whendate", nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getWhen() {
		return when;
	}

	public void setWhen(Date when) {
		this.when = when;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}

	public static Query createQueryDeleteWhereOlder(
			EntityManager entityManager, long ageInMillis) {
		Query query = entityManager.createNamedQuery(QUERY_DELETE_WHERE_OLDER);
		Date ageLimit = new Date(System.currentTimeMillis() - ageInMillis);
		query.setParameter("ageLimit", ageLimit);
		return query;
	}

	public interface QueryInterface {
		@QueryMethod(QUERY_WHERE_SUBJECT)
		List<HistoryEntity> getHistory(@QueryParam("subject")
		SubjectEntity subject);

		@UpdateMethod(DELETE_ALL)
		void deleteAll(@QueryParam("subject")
		SubjectEntity subject);
	}
}
