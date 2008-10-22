/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import net.link.safeonline.demo.cinema.entity.CinemaFilmEntity;
import net.link.safeonline.demo.cinema.entity.CinemaRoomEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatEntity;
import net.link.safeonline.demo.cinema.entity.CinemaSeatOccupationEntity;
import net.link.safeonline.demo.cinema.entity.CinemaShowTimeEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTheatreEntity;
import net.link.safeonline.demo.cinema.entity.CinemaTicketEntity;
import net.link.safeonline.demo.cinema.entity.CinemaUserEntity;
import net.link.safeonline.demo.cinema.service.bean.FilmServiceBean;
import net.link.safeonline.demo.cinema.service.bean.InitializationServiceBean;
import net.link.safeonline.demo.cinema.service.bean.RoomServiceBean;
import net.link.safeonline.demo.cinema.service.bean.SeatServiceBean;
import net.link.safeonline.demo.cinema.service.bean.TheatreServiceBean;
import net.link.safeonline.demo.cinema.service.bean.TicketServiceBean;
import net.link.safeonline.demo.cinema.service.bean.UserServiceBean;
import net.link.safeonline.test.util.AbstractServiceTest;


/**
 * <h2>{@link AbstractCinemaServiceTest}<br>
 * <sub>Abstract class providing all cinema entities and service beans for unit tests.</sub></h2>
 * 
 * <p>
 * <i>Oct 16, 2008</i>
 * </p>
 * 
 * @author lhunath
 */
public abstract class AbstractCinemaServiceTest extends AbstractServiceTest {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getEntities() {

        return new Class<?>[] { CinemaFilmEntity.class, CinemaRoomEntity.class, CinemaSeatEntity.class,
                CinemaSeatOccupationEntity.class, CinemaShowTimeEntity.class, CinemaTheatreEntity.class,
                CinemaTicketEntity.class, CinemaUserEntity.class };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<?>[] getServices() {

        return new Class<?>[] { FilmServiceBean.class, InitializationServiceBean.class, RoomServiceBean.class,
                SeatServiceBean.class, TheatreServiceBean.class, TicketServiceBean.class, UserServiceBean.class };
    }
}
