/*
 * SafeOnline project.
 *
 * Copyright 2007 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.entity;

import static net.link.safeonline.entity.MetadataEntity.TABLE_NAME;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = TABLE_NAME)
public class MetadataEntity {

    public static final String TABLE_NAME = "metadata";

    private String             name;

    private String             value;


    public MetadataEntity() {

        // empty
    }

    public MetadataEntity(String name, String value) {

        this.name = name;
        this.value = value;
    }

    @Id
    public String getName() {

        return this.name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getValue() {

        return this.value;
    }

    public void setValue(String value) {

        this.value = value;
    }
}
