package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

import org.killercarrots.evcharge.models.ERole;
import org.killercarrots.evcharge.models.MessageResponse;
import org.killercarrots.evcharge.models.PointInfoResponse;
import org.killercarrots.evcharge.models.MyAbstractObj;
import org.killercarrots.evcharge.models.Role;
import org.killercarrots.evcharge.models.User;
import org.killercarrots.evcharge.models.Station;
import org.killercarrots.evcharge.models.Point;
import org.killercarrots.evcharge.models.Vehicle;
import org.killercarrots.evcharge.models.ActiveSession;
import org.killercarrots.evcharge.repos.ChargeEventsRepository;
import org.killercarrots.evcharge.repos.RoleRepository;
import org.killercarrots.evcharge.repos.UserRepository;
import org.killercarrots.evcharge.repos.StationRepository;
import org.killercarrots.evcharge.repos.VehicleRepository;
import org.killercarrots.evcharge.repos.ActiveSessionRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import org.killercarrots.evcharge.errorHandling.*;
import org.killercarrots.evcharge.models.*;


// Spring boot seems to not register both controllers
// so we set this one to RestController as a workaround
//@Controller
@RestController
@CrossOrigin(origins = "*")
public class GeneralController {

  @Autowired
  public static SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  StationRepository stationRepository;

  @Autowired
  VehicleRepository vehicleRepository;

	@Autowired
	ChargeEventsRepository chargeEventsRepository;

  @Autowired
  ActiveSessionRepository activeSessionRepository;

  @Autowired
  BCryptPasswordEncoder encoder;

	// Creates the ResponseEntity for the desired response object with the correct format type
  public static ResponseEntity<String> buildResponse(MyAbstractObj obj, String format){
    String body = null;
    HttpHeaders headers = new HttpHeaders();
    switch(format){
      case "csv": {
        body = obj.toCsv();
        //headers.add("Content-Disposition", "attachment; filename=\""+filename+"\"");
        headers.setContentType(new MediaType("txt", "csv", Charset.forName("utf-8")));
        break;
      }
      default: {
        body = obj.toJson();
        headers.setContentType(MediaType.APPLICATION_JSON);
      }
    }
    return new ResponseEntity<String>(body ,headers, HttpStatus.OK);
  }


	public static String formatDate(String date) throws BadRequestException {
		if(date.length()!=8){
			throw new BadRequestException("Invalid date format");
		}
		String year = date.substring(0,4);
		String month = date.substring(4, 6);
		String day = date.substring(6);
		int y = Integer.parseInt(year);
		int m = Integer.parseInt(month);
		int d = Integer.parseInt(day);
		// would be overkill to start checking for 30 or 31 days
		// or leap years
		if(y > 0 && m > 0 && m < 13 && d > 0 && d < 32 ) {
			return year+'-'+month+'-'+day;
		}
		throw new BadRequestException("Invalid date given");
	}

	//just some demo endpoints to check
	// role based authorization
	@GetMapping("/evcharge/test")
	public String allAccess() {
		return "Public Content.";
	}

	@GetMapping("/evcharge/user")
	@PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
	public String userAccess() {
		return "User Content.";
	}

	@GetMapping("/evcharge/operator")
	@PreAuthorize("hasRole('OPERATOR')")
	public String moderatorAccess() {
		return "Moderator Board.";
	}

	@GetMapping("/evcharge/admin")
	@PreAuthorize("hasRole('ADMIN')")
	public String adminAccess() {
		return "Admin Board.";
	}

	@PostMapping(value="/evcharge/api/admin/resetsessions")
	public ResponseEntity<String> resetSessions(@RequestParam(value = "format", defaultValue = "json") String format) {
		try{
			// Make sure to intialize Roles collection too
			// So if you don't load them manually you can
			// initialize the collection through resetsessions call
			roleRepository.save(new Role(1, ERole.ROLE_USER));
			roleRepository.save(new Role(2, ERole.ROLE_OPERATOR));
			roleRepository.save(new Role(3, ERole.ROLE_ADMIN));

			// First reset admin user password and username
			Set<Role> roles = new HashSet<>();
			roles.add(roleRepository.findByName(ERole.ROLE_ADMIN).get());
			User adminUser = new User();
			adminUser.setUsername("admin");
			adminUser.setPassword(encoder.encode("petrol4ever"));
			adminUser.setToken(null);
			adminUser.setRoles(roles);
			userRepository.save(adminUser);
			// drop sessions collection in db
			chargeEventsRepository.deleteAll();
		} catch (Exception e){
			return buildResponse(new MessageResponse("failed", "status"), format);
		}

		return buildResponse(new MessageResponse("OK", "status"), format);
	}

