/*
 * SafeOnline project.
 * 
 * Copyright 2006 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.Date;

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

import static net.link.safeonline.entity.HistoryEntity.QUERY_WHERE_SUBJECT;

@Entity
@Table(name = "hist")
@NamedQueries(@NamedQuery(name = QUERY_WHERE_SUBJECT, query = "SELECT history FROM HistoryEntity AS history WHERE history.subject = :subject ORDER BY history.when DESC"))
public class HistoryEntity implements Serializable {

	public static final String QUERY_WHERE_SUBJECT = "hist.subject";

	private static final long serialVersionUID = 1L;

	private long id;

	private SubjectEntity subject;

	private String event;

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

	public HistoryEntity(Date when, SubjectEntity subject, String event) {
		this.event = event;
		this.subject = subject;
		this.when = when;
	}

	@Column(name = "histevent", nullable = false)
	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	@Column(name = "whendate", nullable = false)
	public Date getWhen() {
		return when;
	}

	public void setWhen(Date when) {
		this.when = when;
	}

	public static Query createQueryWhereEntity(EntityManager entityManager,
			SubjectEntity subject) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_SUBJECT);
		query.setParameter("subject", subject);
		return query;
	}
}
