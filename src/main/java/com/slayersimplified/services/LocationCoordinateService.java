/*
 * BSD 2-Clause License
 * Copyright (c) 2026, Slayer Simplified contributors
 * See LICENSE for details.
 *
 * This service loads a mapping of location names to WorldPoint coordinates
 * from a bundled JSON resource. Used to resolve location strings from
 * tasks.json into navigable coordinates.
 */
package com.slayersimplified.services;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.coords.WorldPoint;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads and provides a lookup from location name strings (e.g. "Slayer Tower")
 * to their corresponding WorldPoint coordinates for pathfinding navigation.
 *
 * Coordinates are loaded from /data/location_coordinates.json at startup.
 */
@Slf4j
@Singleton
public class LocationCoordinateService
{
    private final Map<String, WorldPoint> coordinates;

    @Inject
    public LocationCoordinateService(
            Gson gson,
            @Named("locationDataPath") String locationDataPath)
    {
        Map<String, WorldPoint> loaded = new HashMap<>();

        InputStream inputStream = this.getClass().getResourceAsStream(locationDataPath);

        if (inputStream == null)
        {
            log.error("Could not find location coordinates JSON at path {}", locationDataPath);
            this.coordinates = Collections.emptyMap();
            return;
        }

        try (Reader reader = new InputStreamReader(inputStream))
        {
            // Deserialize as Map<String, CoordEntry> then convert to WorldPoint
            Type type = new TypeToken<Map<String, CoordEntry>>() {}.getType();
            Map<String, CoordEntry> data = gson.fromJson(reader, type);

            data.forEach((locationName, entry) ->
                loaded.put(locationName.toLowerCase(), new WorldPoint(entry.x, entry.y, entry.plane))
            );

            log.info("Loaded {} location coordinates for Slayer Simplified", loaded.size());
        }
        catch (JsonSyntaxException e)
        {
            log.error("JSON syntax error in location coordinates file {}", locationDataPath, e);
        }
        catch (IOException e)
        {
            log.error("Could not read location coordinates from {}", locationDataPath, e);
        }

        this.coordinates = Collections.unmodifiableMap(loaded);
    }

    /**
     * Look up the WorldPoint for a location name. Case-insensitive.
     *
     * @param locationName the location string (e.g. "Slayer Tower")
     * @return the WorldPoint, or null if no coordinates are mapped
     */
    public WorldPoint getCoordinates(String locationName)
    {
        if (locationName == null)
        {
            return null;
        }
        return coordinates.get(locationName.toLowerCase());
    }

    /**
     * Returns an unmodifiable view of all location coordinates.
     */
    public Map<String, WorldPoint> getAll()
    {
        return coordinates;
    }

    /**
     * Inner class for JSON deserialization of coordinate entries.
     */
    private static class CoordEntry
    {
        int x;
        int y;
        int plane;
    }
}
