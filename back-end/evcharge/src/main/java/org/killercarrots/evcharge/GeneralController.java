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
import java.util.ArrayList;
import java.util.Set;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.ChronoUnit;
import static java.time.temporal.ChronoUnit.SECONDS;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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

//K
import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.port;
import static spark.Spark.staticFiles;
//import com.google.gson.Gson;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

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

  // just a demo endpoint for checking
  @GetMapping("/evcharge/test")
  public String allAccess() {
    return "Public Content.";
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


  // Implementing use case 3: statistics

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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    // There is no automatic way of setting unique id in mongodb with java and spring
    // Also no available tutorial was found without using maven dependencies, which we do not use
    // As a workaround, id is generated by concatenating stationId-pointId-startTime,
    // supposing it's not possible to start more than one session in the same second,
    // which in turn means that host system of API must never lose synchronization with universal time
    // In short, this is not a sufficient solution for a real system
    activeSession.setId(arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    activeSession.setStationId(arrOfStr[0]);
    activeSession.setPointId(station_point);
    activeSession.setVehicleId(vehicleId);
    activeSession.setOperator(station.getOperator());
    activeSession.setStartTime(dtf.format(now));
    // find user by token
    String username = auth.getName();
    activeSession.setUser(username);
    activeSession.setProtocol(protocol);
    double kWhRequested = Double.parseDouble(cost) / station.getCost();
    DecimalFormat df = new DecimalFormat("0.00");

    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(dfs);

    df.setRoundingMode(RoundingMode.DOWN);
    kWhRequested = Double.parseDouble(df.format(kWhRequested));
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
    fields_messages.put("status", message+"Charging session started successfully!");
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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    activeSession.setId(arrOfStr[0]+"_"+arrOfStr[1]+"_"+dtf.format(now));
    activeSession.setStationId(arrOfStr[0]);
    activeSession.setPointId(station_point);
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
    fields_messages.put("status", message+"Charging session started successfully!");
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
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    for (ActiveSession s : sessions) {
      Station station = stationRepository.findById(s.getStationId()).get();
      Point point = new Point();
      Set<Point> points = station.getPoints();
      for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(s.getPointId().split("_")[1]))
          point = p;
      // calculate current chargine time
      LocalDateTime now = LocalDateTime.now();
      long diff = ChronoUnit.SECONDS.between(LocalDateTime.parse(s.getStartTime(), dtf), now);
      // calculate current cost
      double currentCost = (diff/3600.0)*point.getPower()*s.getCostPerKWh();
      // if requested amount reached, stop inreasing cost
      if (currentCost / station.getCost() > s.getKWhRequested())
        currentCost = station.getCost() * s.getKWhRequested();
      DecimalFormat df = new DecimalFormat("0.00");
      //K
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      dfs.setDecimalSeparator('.');
      df.setDecimalFormatSymbols(dfs);

      currentCost = Double.parseDouble(df.format(currentCost));
      // put to map
      map.put(s.getId(), currentCost);
    }

    return buildResponse(new ActiveSessionsResponse(map), format);
  }

  // complete charging process
  @PostMapping("/evcharge/api/CheckOut/{sessionId}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> completeCharging(Authentication auth,
  // below we need a string that the user is NEVER going to enter
  @RequestParam(value = "end", defaultValue = "electric vehicles suck, vescoukis-nickie not good professors (obvious lies)") String end,
  @RequestParam(value = "format", defaultValue = "json") String format,
  @PathVariable(value = "sessionId") String sessionId) throws BadRequestException {

    ActiveSession activeSession = activeSessionRepository.findById(sessionId)
        .orElseThrow(() -> new BadRequestException("No such active charging session"));
    ActiveSession session = activeSessionRepository.findById(sessionId).get();
    String username = auth.getName();
    if (!session.getUser().equals(username))
      throw new BadRequestException("Only the user who started this charging session can complete it");
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    LocalDateTime now = LocalDateTime.now();
    if (end.equals("electric vehicles suck, vescoukis-nickie not good professors (obvious lies)"))
      end = dtf.format(now);
    else {
      // check if date-time is valid
      try {
        TemporalAccessor temporal = dtf.parse(end);
      }
      catch (Exception e) {
        throw new BadRequestException("Invalid date-time format given");
      }
    }

    ChargeEvent chargeEvent = new ChargeEvent();
    chargeEvent.setEventId(sessionId);
    chargeEvent.setStationId(session.getStationId());
    chargeEvent.setPointId(session.getPointId());
    chargeEvent.setVehicleId(session.getVehicleId());
    chargeEvent.setOperator(session.getOperator());
    chargeEvent.setStartTime(session.getStartTime());
    chargeEvent.setEndTime(end);
    chargeEvent.setUser(session.getUser());
    chargeEvent.setProtocol(session.getProtocol());
    chargeEvent.setCostPerKWh(session.getCostPerKWh());
    // note that user may check out prematurely, so can't suppose kWhDelivered = kWhRequested
    long diff = ChronoUnit.SECONDS.between(LocalDateTime.parse(session.getStartTime(), dtf), LocalDateTime.parse(end, dtf));
    Station station = stationRepository.findById(session.getStationId()).get();
    Point point = new Point();
    Set<Point> points = station.getPoints();
    for (Point p : points)
      if (Integer.toString(p.getLocalId()).equals(session.getPointId().split("_")[1]))
        point = p;
    double kWhDelivered = (diff / 3600.0) * point.getPower();
    if (kWhDelivered > vehicleRepository.findById(session.getVehicleId()).get().getBatterySize())
      kWhDelivered = vehicleRepository.findById(session.getVehicleId()).get().getBatterySize();
    double sessionCost = kWhDelivered * session.getCostPerKWh();
    DecimalFormat df = new DecimalFormat("0.00");
    //K
    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
    dfs.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(dfs);

    kWhDelivered = Double.parseDouble(df.format(kWhDelivered));
    sessionCost = Double.parseDouble(df.format(sessionCost));
    chargeEvent.setKWhDelivered(kWhDelivered);
    chargeEvent.setSessionCost(sessionCost);
    // add charge event to database and return response
    try {
      activeSessionRepository.delete(activeSession);
      chargeEventsRepository.save(chargeEvent);
    }
    catch (Exception e) {
      return buildResponse(new MessageResponse("Failed to close charging session", "status"), format);
    }

    return buildResponse(new MessageResponse(
        "Charging completed successfully! Total cost is "+Double.toString(sessionCost)+"€", "status"), format);
  }


  // Implementing use case 4: search for nearby station
  @GetMapping(value="/evcharge/api/StationsNearby/{lat}/{lon}/{radius}")
  public ResponseEntity<String> SearchStationsNearby(
  @RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "lon") double lon,
  @PathVariable(value = "lat") double lat,
  @PathVariable(value = "radius") int radius) throws BadRequestException, NoDataException {
    if(lat > 90 || lat < -90 || lon > 180 || lon < -180) {
      throw new BadRequestException("Invalid coordinates");
    }
    List<Station> ls = stationRepository.nearByStations(lon, lat, radius);
    if(ls.isEmpty()){
      throw new NoDataException("No charging stations were found near the given location");
    }
    // System.out.println(ls.size());
    NearbyStationsResponse body = new NearbyStationsResponse(lat, lon, radius);
    body.buildList(ls);
    return buildResponse(body, format);
  }


  // Implementing use case 5: management of stations by operators

  // show stations of operator
  @GetMapping(value="/evcharge/api/Operator/StationShow/{operator}")
  @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> showStations(
  @RequestParam(value = "format", defaultValue = "json") String format,
	@PathVariable(value = "operator") String operator) throws BadRequestException, NoDataException {

    User user = userRepository.findByUsername(operator)
        .orElseThrow(() -> new BadRequestException("No such user"));
    user = userRepository.findByUsername(operator).get();
    Set<Role> roles = user.getRoles();
    HashSet<Integer> rids = new HashSet<>();
    for (Role r : roles)
      rids.add(r.getId());
    if (!rids.contains(2))
      throw new BadRequestException("User is not an operator");
    HashSet<Station> stations = stationRepository.findByOperator(operator);
    if(stations.size() == 0) {
      throw new NoDataException("No available stations for operator: "+operator);
    }

    return buildResponse(new OperatorStationsResponse(stations), format);

  }

  // add new station
  @PostMapping(value="/evcharge/api/Operator/StationAdd")
  @PreAuthorize("hasRole('OPERATOR')")
  public ResponseEntity<String> addStation(Authentication auth,
  @RequestParam(value = "format", defaultValue = "json") String format,
  @RequestParam(value = "id", defaultValue = "") String id,
  @RequestParam(value = "cost", defaultValue = "") String costString,
  @RequestParam(value = "address", defaultValue = "") String address,
  @RequestParam(value = "country", defaultValue = "") String country,
  @RequestParam(value = "lon", defaultValue = "") String lonString,
  @RequestParam(value = "lat", defaultValue = "") String latString,
  @RequestParam(value = "points", defaultValue = "[]") List<String> pointsString) throws BadRequestException {
  // list of pointsString is like: ["404_7.0_ac_type2", "101396_7.0_ac_type2"]
  // so in postman value is: 404_7.0_ac_type2, 101396_7.0_ac_type2

    // check validity of given parameters
    if (id.equals(""))
      throw new BadRequestException("Non-empty ID field of station has to be given");
    double cost;
    try {
      cost = Double.valueOf(costString);
    }
    catch (Exception e) {
      throw new BadRequestException("Invalid cost value given");
    }
    if (address.equals(""))
      throw new BadRequestException("Non-empty address field of station has to be given");
    if (country.equals(""))
      throw new BadRequestException("Non-empty country field of station has to be given");
    float lon, lat;
    try {
      lon = Float.valueOf(lonString);
      lat = Float.valueOf(latString);
    }
    catch (Exception e) {
      throw new BadRequestException("Invalid coordinates given");
    }
    if(lat > 90 || lat < -90 || lon > 180 || lon < -180)
      throw new BadRequestException("Invalid coordinates given");
    if (pointsString.isEmpty())
      throw new BadRequestException("Non-empty list of points for the station has to be given");

    // check if station ID already exists
    if (stationRepository.findById(id).isPresent())
      return buildResponse(new MessageResponse("Station with same ID already exists. If you want to update it, remove it first.",
                                               "status"), format);
    // create new station
    Station station = new Station();
    station.setId(id);
    station.setOperator(auth.getName());
    station.setCost(cost);
    SpatialCoordinates coords = new SpatialCoordinates();
    coords.setType("Point");
    coords.setCoordinates(new float[]{lon, lat});
    Location location = new Location();
    location.setAddress(address);
    location.setCountry(country);
    location.setGeo(coords);
    station.setLocation(location);
    Set<Point> points = new HashSet<Point>();
    for (String s : pointsString) {
      String[] arrOfStr = s.split("_");
      if (arrOfStr.length != 4)
        throw new BadRequestException("All points have to be given in form <id>_<power>_<currentType>_<port>");
      Point p = new Point();
      int localId;
      try {
        localId = Integer.valueOf(arrOfStr[0]);
      }
      catch (Exception e) {
        throw new BadRequestException("Invalid ID given at one of the station's points");
      }
      double power;
      try {
        power = Double.valueOf(arrOfStr[1]);
      }
      catch (Exception e) {
        throw new BadRequestException("Invalid power value given at one of the station's points");
      }
      p.setLocalId(localId);
      p.setPower(power);
      p.setType(arrOfStr[2]);
      p.setPort(arrOfStr[3]);
      points.add(p);
    }
    station.setPoints(points);
    try {
      stationRepository.save(station);
    }
    catch (Exception e) {
      return buildResponse(new MessageResponse("Failed to add station", "status"), format);
    }

    return buildResponse(new MessageResponse("Station added successfully!", "status"), format);
  }

  // delete station
  @PostMapping(value="/evcharge/api/Operator/StationRemove/{stationId}")
  @PreAuthorize("hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> removeStation(Authentication auth,
  @RequestParam(value = "format", defaultValue = "json") String format,
  @PathVariable(value = "stationId") String stationId) throws BadRequestException {

    Station station = stationRepository.findById(stationId).orElseThrow(() -> new BadRequestException("No such station"));
    Station stationGet = stationRepository.findById(stationId).get();
    // admin can delete every station, but an operator only stations he/she owns
    User user = userRepository.findByUsername(auth.getName()).get();
    Set<Role> roles = user.getRoles();
    HashSet<Integer> rids = new HashSet<>();
    for (Role r : roles)
      rids.add(r.getId());
    if (!rids.contains(3))
      if (!auth.getName().equals(stationGet.getOperator()))
        return buildResponse(new MessageResponse("You cannot delete station of other operator", "status"), format);
    // delete station
    try {
      stationRepository.delete(stationGet);
    }
    catch (Exception e) {
      return buildResponse(new MessageResponse("Failed to delete station", "status"), format);
    }
    return buildResponse(new MessageResponse("Station deleted successfully!", "status"), format);
  }


  // vehicles per user (found through charging events)
  @GetMapping("/evcharge/api/evPerUser/{username}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> userVehicles(
  @PathVariable(value = "username") String username,
  @RequestParam(value = "format", defaultValue = "json") String format) throws BadRequestException, NoDataException {

    User user = userRepository.findById(username).orElseThrow(() -> new BadRequestException("No such user"));
    List<ChargeEvent> userEvents = chargeEventsRepository.findByUser(username);
    if (userEvents.isEmpty())
      throw new NoDataException("This user has no vehicles");
    List<Vehicle> vehicles = new ArrayList<Vehicle>();
    List<String> vehiclesIds = new ArrayList<String>();
    for (ChargeEvent ce : userEvents)
      if (!vehiclesIds.contains(ce.getVehicleId())) {
        vehicles.add(vehicleRepository.findById(ce.getVehicleId()).get());
        vehiclesIds.add(ce.getVehicleId());
      }
    return buildResponse(new UserVehiclesResponse(vehicles), format);
  }

}
