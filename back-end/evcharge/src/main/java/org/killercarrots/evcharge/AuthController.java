package org.killercarrots.evcharge;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

// Imports from auth package
import org.killercarrots.evcharge.auth.*;

// Imports from models package
import org.killercarrots.evcharge.models.User;
import org.killercarrots.evcharge.models.Role;
import org.killercarrots.evcharge.models.MessageResponse;
import org.killercarrots.evcharge.models.PointInfoResponse;
import org.killercarrots.evcharge.models.ERole;
import org.killercarrots.evcharge.models.LoginRequest;

// Imports from repos package
import org.killercarrots.evcharge.repos.RoleRepository;
import org.killercarrots.evcharge.repos.UserRepository;

@Controller
// Enable CORS (client and server will be on the same host machine)
@CrossOrigin(origins = "*")
public class AuthController {
	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	AuthenticationManager authenticationManager;

    @Autowired
    BCryptPasswordEncoder encoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    ///////////////////////////////////////////////////////////
  @PostMapping(value = "/evcharge/api/login", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
	public ResponseEntity<?> authenticateUser(@RequestParam(value = "format", defaultValue = "json") String format, LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		// add active token to user in db
		User user = userRepository.findById(loginRequest.getUsername()).get();
		user.setToken(jwt);
		userRepository.save(user);
		Set<Role> roles = user.getRoles();
		String printRoles = "";
		for (Role r : roles) {
			if (r.getId() == 3)
				printRoles += "\"admin\",";
			else if (r.getId() == 2)
				printRoles += "\"operator\",";
			else if (r.getId() == 1)
				printRoles += "\"user\",";
		}
		printRoles = "[" + printRoles.substring(0, printRoles.length() - 1) + "]";
		HashMap<String, String> map = new HashMap<>();
		map.put("token", jwt);
		map.put("roles", printRoles);
		return GeneralController.buildResponse(new PointInfoResponse(map), format);
	}


	@PostMapping("/evcharge/api/logout")
	@PreAuthorize("hasRole('USER') or hasRole('OPERATOR') or hasRole('ADMIN')")
	public ResponseEntity<?> logoutUser(Authentication auth, @RequestParam(value = "format", defaultValue = "json") String format, HttpServletRequest request) {
		// Hence that authorization has already taken place
		// so user and token are valid
		// invalidate token in db by removing it
		String username = auth.getName();
		// delete token from user in dbs
		User user = userRepository.findById(username).get();
		user.setToken(null);
		userRepository.save(user);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = {"/evcharge/api/admin/usermod/{username}/{password}"})
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<String> registerUser(@RequestParam(value = "format", defaultValue = "json") String format,
	// requested roles found in parameter if given any
	@RequestParam(value = "roles", defaultValue = "[\"user\"]") List<String> reqRoles,
	// username and password are provided in url path in specifications
	// (dangerous way of passing password like that, but I obey the rules master ¯\_(ツ)_/¯ )
	@PathVariable(value = "username") String name,
	@PathVariable(value = "password") String pass){

		// Create new user's account
		// (if it exists in db it will be updated)
		User user = new User();
		user.setUsername(name);
		user.setPassword(encoder.encode(pass));
		// Get requested roles for new user
		Set<String> strRoles = new HashSet<String>(reqRoles);
		Set<Role> roles = new HashSet<>();

		if (strRoles.isEmpty()) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "operator":
					Role operatorRole = roleRepository.findByName(ERole.ROLE_OPERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(operatorRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		user.setToken(null);
		userRepository.save(user);

		return GeneralController.buildResponse(new MessageResponse("User registered successfully!", "message"), format);//, "registration.csv");
	}

}
