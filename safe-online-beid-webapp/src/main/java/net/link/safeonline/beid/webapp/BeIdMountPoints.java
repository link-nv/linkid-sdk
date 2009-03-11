/*
 * SafeOnline project.
 *
 * Copyright 2006-2009 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.beid.webapp;

import org.apache.wicket.Page;
import org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy;
import org.apache.wicket.request.target.coding.QueryStringUrlCodingStrategy;


/**
 * <h2>{@link BeIdMountPoints}<br>
 * <sub>All wicket mount points supported by this web application.</sub></h2>
 * 
 * <p>
 * <i>Jan 7, 2009</i>
 * </p>
 * 
 * @author lhunath
 */
public class BeIdMountPoints {

    interface MountPointType {

        String getTypeValue();
    }

    public enum AuthenticationType implements MountPointType {
        PKCS11("pkcs11"),
        PCSC("pcsc");

        private String typeValue;


        private AuthenticationType(String typeValue) {

            this.typeValue = typeValue;
        }

        /**
         * {@inheritDoc}
         */
        public String getTypeValue() {

            return typeValue;
        }
    }

    public enum ErrorType implements MountPointType {
        BAD_PLATFORM("platform"),
        NO_MIDDLEWARE("middleware"),
        BAD_JAVA_VERSION("java-version"),
        NO_JAVA("java-disabled"),
        NO_READER("reader"),
        PROTOCOL_VIOLATION("protocol");

        private String typeValue;


        private ErrorType(String typeValue) {

            this.typeValue = typeValue;
        }

        /**
         * {@inheritDoc}
         */
        public String getTypeValue() {

            return typeValue;
        }
    }

    public enum MountPoint {
        AUTHENTICATION("authenticate", AuthenticationPage.class, AuthenticationType.class),
        IDENTIFICATION("identify", IdentificationPage.class, AuthenticationType.class),
        REGISTRATION("register", RegistrationPage.class, AuthenticationType.class),
        ENABLE("enable", EnablePage.class, AuthenticationType.class),
        ERROR("beid_error", BeIdErrorPage.class, ErrorType.class);

        public static final String              TYPE_PARAMETER = "type";

        private String                          mountPoint;
        private Class<? extends Page>           mountPage;
        private Class<? extends MountPointType> mountPointType;


        MountPoint(String mountPoint, Class<? extends Page> mountPage, Class<? extends MountPointType> mountPointType) {

            this.mountPoint = mountPoint;
            this.mountPage = mountPage;
            this.mountPointType = mountPointType;
        }

        public String getMountPoint() {

            return mountPoint;
        }

        public String linkFor(MountPointType type) {

            if (!mountPointType.isInstance(type))
                throw new IllegalArgumentException("The given type is not compatible with this mount point: Requires a " + mountPointType);

            return String.format("%s?%s=%s", mountPoint, TYPE_PARAMETER, type.getTypeValue());
        }

        IRequestTargetUrlCodingStrategy getUCS() {

            return new QueryStringUrlCodingStrategy(mountPoint, mountPage);
        }
    }
}
