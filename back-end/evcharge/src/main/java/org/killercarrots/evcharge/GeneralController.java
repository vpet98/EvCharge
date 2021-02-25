package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import static java.time.temporal.ChronoUnit.HOURS;

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
import org.springframework.web.multipart.MultipartFile;
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
  public static SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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

  // Creates the ResponseEntity for the desired response object with the correct
  // format type
  public static ResponseEntity<String> buildResponse(MyAbstractObj obj, String format) {
    String body = null;
    HttpHeaders headers = new HttpHeaders();
    switch (format) {
      case "csv": {
        body = obj.toCsv();
        // headers.add("Content-Disposition", "attachment; filename=\""+filename+"\"");
        headers.setContentType(new MediaType("txt", "csv", Charset.forName("utf-8")));
        break;
      }
      default: {
        body = obj.toJson();
        headers.setContentType(MediaType.APPLICATION_JSON);
      }
    }
    return new ResponseEntity<String>(body, headers, HttpStatus.OK);
  }

  public static String formatDate(String date) throws BadRequestException {
    if (date.length() != 8) {
      throw new BadRequestException("Invalid date format");
    }
    String year = date.substring(0, 4);
    String month = date.substring(4, 6);
    String day = date.substring(6);
    int y = Integer.parseInt(year);
    int m = Integer.parseInt(month);
    int d = Integer.parseInt(day);
    // would be overkill to start checking for 30 or 31 days
    // or leap years
    if (y > 0 && m > 0 && m < 13 && d > 0 && d < 32) {
      return year + '-' + month + '-' + day;
    }
    throw new BadRequestException("Invalid date given");
  }

  public static boolean validateDateFormat(String input, String[] formats) {
    for (String formatString : formats) {
      try {
          new SimpleDateFormat(formatString).parse(input);
          return true;
      } catch (Exception e) {
          /* nothing */
      }
    }
    return false;
  }

  // just some demo endpoints to check
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

  @GetMapping(value="/evcharge/api/admin/users/{username}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> GetUserStatus(@RequestParam(value = "format", defaultValue = "json") String format,
  @PathVariable(value = "username") String username) throws NoDataException {
    User user = null;
    System.out.println("searching");
    try {
      user = userRepository.findById(username).get();
    } catch (Exception e) {
      throw new NoDataException("No registered user matching the given username: "+username);
    }
    return buildResponse(new UserStatusDetailsResponse(user), format);
  }

  // for an event in csv file to be valid we must provide the following information in the correct order
  // StationId_PointId,VehicleId,User,StartTimestamp,EndTimestamp,DeliveredKWh
  @PostMapping(value = "/evcharge/api/admin/system/sessionsupd")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> sessionsUploadFile(@RequestParam("file") MultipartFile file) throws BadRequestException, IOException {
    if (file.isEmpty()) {
      throw new BadRequestException("No file was provided by the user");
    }
    int uploaded = 0;
    int imported = 0;
    String line;
    InputStream is = file.getInputStream();
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    while((line = br.readLine()) != null) {
      try {
        uploaded++;
        //System.out.println(line);
        String[] tokens = line.split(",");
        String[] tmp = tokens[0].split("_");
        String stationId = tmp[0];
        int pointId = Integer.parseInt(tmp[1]);
        String vehicleId = tokens[1];
        String user = tokens[2];
        String start = tokens[3];
        String end = tokens[4];
        float kWh = Float.parseFloat(tokens[5]);
        // check date format and start < end
        if(!(validateDateFormat(start, new String[]{"yyyy-MM-dd hh:mm:ss"}) && validateDateFormat(end, new String[]{"yyyy-MM-dd hh:mm:ss"}) && end.compareTo(start)>0)){
          //System.out.println("skipping line");
          continue;
        }
        // retrieve point and vehicle to validate existence compatibility
        Vehicle v = vehicleRepository.findById(vehicleId).get();
        Station st = stationRepository.findById(stationId).get();
        // userRepository.findById(user).get();
        // find point in station
        Point p = null;
        String protocol = null;
        for(Point i : st.getPoints()) {
          if(i.getLocalId() == pointId) {
            p = i;
            break;
          }
        }
        // check compatibility
        if(v.getAc().getPorts().contains(p.getPort())) {
          protocol = p.getPort()+"_ac_"+String.valueOf(Math.max(p.getPower(), v.getAc().getMax_power()))+"kW";
        } else if(v.getDc().getPorts().contains(p.getPort())) {
          protocol = p.getPort()+"_dc_"+String.valueOf(Math.max(p.getPower(), v.getAc().getMax_power()))+"kW";
        } else {
          continue;
        }
        ChargeEvent event = new ChargeEvent();
        event.setEventId(stationId+"_"+String.valueOf(pointId)+"_"+start);
        event.setStationId(stationId);
        event.setPointId(stationId+"_"+String.valueOf(pointId));
        event.setOperator(st.getOperator());
        event.setVehicleId(vehicleId);
        event.setStartTime(start);
        event.setEndTime(end);
        event.setKWhDelivered(kWh);
        event.setCostPerKWh(st.getCost());
        event.setSessionCost(((double) Math.round(kWh*st.getCost()*100))/100);
        event.setProtocol(protocol);
        event.setUser(user);
        chargeEventsRepository.save(event);
        imported++;
      } catch(Exception e) {
        /* just skipping the line if found invalid format */
      }
    }
    long total = chargeEventsRepository.count();
    return buildResponse(new SessionsUploadResponse(uploaded, imported, total), "json");
  }

	@GetMapping(value="/evcharge/api/admin/healthcheck")
	public ResponseEntity<String> healthCheck() {
		try {
			// just a demo simple query to check connectivity
			userRepository.findById("admin");
		} catch (Exception e) {
			return buildResponse(new MessageResponse("failed", "status"), "json");
		}
		return buildResponse(new MessageResponse("OK", "status"), "json");
	}

	@PostMapping(value="/evcharge/api/admin/resetsessions")
	public ResponseEntity<String> resetSessions() {
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
			return buildResponse(new MessageResponse("failed", "status"), "json");
		}

		return buildResponse(new MessageResponse("OK", "status"), "json");
	}

  // Get sessions in a specified period of time for a given pointID
  @GetMapping(value="/evcharge/api/SessionsPerPoint/{pointId}/{fromDate}/{toDate}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
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
		PointSessionsResponse body = new PointSessionsResponse(point, events.get(0).getOperator(), from, to);
		body.buildList(events);
		return buildResponse(body, format);
	}

	// Get sessions in a specified period of time for a given stationID
	@GetMapping(value="/evcharge/api/SessionsPerStation/{stationId}/{fromDate}/{toDate}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
	public ResponseEntity<String> getSessionsPerStation(
	@RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "stationId") String station,
	@PathVariable(value = "fromDate") String from,
	@PathVariable(value = "toDate") String to) throws NoDataException, BadRequestException {
		from = formatDate(from) + " 00:00:00";
		to = formatDate(to) + " 23:59:59";
		List<ChargeEvent> events = chargeEventsRepository.findByStationIdAndStartTimeBetweenOrderByStartTimeAsc(station, from, to);
		if(events.size() == 0){
			throw new NoDataException("No charging events found for given period and stationID: "+station);
		}
		StationSessionsResponse body = new StationSessionsResponse(station, events.get(0).getOperator(), from, to);
		body.buildList(events);
		return buildResponse(body, format);
	}

	// Get sessions in a specified period of time for a given vehicleID
	@GetMapping(value="/evcharge/api/SessionsPerEV/{vehicleId}/{fromDate}/{toDate}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('USER') or hasRole('OPERATOR')")
	public ResponseEntity<String> getSessionsPerEV(
	@RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "vehicleId") String vehicle,
	@PathVariable(value = "fromDate") String from,
	@PathVariable(value = "toDate") String to) throws NoDataException, BadRequestException {
		from = formatDate(from) + " 00:00:00";
		to = formatDate(to) + " 23:59:59";
		List<ChargeEvent> events = chargeEventsRepository.findByVehicleIdAndStartTimeBetweenOrderByStartTimeAsc(vehicle, from, to);
		if(events.size() == 0){
			throw new NoDataException("No charging events found for given period and vehicleID: "+vehicle);
		}
		VehicleSessionsResponse body = new VehicleSessionsResponse(vehicle, from, to);
		body.buildList(events);
		return buildResponse(body, format);
	}

	// Get sessions in a specified period of time for a given provider
	@GetMapping(value="/evcharge/api/SessionsPerProvider/{providerId}/{fromDate}/{toDate}")
	@PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
	public ResponseEntity<String> getSessionsPerProvider(
	@RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "providerId") String operator,
	@PathVariable(value = "fromDate") String from,
	@PathVariable(value = "toDate") String to) throws NoDataException, BadRequestException {
		from = formatDate(from) + " 00:00:00";
		to = formatDate(to) + " 23:59:59";
		List<ChargeEvent> events = chargeEventsRepository.findByOperatorAndStartTimeBetweenOrderByStartTimeAsc(operator, from, to);
		if(events.size() == 0){
			throw new NoDataException("No charging events found for given period and providerID: "+operator);
		}
		OperatorSessionsResponse body = new OperatorSessionsResponse(operator, from, to);
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
    activeSession.setOperator(station.getOperator());
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
    HashMap<String, String> fields_messages = new HashMap<String, String>();
    fields_messages.put("session", arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    fields_messages.put("status", message+"Charging session started succesfully!");
    return buildResponse(new PointInfoResponse(fields_messages), format);
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
    activeSession.setOperator(station.getOperator());
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
    HashMap<String, String> fields_messages = new HashMap<String, String>();
    fields_messages.put("session", arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    fields_messages.put("status", message+"Charging session started succesfully!");
    return buildResponse(new PointInfoResponse(fields_messages), format);
  }

  // Implementing use case 2: complete charging event

  // User requests his/her active sessions
  @GetMapping("/evcharge/api/ActiveSession")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> userActiveSessions(Authentication auth,
  @RequestParam(value = "format", defaultValue = "json") String format) throws NoDataException {

    String username = auth.getName();
    HashSet<ActiveSession> sessions = activeSessionRepository.findByUser(username);
    if(sessions.size() == 0) {
      throw new NoDataException("No active sessions for user: "+username);
    }

    // collect all active sessions of user in a Hashmap(sessionId, currentCost)
    HashMap<String, Double> map = new HashMap<>();
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    for (ActiveSession s : sessions) {
      Station station = stationRepository.findById(s.getStationId());
      Point point = new Point();
      Set<Point> points = station.getPoints();
      for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(s.getPointId()))
          point = p;
      // calculate current chargine time
      LocalDateTime now = LocalDateTime.now();
      long diff = ChronoUnit.SECONDS.between(LocalDateTime.parse(s.getStartTime(), dtf), now);
      // calculate current cost
      double currentCost = (diff/60.0)*point.getPower()*s.getCostPerKWh();
      // put to map
      map.put(s.getId(), currentCost);
    }

    ActiveSessionsResponse response = new ActiveSessionsResponse(map);
    body.buildList(events);
    return buildResponse(body, format);
  }

}
