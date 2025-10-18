package br.com.loja_online.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
public class Endereco implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;
  private String logradouro;
  private Integer numero;
  private String bairro;
  private String complemento;
  private String referencia;
  private String cep;
  private String cidade;
  private String estado;

  public Endereco(String logradouro, Integer numero, String bairro, String complemento, String referencia, String cep, String cidade, String estado) {
    this.logradouro = logradouro;
    this.numero = numero;
    this.bairro = bairro;
    this.complemento = complemento;
    this.referencia = referencia;
    this.cep = cep;
    this.cidade = cidade;
    this.estado = estado;
  }
}
