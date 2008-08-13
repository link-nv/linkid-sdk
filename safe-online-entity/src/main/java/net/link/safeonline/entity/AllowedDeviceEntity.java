/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import net.link.safeonline.jpa.annotation.QueryMethod;
import net.link.safeonline.jpa.annotation.QueryParam;
import net.link.safeonline.jpa.annotation.UpdateMethod;

import static net.link.safeonline.entity.AllowedDeviceEntity.QUERY_WHERE_APPLICATION;
import static net.link.safeonline.entity.AllowedDeviceEntity.DELETE_WHERE_APPLICATION;
import static net.link.safeonline.entity.AllowedDeviceEntity.QUERY_WHERE_APPLICATION_DEVICE;


@Entity
@Table(name = "alloweddevices", uniqueConstraints = @UniqueConstraint(columnNames = { "application", "device" }))
@NamedQueries( {
        @NamedQuery(name = QUERY_WHERE_APPLICATION, query = "SELECT allowedDevice "
                + "FROM AllowedDeviceEntity AS allowedDevice " + "WHERE allowedDevice.application = :application"),
        @NamedQuery(name = DELETE_WHERE_APPLICATION, query = "DELETE FROM AllowedDeviceEntity "
                + "AS allowedDevice WHERE allowedDevice.application = :application"),
        @NamedQuery(name = QUERY_WHERE_APPLICATION_DEVICE, query = "SELECT allowedDevice "
                + "FROM AllowedDeviceEntity AS allowedDevice " + "WHERE allowedDevice.application = :application AND "
                + "allowedDevice.device = :device") })
public class AllowedDeviceEntity implements Serializable {

    private static final long  serialVersionUID               = 1L;

    public static final String QUERY_WHERE_APPLICATION        = "alloweddevice.app";

    public static final String DELETE_WHERE_APPLICATION       = "alloweddevice.del";

    public static final String QUERY_WHERE_APPLICATION_DEVICE = "alloweddevice.app.dev";

    private long               id;

    private ApplicationEntity  application;

    private DeviceEntity       device;

    private int                weight;


    public AllowedDeviceEntity() {

        // empty
    }

    public AllowedDeviceEntity(ApplicationEntity application, DeviceEntity device, int weight) {

        this.application = application;
        this.device = device;
        this.weight = weight;
    }

    @ManyToOne
    @JoinColumn(name = "application", nullable = false)
    public ApplicationEntity getApplication() {

        return this.application;
    }

    public void setApplication(ApplicationEntity application) {

        this.application = application;
    }

    @ManyToOne
    @JoinColumn(name = "device", nullable = false)
    public DeviceEntity getDevice() {

        return this.device;
    }

    public void setDevice(DeviceEntity device) {

        this.device = device;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long getId() {

        return this.id;
    }

    public void setId(long id) {

        this.id = id;
    }

    public int getWeight() {

        return this.weight;
    }

    public void setWeight(int weight) {

        this.weight = weight;
    }


    public interface QueryInterface {

        @QueryMethod(QUERY_WHERE_APPLICATION)
        List<AllowedDeviceEntity> listAllowedDevices(@QueryParam("application") ApplicationEntity application);

        @UpdateMethod(DELETE_WHERE_APPLICATION)
        void deleteAllowedDevices(@QueryParam("application") ApplicationEntity application);

        @QueryMethod(value = QUERY_WHERE_APPLICATION_DEVICE, nullable = true)
        AllowedDeviceEntity find(@QueryParam("application") ApplicationEntity application,
                @QueryParam("device") DeviceEntity device);
    }
}
