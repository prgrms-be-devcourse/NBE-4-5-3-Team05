package com.NBE_4_5_2.Team5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class Team5Application {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(Team5Application.class);
		// PID 파일을 기본 경로 (현재 디렉터리의 application.pid)로 저장
		app.addListeners(new ApplicationPidFileWriter());
		app.run(args);
	}

}