  // Get sessions in a specified period of time for a given pointID
  @GetMapping(value="/evcharge/api/SessionsPerPoint/{pointId}/{fromDate}/{toDate}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> getSessionsPerPoint(@RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "pointId") String point,
	@PathVariable(value = "fromDate") String from,
	@PathVariable(value = "toDate") String to) throws NoDataException, BadRequestException {
		from = formatDate(from) + " 00:00:00";
		to = formatDate(to) + " 23:59:59";
		List<ChargeEvent> events = chargeEventsRepository.findByPointIdAndStartTimeBetweenOrderByStartTimeAsc(point, from, to);
		if(events.size() == 0){
			throw new NoDataException("No charging events found for given period and pointID: "+point);
		}
		// get station id and search for the operator
		String[] tmp = point.split("_");
		int suffixLen = tmp[tmp.length-1].length()+1;
		String stationId = point.substring(0,point.length()-suffixLen);
		// we let no point or session exist if it does not corrispond to a station so no need to check existence
		String operator = stationRepository.findById(stationId).get().getOperator();
		PointSessionsResponse body = new PointSessionsResponse(point, operator, from, to);
		body.buildList(events);
		return buildResponse(body, format);
	}

  // Implementing use case 1: start charging event

  // User requests charging point info
  @GetMapping("/evcharge/api/SessionCost/{vehicleId}/{station_point}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> pointInfo(@RequestParam(value = "format", defaultValue = "json") String format,
  // vehicleId and stationId_pointId are provided in url path
	@PathVariable(value = "vehicleId") String vehicleId,
  @PathVariable(value = "station_point") String station_point) throws BadRequestException {

    // get cost of chosen station
    String[] arrOfStr = station_point.split("_");
    Station station = stationRepository.findById(arrOfStr[0]).orElseThrow(() -> new BadRequestException("No such station"));
    station = stationRepository.findById(arrOfStr[0]).get();
    double cost = station.getCost();

    // get protocol of chosen point
    Point point = new Point();
    Set<Point> points = station.getPoints();
    for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(arrOfStr[1]))
          point = p;
    if (point.getLocalId() == 0) throw new BadRequestException("No such point in this station");

