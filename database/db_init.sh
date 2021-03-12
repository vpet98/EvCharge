#!/bin/bash

echo "Creating database..."
mongo --quiet --eval '  db = db.getSiblingDB("evcharge")
                        db.dropDatabase();
                        db.createCollection("vehicles");
                        db.createCollection("users");
                        db.createCollection("stations");
                        db.createCollection("chargeEvents");
                        db.createCollection("roles");
                        db.createCollection("activeSessions");
                        db.roles.insertMany([{"_id": 1,"name": "ROLE_USER"},
                                                {"_id": 2,"name": "ROLE_OPERATOR"},
                                                {"_id": 3,"name": "ROLE_ADMIN"}
                                                ]);
                        db.stations.createIndex( { "location.geo" : "2dsphere" } );
                        '
echo "Loading data into database..."
mongoimport --db evcharge --collection stations --file ./datafiles/final_data/stations.json
mongoimport --db evcharge --collection vehicles --file ./datafiles/final_data/vehicles.json

echo "Database is ready!" 