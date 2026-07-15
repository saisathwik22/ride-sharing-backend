"""
Seeds 500 driver locations scattered realistically across Hyderabad city.
Simulates the "500 drivers moving around your city RIGHT NOW" scenario
from the problem statement.

Usage:
    python seed_500_drivers.py

Requires: requests  (pip install requests)
"""

import random
import requests
import time
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

BASE_URL = "http://localhost:8082/api/v1/locations/drivers/update"

# Hyderabad city bounding box (roughly covers outer ring road to city core)
# lat: 17.20 (south, near Shamshabad) to 17.55 (north, near Bachupally)
# long: 78.30 (west, near Gachibowli) to 78.60 (east, near ECIL)
LAT_MIN, LAT_MAX = 17.20, 17.55
LON_MIN, LON_MAX = 78.30, 78.60

# Bias toward a few dense clusters (like real Uber demand hotspots)
# instead of pure uniform random, which would be unrealistic
HOTSPOTS = [
    (17.4400, 78.3489, 0.03),  # Hitech City / Gachibowli corridor
    (17.4374, 78.4487, 0.03),  # Ameerpet / central
    (17.4239, 78.4738, 0.03),  # Secunderabad
    (17.3616, 78.4747, 0.03),  # Old city / Charminar
    (17.4849, 78.4108, 0.03),  # Kukatpally
]

NUM_DRIVERS = 1000
CLUSTER_RATIO = 0.7  # 70% of drivers near hotspots, 30% scattered city-wide


def random_point():
    if random.random() < CLUSTER_RATIO:
        center_lat, center_lon, spread = random.choice(HOTSPOTS)
        lat = random.gauss(center_lat, spread)
        lon = random.gauss(center_lon, spread)
    else:
        lat = random.uniform(LAT_MIN, LAT_MAX)
        lon = random.uniform(LON_MIN, LON_MAX)

    # clamp to city bounds in case gaussian spread overshoots
    lat = max(LAT_MIN, min(LAT_MAX, lat))
    lon = max(LON_MIN, min(LON_MAX, lon))
    return round(lat, 6), round(lon, 6)


def seed_driver(i):
    lat, lon = random_point()
    rating = round(random.uniform(3.5, 5.0), 2)
    payload = {
        "driverId": f"driver:{i}",
        "latitude": lat,
        "longitude": lon,
        "rating": rating,
    }
    try:
        resp = requests.post(BASE_URL, json=payload, timeout=5)
        if resp.status_code == 200:
            return (True, None)
        else:
            return (False, f"driver:{i} -> HTTP {resp.status_code}: {resp.text}")
    except requests.exceptions.RequestException as e:
        return (False, f"driver:{i} -> ERROR: {e}")


def main():
    success = 0
    failed = 0
    completed = 0
    lock = threading.Lock()
    start = time.time()

    with ThreadPoolExecutor(max_workers=20) as executor:
        futures = [executor.submit(seed_driver, i) for i in range(1, NUM_DRIVERS + 1)]
        for future in as_completed(futures):
            ok, err = future.result()
            with lock:
                completed += 1
                if ok:
                    success += 1
                else:
                    failed += 1
                    print(f"  {err}")
                if completed % 100 == 0:
                    print(f"Progress: {completed}/{NUM_DRIVERS}")

    elapsed = time.time() - start
    print(f"\nDone in {elapsed:.2f}s")
    print(f"Success: {success}  Failed: {failed}")


if __name__ == "__main__":
    main()