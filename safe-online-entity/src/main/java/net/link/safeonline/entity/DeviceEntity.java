package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Query;
import javax.persistence.Table;

import static net.link.safeonline.entity.DeviceEntity.QUERY_LIST_ALL;

@Entity
@Table(name = "devices")
@NamedQueries( { @NamedQuery(name = QUERY_LIST_ALL, query = "FROM DeviceEntity d") })
public class DeviceEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	public static final String QUERY_LIST_ALL = "dev.all";

	private String name;

	private List<AttributeTypeEntity> attributeTypes;

	public DeviceEntity() {
		// empty
	}

	public DeviceEntity(String name) {
		this.name = name;
	}

	@Id
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@OneToMany
	public List<AttributeTypeEntity> getAttributeTypes() {
		return attributeTypes;
	}

	public void setAttributeTypes(List<AttributeTypeEntity> attributeTypes) {
		this.attributeTypes = attributeTypes;
	}

	public static Query createQueryListAll(EntityManager entityManager) {
		Query query = entityManager.createNamedQuery(QUERY_LIST_ALL);
		return query;
	}

}
