package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.killercarrots.evcharge.models.ERole;
import org.killercarrots.evcharge.models.MessageResponse;
import org.killercarrots.evcharge.models.MyAbstractObj;
import org.killercarrots.evcharge.models.Role;
import org.killercarrots.evcharge.models.User;
import org.killercarrots.evcharge.repos.RoleRepository;
import org.killercarrots.evcharge.repos.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.stereotype.Controller;
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
    BCryptPasswordEncoder encoder;
	
	// Creates the ResponseEntity for the desired response object with the correct format type
    public static ResponseEntity<String> buildResponse(MyAbstractObj obj, String format){//, String filename){
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
	@PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
	public String userAccess() {
		return "User Content.";
	}

	@GetMapping("/evcharge/mod")
	@PreAuthorize("hasRole('MODERATOR')")
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
			// So if you dont load them manually you can
			// initialize the collection through resetsessions call
			roleRepository.save(new Role(1, ERole.ROLE_USER));
			roleRepository.save(new Role(2, ERole.ROLE_MODERATOR));
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
			// Try to drop sessions collection in db
			// *********NOT IMPLEMENTED YET*********
		} catch (Exception e){
			return buildResponse(new MessageResponse("failed", "status"), format);//, "status.csv");
		}
		
		return buildResponse(new MessageResponse("OK", "status"), format);//, "status.csv");
	}
	
}