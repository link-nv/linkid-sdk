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

import static net.link.safeonline.entity.HistoryEntity.QUERY_WHERE_ENTITY;

@Entity
@Table(name = "hist")
@NamedQueries(@NamedQuery(name = QUERY_WHERE_ENTITY, query = "SELECT history FROM HistoryEntity AS history WHERE history.entity = :entity ORDER BY history.when DESC"))
public class HistoryEntity implements Serializable {

	public static final String QUERY_WHERE_ENTITY = "hist.entity";

	private static final long serialVersionUID = 1L;

	private long id;

	private EntityEntity entity;

	private String event;

	private Date when;

	@ManyToOne(optional = false)
	public EntityEntity getEntity() {
		return entity;
	}

	public void setEntity(EntityEntity entity) {
		this.entity = entity;
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

	public HistoryEntity(Date when, EntityEntity entity, String event) {
		this.event = event;
		this.entity = entity;
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
			EntityEntity entity) {
		Query query = entityManager.createNamedQuery(QUERY_WHERE_ENTITY);
		query.setParameter("entity", entity);
		return query;
	}
}
