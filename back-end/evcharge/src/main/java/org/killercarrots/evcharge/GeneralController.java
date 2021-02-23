package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.Map;

import org.killercarrots.evcharge.models.ERole;
import org.killercarrots.evcharge.models.MessageResponse;
import org.killercarrots.evcharge.models.PointInfoResponse;
import org.killercarrots.evcharge.models.MyAbstractObj;
import org.killercarrots.evcharge.models.Role;
import org.killercarrots.evcharge.models.User;
import org.killercarrots.evcharge.models.Station;
import org.killercarrots.evcharge.models.Point;
import org.killercarrots.evcharge.models.Vehicle;
import org.killercarrots.evcharge.repos.RoleRepository;
import org.killercarrots.evcharge.repos.UserRepository;
import org.killercarrots.evcharge.repos.StationRepository;
import org.killercarrots.evcharge.repos.VehicleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


// Spring boot seems to not register both controllers
// so we set this one to RestController as a workaround
//@Controller
@RestController
@CrossOrigin(origins = "*")
public class GeneralController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    VehicleRepository vehicleRepository;

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
			// TODO : Try to drop sessions collection in db
			// *********NOT IMPLEMENTED YET*********
		} catch (Exception e){
			return buildResponse(new MessageResponse("failed", "status"), format);
		}

		return buildResponse(new MessageResponse("OK", "status"), format);
	}


  // Implementing use case 1: start charging event

  // User requests charging point info
  @GetMapping("/evcharge/api/SessionCost/{vehicleId}/{station_point}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> pointInfo(@RequestParam(value = "format", defaultValue = "json") String format,
	// vehicle, stationId and pointId are provided in url path
	@PathVariable(value = "vehicleId") String vehicleId,
  @PathVariable(value = "station_point") String station_point) {

    // get cost of chosen station
    String[] arrOfStr = station_point.split("_");
    Station station = stationRepository.findById(arrOfStr[0]).get();
    double cost = station.getCost();

    // get protocol of chosen point
    Point point = new Point();
    Set<Point> points = station.getPoints();
    for (Point p : points)
        if (Integer.toString(p.getLocalId()).equals(arrOfStr[1]))
          point = p;

    // check if vehicle supports this protocol and return message if not
    Vehicle vehicle = vehicleRepository.findById(vehicleId).get();
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
  /*@PostMapping(value="/evcharge/api/SessionCost/{vehicle}/{pointId}")
  @PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
  public ResponseEntity<String> userCharging(@RequestParam(value = "format", defaultValue = "json") String format,
	// vehicle and pointId are provided in url path
	@PathVariable(value = "vehicle") String vehicle,
	@PathVariable(value = "pointId") String point) {

    // what is the protocol field ???


  }*/

}
