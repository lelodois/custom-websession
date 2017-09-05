package br.com.customsession;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(scanBasePackages = "br.com.customsession")
@EnableWebMvc
public class CustomSessionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomSessionApplication.class, args);
    }

}
