package br.com.loja_online;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Desabilitado para evitar conexão real com o banco nos testes de contexto")
class LojaOnlineApplicationTests {

	@Test
	void contextLoads() {
	}

}
