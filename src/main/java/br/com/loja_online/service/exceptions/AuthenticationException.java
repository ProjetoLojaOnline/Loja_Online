package br.com.loja_online.service.exceptions;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class AuthenticationException extends UsernameNotFoundException {
public AuthenticationException(String message) {
    super(message);
}
}
