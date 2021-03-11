import json
import datetime
from random import shuffle, choice

def formatTimestamp(t):
    result = datetime.datetime.strptime(t, "%a, %d %b %Y %H:%M:%S GMT")
    return result


vehicles_file = "./datafiles/final_data/vehicles.json"
stations_file = "./datafiles/final_data/sample_stations.json"
sessions_file1 = "./datafiles/points_original_files/caltech_acndata_sessions_33month.json"
sessions_file2 = "./datafiles/points_original_files/jpl_acndata_sessions_46month.json"
names_file = "./datafiles/points_original_files/names.csv"

sessions_out_file = "./datafiles/final_data/events.json"

names_in = open(names_file, 'r')
names = []
for line in names_in:
    tokens = line.split(",")
    names.append(tokens[0])
names_in.close()
shuffle(names)
# let's just keep only 1000 names eventually
names = names[:1000]


vehicles = {}
ports = {'chademo': [], 'ccs': [], 'tesla_ccs': [], 'tesla_suc': [], 'type1': [],
         'type2': []}  # keeps vehicle compatibility per port type
points = {}  # keeps all charging points
point_ids = []
# first get all vehicles
vehicles_in = open(vehicles_file, 'r')
for line in vehicles_in:
    v = json.loads(line)
    vehicles[v['_id']] = v
    if v['ac'] is not None:
        for p in v['ac']['ports']:
            ports[p].append(v['_id'])
    if v['dc'] is not None:
        for p in v['dc']['ports']:
            ports[p].append(v['_id'])
vehicles_in.close()
# get all charging points
stations_in = open(stations_file, 'r')
for line in stations_in:
    st = json.loads(line)
    for p in st['points']:
        # need to extract uuid, pointId, stationId and port for every point
        pointUUID = st['_id'] + '_' + str(p['_id'])
        tmp = {'UUID': pointUUID, 'stationsId': st['_id'], 'pointId': p['_id'], 'type': p['type'], 'port': p['port'],
               'cost': st['cost'], 'power': p['power'], 'operator': st['operator']}
        points[pointUUID] = tmp
stations_in.close()
point_ids = list(points.keys())

out = open(sessions_out_file, 'w')
for sessions_file in [sessions_file1, sessions_file2]:
    # for every event that we read pick a random charging point to assign to it
    # then according to the point port pick a random vehicle id for that event along with a random username
    # also pick the power/protocol used for the charging session
    sessions_in = open(sessions_file, 'r')
    tmp = json.load(sessions_in)
    sessions_in.close()
    data = tmp['_items']
    for i in data:
        # extract timestamps in correct format
        startTime = str(formatTimestamp(i['connectionTime']))
        endTime = str(formatTimestamp(i['disconnectTime']))
        delivered = i['kWhDelivered']
        p_id = choice(point_ids)
        op = points[p_id]['operator']
        session_id = p_id+'_'+startTime
        # get random but compatible vehicle id
        vehicle_id = choice(ports[points[p_id]['port']])
        costPerKWh = points[p_id]['cost']
        if points[p_id]['port'] == "type1" or points[p_id]['port'] == "type2":
            # if ac pick highest protocol
            protocol = points[p_id]['port']+"_ac_"+str(min(vehicles[vehicle_id]['ac']['max_power'], points[p_id]['power']))+"kW"
        else:
            # if dc pick highest power
            protocol = points[p_id]['port']+"_dc_"+str(min(vehicles[vehicle_id]['dc']['max_power'], points[p_id]['power']))+"kW"
        session = {'_id': session_id, 'stationId': p_id.split("_")[0], 'pointId': p_id, 'operator': op, 'vehicleId': vehicle_id,
                   'startTime': startTime, 'endTime': endTime, 'kWhDelivered': delivered, 'costPerKWh': costPerKWh,
                   'sessionCost': round(delivered*costPerKWh, 2), 'protocol': protocol, 'user': choice(names)}
        string = json.dumps(session)
        out.write(string+"\n")
        #print(session)
        #print(vehicle_id)
        #print(vehicles[vehicle_id]['ac'])
        #print(vehicles[vehicle_id]['dc'])
        #break
out.close()
# print(points)
# for k in ports.keys():
#    print((k,ports[k]))
