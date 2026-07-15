package com.rideshare.locationservice.service;

import com.rideshare.locationservice.dto.DriverLocationRequest;
import com.rideshare.locationservice.dto.NearByDriverResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationService {

    private final RedisTemplate<String, String> redisTemplate;

    //Redis key for all driver locations
    private static final String DRIVERS_GEO_KEY = "drivers:locations";

    // Redis hash field name for rating, stored under "driver:{id}:meta"
    private static final String RATING_FIELD = "rating";

    // Default rating for drivers with no rating set yet
    private static final double DEFAULT_RATING = 4.5;

    /**
     * Update driver location in Redis.
     * Called every 3 seconds by driver's phone
     * Maps to Redis GEOADD command
     * Rating is optional - only stored if provided (e.g. during seeding)
     */

    public void updateDriverLocation(DriverLocationRequest  driverLocationRequest){
        log.info("Updating location for driver: {}", driverLocationRequest.getDriverId());

        // IMPORTANT: longitude FIRST, latitude SECOND - GeoSpatial Standard
        Point driverPoint = new Point(
                driverLocationRequest.getLongitude(),
                driverLocationRequest.getLatitude()
        );

        redisTemplate.opsForGeo().add(
                DRIVERS_GEO_KEY,
                driverPoint,
                driverLocationRequest.getDriverId()
        );

        // Store rating only if explicitly provided - avoids overwriting
        // an existing rating on routine 3-second location pings
        if (driverLocationRequest.getRating() != null) {
            String metaKey = driverMetaKey(driverLocationRequest.getDriverId());
            redisTemplate.opsForHash().put(
                    metaKey,
                    RATING_FIELD,
                    String.valueOf(driverLocationRequest.getRating())
            );
        }

        log.info("Location updated for driver: {}", driverLocationRequest.getDriverId());
    }

    /**
     * Find nearby drivers within given radius.
     * Called by Matching Service on ride request.
     * Maps to Redis GEORADIUS command.
     * Now also attaches each driver's rating from Redis hash.
     */

    public List<NearByDriverResponse> findNearbyDrivers(
            double latitude, double longitude, double radiusInKm) {

        log.info("Finding drivers near lat: {} long: {} withing {}Km",
                latitude, longitude, radiusInKm);

        Circle searchArea = new Circle(
                new Point(longitude, latitude),
                new Distance(radiusInKm, Metrics.KILOMETERS)
        );

        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                redisTemplate.opsForGeo().radius(
                        DRIVERS_GEO_KEY,
                        searchArea,
                        RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                                .includeCoordinates()
                                .includeDistance()
                                .sortAscending()
                                .limit(10)
                );

        List<NearByDriverResponse> nearbyDrivers = new ArrayList<>();

        if(results != null){
            results.getContent().forEach(result -> {
                RedisGeoCommands.GeoLocation<String> location = result.getContent();
                double rating = getDriverRating(location.getName());
                nearbyDrivers.add(new NearByDriverResponse(
                        location.getName(),
                        location.getPoint().getY(),
                        location.getPoint().getX(),
                        result.getDistance().getValue(),
                        rating
                ));
            });
        }
        log.info("Found {} drivers nearby", nearbyDrivers.size());
        return nearbyDrivers;
    }

    /**
     * Fetch a driver's rating from Redis hash.
     * Falls back to a default if the driver has no rating stored yet.
     */
    private double getDriverRating(String driverId) {
        Object ratingObj = redisTemplate.opsForHash().get(driverMetaKey(driverId), RATING_FIELD);
        if (ratingObj == null) {
            return DEFAULT_RATING;
        }
        try {
            return Double.parseDouble(ratingObj.toString());
        } catch (NumberFormatException e) {
            log.warn("Invalid rating stored for driver {}, using default", driverId);
            return DEFAULT_RATING;
        }
    }

    private String driverMetaKey(String driverId) {
        return driverId + ":meta";
    }

    /**
     * Remove driver when they go offline
     * Maps to Redis ZREM command.
     */

    public void removeDriver(String driverId){
        log.info("Removing driver: {}", driverId);
        redisTemplate.opsForGeo().remove(DRIVERS_GEO_KEY, driverId);
    }
}