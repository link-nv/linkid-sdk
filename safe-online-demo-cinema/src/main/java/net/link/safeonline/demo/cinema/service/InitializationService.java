/*
 * SafeOnline project.
 *
 * Copyright 2006-2008 Lin.k N.V. All rights reserved.
 * Lin.k N.V. proprietary/confidential. Use is subject to license terms.
 */
package net.link.safeonline.demo.cinema.service;

import javax.ejb.Local;

import net.link.safeonline.demo.cinema.entity.ShowTimeEntity;


/**
 * <h2>{@link InitializationService}<br>
 * <sub>Service that executes after application deployment.</sub></h2>
 *
 * <p>
 * Creates some initial dummy entities to fill up the database with theatres, films, rooms and seats.
 * </p>
 *
 * <p>
 * <i>Jun 23, 2008</i>
 * </p>
 *
 * @author mbillemo
 */
@Local
public interface InitializationService extends CinemaService {

    public static final String             BINDING          = JNDI_PREFIX + "InitializationServiceBean/local";

    static final int                       H                = 3600, M = 60;

    public static final String[]           filmNames        = {
                                                            // Journey
            "Journey to the Center of the Earth",
            // Happening
            "The Happening",
            // Knight
            "The Dark Knight",
            // Shaun
            "Shaun Of The Dead",
            // Hellboy
            "Hellboy II: The Golden Army"                  };
    public static final String[]           filmDescriptions = {
                                                            // Journey
            "On a quest to find out what happened to his missing brother, a scientist, "
                    + "his nephew and their mountain guide discover a fantastic and "
                    + "dangerous lost world in the center of the earth.",
            // Happening
            "A paranoid thriller about a family on the run from a "
                    + "natural crisis that presents a large-scale threat to humanity..",
            // Knight
            "Batman and James Gordon join forces with Gotham's new District Attorney, "
                    + "Harvey Dent, to take on a psychotic bank robber known as The Joker, "
                    + "whilst other forces plot against them, and Joker's crimes grow more " + "and more deadly..",
            // Shaun
            "A man decides to turn his moribund life around by winning back his ex-"
                    + "girlfriend, reconciling his relationship with his mother, "
                    + "and dealing with an entire community that has returned " + "from the dead to eat the living.",
            // Hellboy
            "The mythical world starts a rebellion against humanity in order to rule "
                    + "the Earth, so Hellboy and his team must save the world from " + "the rebellious creatures.." };
    public static final ShowTimeEntity[][] filmTimes        = {
                                                            // Journey
            { new ShowTimeEntity(14 * H, 14 * H, 15 * H, 14 * H, 14 * H + 30 * M, 16 * H, 16 * H),
            new ShowTimeEntity(20 * H, 20 * H, 20 * H + 15 * M, 20 * H, 20 * H, 22 * H, 22 * H) },
            // Happening
            { new ShowTimeEntity(14 * H, 14 * H, 15 * H, 14 * H, 14 * H + 30 * M, 16 * H, 16 * H),
            new ShowTimeEntity(20 * H, 20 * H, 20 * H + 15 * M, 20 * H, 20 * H, 22 * H, 22 * H) },
            // Knight
            { new ShowTimeEntity(20 * H, 20 * H, 20 * H, 20 * H + 15 * M, 20 * H, 22 * H, 22 * H),
            new ShowTimeEntity(23 * H, 23 * H, 23 * H, 23 * H, 23 * H, null, null) },
            // Shaun
            { new ShowTimeEntity(20 * H, 20 * H, 20 * H, 20 * H + 15 * M, 20 * H, 22 * H, 22 * H),
            new ShowTimeEntity(23 * H, 23 * H, 23 * H, 23 * H, 23 * H, null, null) },
            // Hellboy
            { new ShowTimeEntity(20 * H, 20 * H, 20 * H, 20 * H + 15 * M, 20 * H, 22 * H, 22 * H),
            new ShowTimeEntity(23 * H, 23 * H, 23 * H, 23 * H, 23 * H, null, null) } };
    public static final long[]             filmDurations    = { 92 * M, 91 * M, 97 * M, 99 * M, 90 * M };
    public static final int[]              filmPrices       = { 30, 30, 25, 20, 20 };
    public static final int[][]            filmTheatres     = {
                                                            // Journey
            { 0, 2, 4 },
            // Happening
            { 0, 1, 4 },
            // Knight
            { 3, 4 },
            // Shaun
            { 2, 3, 4 },
            // Hellboy
            { 1, 3 }                                       };
    public static final int[][][]          filmRooms        = {
                                                            // Journey
            { { 0 }, { 1 }, { 2 } },
            // Happening
            { { 1 }, { 1 }, { 1 } },
            // Knight
            { { 0 }, { 0 } },
            // Shaun
            { { 3 }, { 3 }, { 0 } },
            // Hellboy
            { { 0 }, { 3 } }                               };

    public static final String[]           theatreNames     = {
                                                            // Gent
            "Kinepolis Gent",
            // Kortrijk
            "Kinepolis Kortrijk",
            // Hasselt
            "Kinepolis Hasselt",
            // Brussel
            "Kinepolis Brussel",
            // Leuven
            "Kinepolis Leuven"                             };
    public static final String[]           theatreAdresses  = {
                                                            // Gent
            "Ter Platen 12\n9000 Gent",
            // Kortrijk
            "President Kennedylaan 100A\n8500 Kortrijk",
            // Hasselt
            "Via Media 1\n3500 Hasselt",
            // Brussel
            "Eeuwfeestlaan 20\n1020 Brussel",
            // Leuven
            "Bondgenotenlaan 145-149\n3000 Leuven"         };
    public static final String[][]         theatreRooms     = {
                                                            // Gent
            { "A", "B", "C" },
            // Kortrijk
            { "A", "B" },
            // Hasselt
            { "A", "B", "C", "D" },
            // Brussel
            { "A", "B", "C", "D" },
            // Leuven
            { "A", "B", "C" }                              };
    public static final int[][][]          theatreRoomSeats = {
                                                            // Gent
            { { 10, 4 }, { 12, 5 }, { 8, 5 } },
            // Kortrijk
            { { 8, 4 }, { 8, 6 } },
            // Hasselt
            { { 10, 5 }, { 12, 8 }, { 12, 8 }, { 10, 8 } },
            // Brussel
            { { 8, 4 }, { 10, 5 }, { 12, 8 }, { 10, 8 } },
            // Leuven
            { { 8, 8 }, { 10, 8 }, { 6, 4 } }              };


    public void buildEntities();
}
