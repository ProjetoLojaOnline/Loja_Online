package br.com.loja_online.unit.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import br.com.loja_online.dto.validation.CnpjValidator;

class CnpjValidatorTest {

    private final CnpjValidator validator = new CnpjValidator();

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(
            strings = {
                "00.000.000/0001-91", // Banco do Brasil
                "00000000000191",
                "11.222.333/0001-81",
                "11222333000181",
                "12.ABC.345/01DE-35", // alfanumérico (exemplo oficial da Receita)
                "12ABC34501DE35",
                "12.abc.345/01de-35" // letras minúsculas são aceitas
            })
    @DisplayName("deveAceitarCnpjValido")
    void deveAceitarCnpjValido(String cnpj) {
        assertThat(CnpjValidator.isCnpjValid(cnpj)).isTrue();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(
            strings = {
                "00.000.000/0001-92", // dígito verificador errado
                "11222333000180",
                "12ABC34501DE36",
                "11.111.111/1111-11", // todos os dígitos iguais
                "00000000000000",
                "1122233300018", // 13 caracteres
                "112223330001811", // 15 caracteres
                "11.222.333.0001-81", // pontuação fora do padrão
                "12ABC34501DEAB", // dígitos verificadores precisam ser números
                ""
            })
    @DisplayName("deveRejeitarCnpjInvalido")
    void deveRejeitarCnpjInvalido(String cnpj) {
        assertThat(CnpjValidator.isCnpjValid(cnpj)).isFalse();
    }

    @Test
    @DisplayName("deveDeixarNullParaONotBlank")
    void deveDeixarNullParaONotBlank() {
        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(CnpjValidator.isCnpjValid(null)).isFalse();
    }

    @Test
    @DisplayName("deveNormalizarRemovendoPontuacaoEColocandoMaiusculo")
    void deveNormalizarRemovendoPontuacaoEColocandoMaiusculo() {
        assertThat(CnpjValidator.normalizar("12.abc.345/01de-35")).isEqualTo("12ABC34501DE35");
        assertThat(CnpjValidator.normalizar("00.000.000/0001-91")).isEqualTo("00000000000191");
    }
}
