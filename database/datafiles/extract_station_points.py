import json
from random import randint

points_file = "./datafiles/points_original_files/poi.json"
final_stations_file = "./datafiles/final_data/stations.json"

points_in = open(points_file, 'r')
out = open(final_stations_file, 'w')

# find all stations and get their chargers/points
station = {}
for line in points_in:
    point = json.loads(line)
    try:
        station['_id'] = point['_id']['$oid']
        station['operator'] = point['OperatorInfo']['Title'].replace('\n', '')
        station['cost'] = float(randint(3,10))/10
        skip = False
        for i in station.keys():
            if station[i] is None or station[i] == "" or station[i] == "null":
                skip = True
                break
        if skip:
            continue
        station['location'] = {}
        station['location']['address'] = point['AddressInfo']['AddressLine1'].replace('\n', '').replace('\r', ' ').replace('\"', '').replace('\t', '')
        station['location']['country'] = point['AddressInfo']['Country']['Title'].replace('\n', '').replace('\r', ' ').replace('\t', '')
        lat = point['AddressInfo']['Latitude']
        lon = point['AddressInfo']['Longitude']
        geo = {'type': "Point", 'coordinates': [lon, lat]}
        #station['location']['lat'] = point['AddressInfo']['Latitude']
        #station['location']['lon'] = point['AddressInfo']['Longitude']
        station['location']['geo'] = geo
        skip = False
        for i in station['location'].keys():
            if station['location'][i] is None or station['location'][i] == "" or station['location'][i] == "null":
                skip = True
                break
            if lat is None or lon is None:
                skip = True
        if skip:
            continue
        tmp_list = []
        # let's try to get the chargers/points info...
        for charger in point['Connections']:
            dict = {}
            dict['_id'] = charger['ID']
            dict['power'] = charger['PowerKW']
            type = charger['ConnectionTypeID']
            if type == 2:
                dict['type'] = "dc"
                dict['port'] = "chademo"
            elif type == 32 or type == 33:
                dict['type'] = "dc"
                dict['port'] = "ccs"
            elif type == 8:
                dict['type'] = "dc"
                dict['port'] = "tesla_ccs"
            elif type == 27:
                dict['type'] = "dc"
                dict['port'] = "tesla_suc"
            elif type == 1 or type == 29:
                dict['type'] = "ac"
                dict['port'] = "type1"
            elif type == 25 or type == 1036:
                dict['type'] = "ac"
                dict['port'] = "type2"
            else:
                continue
            skip = False
            for i in dict.keys():
                if dict[i] is None or dict[i] == '' or dict[i] == "null":
                    skip = True
                    break
            if skip:
                continue
            tmp_list.append(dict)
        if not tmp_list:
            continue
        else:
            station['points'] = tmp_list
            #print(station)
            string = json.dumps(station)
            out.write(string+"\n")
    except:
        continue
    #break

points_in.close()
out.close()
