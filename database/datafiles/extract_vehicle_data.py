import json

vehicles_file = "./datafiles/points_original_files/electric_vehicles_data.json"
final_vehicles_file = "./datafiles/final_data/vehicles.json"

vehicles_in = open(vehicles_file, 'r')
out = open(final_vehicles_file, 'w')

tmp = json.load(vehicles_in)
data = tmp['data']
for v in data:
    vehicle = {}
    vehicle['_id'] = v['id']
    vehicle['brand'] = v['brand']
    vehicle['model'] = v['model']
    vehicle['variant'] = v['variant']
    vehicle['consumption'] = v['energy_consumption']['average_consumption']
    vehicle['batterySize'] = v['usable_battery_size']
    vehicle['ac'] = v['ac_charger']
    if vehicle['ac'] is not None:
        vehicle['ac'].pop('usable_phases', None)
        vehicle['ac'].pop('power_per_charging_point', None)
    vehicle['dc'] = v['dc_charger']
    if vehicle['dc'] is not None:
        vehicle['dc'].pop('charging_curve', None)
        vehicle['dc'].pop('is_default_charging_curve', None)
    string = json.dumps(vehicle)
    out.write(string+"\n")

vehicles_in.close()
out.close()
