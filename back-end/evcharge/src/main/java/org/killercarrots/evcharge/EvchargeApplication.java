package org.killercarrots.evcharge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
@ComponentScan(basePackageClasses = {AuthController.class, GeneralController.class})
public class EvchargeApplication {

	@Bean
	public BCryptPasswordEncoder bCryptPasswordEncoder() {
    	return new BCryptPasswordEncoder();
	}
	public static void main(String[] args) {
		SpringApplication.run(EvchargeApplication.class, args);
	}
	
}
