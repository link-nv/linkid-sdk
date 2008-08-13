/*
 * SafeOnline project.
 * 
 * Copyright 2006-2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.demo.payment.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "demo_payment_user")
public class UserEntity implements Serializable {

    private static final long   serialVersionUID = 1L;

    private List<PaymentEntity> payments;

    private String              safeOnlineUserName;


    public UserEntity() {

        this(null);
    }

    public UserEntity(String safeOnlineUserName) {

        this.payments = new ArrayList<PaymentEntity>();
        this.safeOnlineUserName = safeOnlineUserName;
    }

    @Id
    public String getSafeOnlineUserName() {

        return this.safeOnlineUserName;
    }

    public void setSafeOnlineUserName(String safeOnlineUserName) {

        this.safeOnlineUserName = safeOnlineUserName;
    }

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    public List<PaymentEntity> getPayments() {

        return this.payments;
    }

    public void setPayments(List<PaymentEntity> payments) {

        this.payments = payments;
    }
}
