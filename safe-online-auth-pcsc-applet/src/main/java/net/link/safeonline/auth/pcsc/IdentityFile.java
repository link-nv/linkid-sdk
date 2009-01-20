/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */

package net.link.safeonline.auth.pcsc;

import java.util.Date;


public class IdentityFile {

    @Tag(1)
    String cardNumber;

    @Tag(2)
    String chipNumber;

    @Tag(3)
    @Convert(ValidityDateConvertor.class)
    Date   validityDateBegin;

    @Tag(4)
    @Convert(ValidityDateConvertor.class)
    Date   validityDateEnd;

    @Tag(5)
    String municipal;

    @Tag(6)
    String nationalNumber;

    @Tag(7)
    String name;

    @Tag(8)
    String firstName;

    @Tag(9)
    String middleNameLetter;

    @Tag(10)
    String nationality;

    @Tag(11)
    String birthLocation;

    @Tag(12)
    @Convert(BirthDateConvertor.class)
    Date   birthDate;

    @Tag(13)
    @Convert(SexConvertor.class)
    Sex    sex;

    @Tag(14)
    String nobleCondition;

    @Tag(15)
    String documentType;

    @Tag(16)
    String specialStatus;

    @Tag(17)
    byte[] hashPhoto;


    public String getCardNumber() {

        return cardNumber;
    }

    public String getChipNumber() {

        return chipNumber;
    }

    public Date getValidityDateBegin() {

        return validityDateBegin;
    }

    public Date getValidityDateEnd() {

        return validityDateEnd;
    }

    public String getMunicipal() {

        return municipal;
    }

    public String getNationalNumber() {

        return nationalNumber;
    }

    public String getName() {

        return name;
    }

    public String getFirstName() {

        return firstName;
    }

    public String getMiddleNameLetter() {

        return middleNameLetter;
    }

    public String getNationality() {

        return nationality;
    }

    public String getBirthLocation() {

        return birthLocation;
    }

    public Date getBirthDate() {

        return birthDate;
    }

    public Sex getSex() {

        return sex;
    }

    public String getNobleCondition() {

        return nobleCondition;
    }

    public String getDocumentType() {

        return documentType;
    }

    public String getSpecialStatus() {

        return specialStatus;
    }

    public byte[] getHashPhoto() {

        return hashPhoto;
    }
}