    // check if vehicle supports this protocol and return message if not
    Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new BadRequestException("No such vehicle"));
    vehicle = vehicleRepository.findById(vehicleId).get();
    if (point.getType().equals("ac")) {
      if (vehicle.getAc() == null || !vehicle.getAc().ports.contains(point.getPort()) || vehicle.getAc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);
      }
    else if (point.getType().equals("dc"))
      if (vehicle.getDc() == null || !vehicle.getDc().ports.contains(point.getPort()) || vehicle.getDc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);

    // return supported protocol
    String protocol;
    try {
      protocol = point.getPort() + "_" + point.getType() + "_" + Double.toString(point.getPower()) + "kW";
    }
    catch (Exception e) {
			return buildResponse(new MessageResponse("failed", "status"), format);
    }
    HashMap<String, String> fields_messages = new HashMap<String, String>();
    fields_messages.put("cost", Double.toString(cost));
    fields_messages.put("protocol", protocol);
    return buildResponse(new PointInfoResponse(fields_messages), format);
  }

  // Charging starts with cost given as goal
  @PostMapping(value="/evcharge/api/StartSessionCost/{vehicleId}/{station_point}/{cost}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> startChargingCost(Authentication auth,
  @RequestParam(value = "format", defaultValue = "json") String format,
	// vehicleId, stationID_pointId, protocol and cost are provided in url path
	@PathVariable(value = "vehicleId") String vehicleId,
	@PathVariable(value = "station_point") String station_point,
  @PathVariable(value = "cost") String cost) throws BadRequestException {

    String[] arrOfStr = station_point.split("_");
    Station station = stationRepository.findById(arrOfStr[0]).orElseThrow(() -> new BadRequestException("No such station"));
    station = stationRepository.findById(arrOfStr[0]).get();
    // get protocol of chosen point
    Point point = new Point();
    Set<Point> points = station.getPoints();
    for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(arrOfStr[1]))
          point = p;
    if (point.getLocalId() == 0) throw new BadRequestException("No such point in this station");

    // check if vehicle supports this protocol and return message if not
    Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new BadRequestException("No such vehicle"));
    vehicle = vehicleRepository.findById(vehicleId).get();
    if (point.getType().equals("ac")) {
      if (vehicle.getAc() == null || !vehicle.getAc().ports.contains(point.getPort()) || vehicle.getAc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);
      }
    else if (point.getType().equals("dc"))
      if (vehicle.getDc() == null || !vehicle.getDc().ports.contains(point.getPort()) || vehicle.getDc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);

    // supported protocol as string
    String protocol;
    protocol = point.getPort() + "_" + point.getType() + "_" + Double.toString(point.getPower()) + "kW";

    ActiveSession activeSession = new ActiveSession();
    // get system time, needed later
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    // There is no automatic way of setting unique id in mongodb with java and spring
    // Also no available tutorial was found without using maven dependencies, which we do not use
    // As a workaround, id is generated by concatenating stationId-pointId-startTime,
    // supposing it's not possible to start more than one session in the same second,
    // which in turn means that host system of API must never lose synchronization with universal time
    // In short, this is not a sufficient solution for a real system
    activeSession.setId(arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    activeSession.setStationId(arrOfStr[0]);
    activeSession.setPointId(arrOfStr[1]);
    activeSession.setVehicleId(vehicleId);
    activeSession.setOperatorId(station.getOperator());
    activeSession.setStartTime(dtf.format(now));
    // find user by token
    String username = auth.getName();
    activeSession.setUser(username);
    activeSession.setProtocol(protocol);
    double kWhRequested = Double.parseDouble(cost) / station.getCost();
    // get vehicle's battery size, if requested amount exceeds battery size adjust request to battery size amount
    String message = "";
    if (kWhRequested > vehicle.getBatterySize()) {
      kWhRequested = vehicle.getBatterySize();
      message = "Vehicle's battery size exceeded and request adjusted to maximum amount. ";
    }
    activeSession.setKWhRequested(kWhRequested);
    activeSession.setCostPerKWh(station.getCost());

    // add active session to database and return response
    try {
      activeSessionRepository.save(activeSession);
    }
    catch (Exception e) {
			return buildResponse(new MessageResponse("Failed to start session", "status"), format);
    }
    return buildResponse(new MessageResponse(message+"Charging session started succesfully!", "status"), format);
  }

  // Charging starts with amount given as goal
  @PostMapping(value="/evcharge/api/StartSessionAmount/{vehicleId}/{station_point}/{amount}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> startChargingAmount(Authentication auth,
  @RequestParam(value = "format", defaultValue = "json") String format,
	// vehicleId, stationID_pointId, protocol and cost are provided in url path
	@PathVariable(value = "vehicleId") String vehicleId,
	@PathVariable(value = "station_point") String station_point,
  @PathVariable(value = "amount") String amount) throws BadRequestException {

    String[] arrOfStr = station_point.split("_");
    Station station = stationRepository.findById(arrOfStr[0]).orElseThrow(() -> new BadRequestException("No such station"));
    station = stationRepository.findById(arrOfStr[0]).get();
    // get protocol of chosen point
    Point point = new Point();
    Set<Point> points = station.getPoints();
    for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(arrOfStr[1]))
          point = p;
    if (point.getLocalId() == 0) throw new BadRequestException("No such point in this station");

    // check if vehicle supports this protocol and return message if not
    Vehicle vehicle = vehicleRepository.findById(vehicleId).orElseThrow(() -> new BadRequestException("No such vehicle"));
    vehicle = vehicleRepository.findById(vehicleId).get();
    if (point.getType().equals("ac")) {
      if (vehicle.getAc() == null || !vehicle.getAc().ports.contains(point.getPort()) || vehicle.getAc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);
      }
    else if (point.getType().equals("dc"))
      if (vehicle.getDc() == null || !vehicle.getDc().ports.contains(point.getPort()) || vehicle.getDc().max_power < point.getPower())
        return buildResponse(new MessageResponse("Vehicle does not support this charging protocol", "Response"), format);

    // supported protocol as string
    String protocol;
    protocol = point.getPort() + "_" + point.getType() + "_" + Double.toString(point.getPower()) + "kW";

    ActiveSession activeSession = new ActiveSession();
    // get system time, needed later
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    // There is no automatic way of setting unique id in mongodb with java and spring
    // Also no available tutorial was found without using maven dependencies, which we do not use
    // As a workaround, id is generated by concatenating stationId-pointId-startTime,
    // supposing it's not possible to start more than one session in the same second,
    // which in turn means that host system of API must never lose synchronization with universal time
    // In short, this is not a sufficient solution for a real system
    activeSession.setId(arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    activeSession.setStationId(arrOfStr[0]);
    activeSession.setPointId(arrOfStr[1]);
    activeSession.setVehicleId(vehicleId);
    activeSession.setOperatorId(station.getOperator());
    activeSession.setStartTime(dtf.format(now));
    // find user by token
    String username = auth.getName();
    activeSession.setUser(username);
    activeSession.setProtocol(protocol);
    double kWhRequested = Double.parseDouble(amount);
    // get vehicle's battery size, if requested amount exceeds battery size adjust request to battery size amount
    String message = "";
    if (kWhRequested > vehicle.getBatterySize()) {
      kWhRequested = vehicle.getBatterySize();
      message = "Vehicle's battery size exceeded and request adjusted to maximum amount. ";
    }
    activeSession.setKWhRequested(kWhRequested);
    activeSession.setCostPerKWh(station.getCost());

    // add active session to database and return response
    try {
      activeSessionRepository.save(activeSession);
    }
    catch (Exception e) {
			return buildResponse(new MessageResponse("Failed to start session", "status"), format);
    }
    return buildResponse(new MessageResponse(message+"Charging session started succesfully!", "status"), format);
  }

}
