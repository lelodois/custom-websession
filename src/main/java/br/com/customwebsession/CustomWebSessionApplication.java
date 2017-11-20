package br.com.customwebsession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(scanBasePackages = "br.com.customwebsession")
@EnableWebMvc
public class CustomWebSessionApplication {

	public static void main(String[] args) {
		SpringApplication.run(CustomWebSessionApplication.class, args);
	}

}
