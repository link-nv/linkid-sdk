package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.DeviceDAO;
import net.link.safeonline.entity.DeviceEntity;

@Stateless
public class DeviceDAOBean implements DeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public DeviceEntity addDevice(String name) {
		DeviceEntity device = new DeviceEntity(name);
		this.entityManager.persist(device);
		return device;
	}

	@SuppressWarnings("unchecked")
	public List<DeviceEntity> listDevices() {
		Query query = DeviceEntity.createQueryListAll(this.entityManager);
		List<DeviceEntity> result = query.getResultList();
		return result;
	}

}
