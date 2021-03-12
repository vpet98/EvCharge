import json

events_file = "./datafiles/final_data/events.json"
events_csv = "./datafiles/final_data/events.csv"

fp = open(events_file, 'r')
out = open(events_csv, 'w')
for line in fp:
    if line:
        try:
            event = json.loads(line)
            out.write(event['pointId']+","+event['vehicleId']+","+event['user']+","+event['startTime']+","+event['endTime']+","+str(event['kWhDelivered'])+"\n")
        except:
            continue
out.close()
fp.close()
