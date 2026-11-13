package ferrefix.arriendos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableScheduling
public class ArriendosApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArriendosApplication.class, args);
	}

}