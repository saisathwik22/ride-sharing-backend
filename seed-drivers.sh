#!/bin/bash
BASE_URL="http://localhost:8082/api/v1/locations/drivers/update"

drivers=(
  '{"driverId":"driver:1","latitude":17.4400,"longitude":78.3489}'   # Hitech City
  '{"driverId":"driver:2","latitude":17.4435,"longitude":78.3772}'   # Gachibowli
  '{"driverId":"driver:3","latitude":17.4483,"longitude":78.3915}'   # Kondapur
  '{"driverId":"driver:4","latitude":17.4483,"longitude":78.3915}'   # Madhapur
  '{"driverId":"driver:5","latitude":17.4325,"longitude":78.4071}'   # Jubilee Hills
  '{"driverId":"driver:6","latitude":17.4256,"longitude":78.4076}'   # Banjara Hills
  '{"driverId":"driver:7","latitude":17.4374,"longitude":78.4487}'   # Ameerpet
  '{"driverId":"driver:8","latitude":17.4254,"longitude":78.4517}'   # Punjagutta
  '{"driverId":"driver:9","latitude":17.4239,"longitude":78.4738}'   # Secunderabad
  '{"driverId":"driver:10","latitude":17.4400,"longitude":78.4650}'  # Begumpet
  '{"driverId":"driver:11","latitude":17.3850,"longitude":78.4867}'  # Abids
  '{"driverId":"driver:12","latitude":17.4062,"longitude":78.4691}'  # Nampally
  '{"driverId":"driver:13","latitude":17.3616,"longitude":78.4747}'  # Charminar
  '{"driverId":"driver:14","latitude":17.3687,"longitude":78.5247}'  # Dilsukhnagar
  '{"driverId":"driver:15","latitude":17.3455,"longitude":78.5518}'  # LB Nagar
  '{"driverId":"driver:16","latitude":17.4058,"longitude":78.5590}'  # Uppal
  '{"driverId":"driver:17","latitude":17.4948,"longitude":78.3996}'  # Kompally
  '{"driverId":"driver:18","latitude":17.4849,"longitude":78.4108}'  # Kukatpally
  '{"driverId":"driver:19","latitude":17.4966,"longitude":78.3648}'  # Miyapur
  '{"driverId":"driver:20","latitude":17.5245,"longitude":78.3736}'  # Bachupally
  '{"driverId":"driver:21","latitude":17.4922,"longitude":78.5062}'  # Alwal
  '{"driverId":"driver:22","latitude":17.4522,"longitude":78.5265}'  # Malkajgiri
  '{"driverId":"driver:23","latitude":17.4653,"longitude":78.5652}'  # ECIL
  '{"driverId":"driver:24","latitude":17.3336,"longitude":78.5645}'  # Vanasthalipuram
  '{"driverId":"driver:25","latitude":17.3607,"longitude":78.4265}'  # Attapur
  '{"driverId":"driver:26","latitude":17.3941,"longitude":78.4394}'  # Mehdipatnam
  '{"driverId":"driver:27","latitude":17.3936,"longitude":78.4104}'  # Tolichowki
  '{"driverId":"driver:28","latitude":17.4064,"longitude":78.3907}'  # Manikonda
  '{"driverId":"driver:29","latitude":17.2403,"longitude":78.4294}'  # Shamshabad (isolated, for "no driver nearby" test)
  '{"driverId":"driver:30","latitude":17.3286,"longitude":78.4131}'  # Rajendranagar
)

for d in "${drivers[@]}"; do
  curl -s -X POST "$BASE_URL" -H "Content-Type: application/json" -d "$d"
  echo ""
  sleep 0.2
done