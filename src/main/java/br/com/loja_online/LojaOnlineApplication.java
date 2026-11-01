package br.com.loja_online;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LojaOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaOnlineApplication.class, args);
    }
}
