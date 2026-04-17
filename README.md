# Uber Application
### ▶️ Watch the Full Video on YouTube: [Uber-App](https://www.youtube.com/watch?v=Cdx4DF9N8d8)
## YouTube Series: How Uber Works Under The Hood

---

## Services Overview

| Service | Port | Responsibility |
|---|---|---|
| location-service | 8082 | Tracks real-time driver locations via Redis Geospatial |
| ride-service | 8083 | Manages ride lifecycle, publishes events to Kafka |
| matching-service | 8084 | Consumes ride events, finds & assigns best driver |

---

## Architecture Flow

```
Driver Phone → Location Service → Redis (GEOADD)

Rider App → Ride Service → Kafka (ride.requested)
                                      ↓
                           Matching Service (consumer)
                                      ↓
                           Location Service (find nearby drivers)
                                      ↓
                           Matching Algorithm (score drivers)
                                      ↓
                           Kafka (ride.matched)
                                      ↓
                           Ride Service (update ride with driver)
```

---

## How To Run

### Step 1: Start Infrastructure
```bash
docker-compose up -d
```
This starts Redis, MySQL, Zookeeper, and Kafka.

Wait 30 seconds for Kafka to fully start before running services.

### Step 2: Start Location Service
```bash
cd location-service
mvn spring-boot:run
```

### Step 3: Start Ride Service
```bash
cd ride-service
mvn spring-boot:run
```

### Step 4: Start Matching Service
```bash
cd matching-service
mvn spring-boot:run
```

---

## Testing End-to-End Flow

### Step 1: Add Driver Locations (Location Service)
```
POST http://localhost:8082/api/v1/locations/drivers/update
{
    "driverId": "driver:1",
    "latitude": 12.9716,
    "longitude": 77.5946
}

POST http://localhost:8082/api/v1/locations/drivers/update
{
    "driverId": "driver:2",
    "latitude": 12.9800,
    "longitude": 77.5800
}

POST http://localhost:8082/api/v1/locations/drivers/update
{
    "driverId": "driver:3",
    "latitude": 12.9600,
    "longitude": 77.6100
}
```

### Step 2: Request a Ride (Ride Service)
```
POST http://localhost:8083/api/v1/rides/request
{
    "riderId": "rider:1",
    "pickupLatitude": 12.9716,
    "pickupLongitude": 77.5946,
    "pickupAddress": "MG Road, Bangalore",
    "dropLatitude": 12.9352,
    "dropLongitude": 77.6245,
    "dropAddress": "Koramangala, Bangalore"
}
```

### Step 3: Check Ride Status
```
GET http://localhost:8083/api/v1/rides/{rideId}
```
You will see driverId assigned and status = ACCEPTED

### Step 4: Start the Ride
```
PUT http://localhost:8083/api/v1/rides/{rideId}/start
```

### Step 5: Complete the Ride
```
PUT http://localhost:8083/api/v1/rides/{rideId}/complete
```

### Step 6: Get Rider History
```
GET http://localhost:8083/api/v1/rides/rider/rider:1
```

---

## Verify in Redis CLI
```bash
docker exec -it redis-geo redis-cli

# See all stored drivers
ZRANGE drivers:locations 0 -1

# Check specific driver position
GEOPOS drivers:locations "driver:1"

# Distance between two drivers
GEODIST drivers:locations "driver:1" "driver:2" km
```

---

## Key Concepts Covered
- Redis Geospatial (GEOADD, GEORADIUS)
- Kafka event-driven architecture (Producer/Consumer)
- Ride state machine (REQUESTED → MATCHING → ACCEPTED → STARTED → COMPLETED)
- Driver scoring algorithm (distance + rating weighted score)
- Service-to-service REST communication (Matching → Location Service)
- Docker Compose for infrastructure setup
