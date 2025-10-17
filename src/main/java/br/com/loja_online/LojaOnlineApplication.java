package br.com.loja_online;

import br.com.loja_online.domain.login.Login;
import br.com.loja_online.repository.LoginRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LojaOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(LojaOnlineApplication.class, args);
    }

    // 👇 Esse método será executado automaticamente ao iniciar a aplicação
    @Bean
    CommandLineRunner seed(LoginRepository repo) {
        return args -> {
            String user = "mazur";
            if (!repo.existsByUsername(user)) {
                repo.save(new Login(user, "senhaSegura123"));
                System.out.println("✅ Login inserido!");
            } else {
                System.out.println("⚠️ Usuário já existe, não inserido novamente.");
            }
            System.out.println("Total de logins: " + repo.count());
        };
    }
}
