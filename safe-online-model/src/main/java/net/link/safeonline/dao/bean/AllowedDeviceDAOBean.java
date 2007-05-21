package net.link.safeonline.dao.bean;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import net.link.safeonline.SafeOnlineConstants;
import net.link.safeonline.dao.AllowedDeviceDAO;
import net.link.safeonline.entity.AllowedDeviceEntity;
import net.link.safeonline.entity.ApplicationEntity;
import net.link.safeonline.entity.DeviceEntity;

@Stateless
public class AllowedDeviceDAOBean implements AllowedDeviceDAO {

	@PersistenceContext(unitName = SafeOnlineConstants.SAFE_ONLINE_ENTITY_MANAGER)
	private EntityManager entityManager;

	public AllowedDeviceEntity addAllowedDevice(ApplicationEntity application,
			DeviceEntity device, int weight) {
		AllowedDeviceEntity allowedDevice = new AllowedDeviceEntity(
				application, device, weight);
		this.entityManager.persist(allowedDevice);
		return allowedDevice;
	}

	@SuppressWarnings("unchecked")
	public List<AllowedDeviceEntity> listAllowedDevices(
			ApplicationEntity application) {
		Query query = AllowedDeviceEntity
				.createQueryListAllowedDevicesByApplication(this.entityManager,
						application);
		List<AllowedDeviceEntity> result = query.getResultList();
		return result;
	}

	public void deleteAllowedDevices(ApplicationEntity application) {
		Query query = AllowedDeviceEntity.createQueryDeleteByApplication(
				this.entityManager, application);
		query.executeUpdate();
	}

}
