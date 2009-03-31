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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;


@Entity
@Table(name = "dPaymentUser")
@NamedQueries( { @NamedQuery(name = PaymentUserEntity.getAll, query = "SELECT u FROM PaymentUserEntity u") })
public class PaymentUserEntity implements Serializable {

    private static final long   serialVersionUID = 1L;
    public static final String  getAll           = "PaymentUserEntity.getAll";

    @Id
    private String              olasId;

    private String              olasName;

    @OneToMany(mappedBy = "owner", fetch = FetchType.EAGER)
    private List<PaymentEntity> payments;


    public PaymentUserEntity() {

        this(null, null);
    }

    public PaymentUserEntity(String olasId, String olasName) {

        payments = new ArrayList<PaymentEntity>();
        this.olasId = olasId;
        this.olasName = olasName;
    }

    public String getOlasId() {

        return olasId;
    }

    public void setOlasId(String olasId) {

        this.olasId = olasId;
    }

    public String getOlasName() {

        return olasName;
    }

    public void setOlasName(String olasName) {

        this.olasName = olasName;
    }

    public List<PaymentEntity> getPayments() {

        return payments;
    }

    public void setPayments(List<PaymentEntity> payments) {

        this.payments = payments;
    }
}
