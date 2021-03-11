#!/bin/bash

echo "Creating datafiles..."

rm -r ./datafiles/final_data
mkdir ./datafiles/final_data

echo "Producing vehicle data"
python3 ./datafiles/extract_vehicle_data.py

echo "Producing charging station data"
python3 ./datafiles/extract_station_points.py

echo "Producing charging sessions"
head -100 ./datafiles/final_data/stations.json > ./datafiles/final_data/sample_stations.json
python3 ./datafiles/extract_sessions_tojson.py
rm ./datafiles/final_data/sample_stations.json
python3 ./datafiles/events_to_csv.py

echo "Done creating datafiles"