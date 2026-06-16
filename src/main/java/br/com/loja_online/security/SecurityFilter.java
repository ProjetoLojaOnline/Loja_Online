package br.com.loja_online.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.loja_online.exception.InvalidTokenException;
import br.com.loja_online.exception.StandardError;
import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private final TokenService tokenService;
    private final LoginRepository loginRepository;
    private final ObjectMapper objectMapper;

    public SecurityFilter(TokenService tokenService, LoginRepository loginRepository,
                          ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.loginRepository = loginRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(
            @org.springframework.lang.NonNull HttpServletRequest request,
            @org.springframework.lang.NonNull HttpServletResponse response,
            @org.springframework.lang.NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = recuperarToken(request);

        if (token != null) {
            try {
                String login = tokenService.getSubject(token);

                Login usuario = loginRepository.findByLogin(login)
                        .orElseThrow(() -> new InvalidTokenException("Usuário do token não encontrado"));

                var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(autenticacao);

            } catch (InvalidTokenException ex) {
                SecurityContextHolder.clearContext();
                writeErrorJson(response, request, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
                return;

            } catch (RuntimeException ex) {
                SecurityContextHolder.clearContext();
                log.error("Erro inesperado ao processar token de autenticação", ex);
                writeErrorJson(response, request, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Serviço temporariamente indisponível");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void writeErrorJson(HttpServletResponse response, HttpServletRequest request,
                                int status, String message) throws IOException {
        String error = (status == HttpServletResponse.SC_UNAUTHORIZED) ? "Unauthorized" : "Service Unavailable";
        StandardError body = new StandardError(System.currentTimeMillis(), status, error, message,
                request.getRequestURI());
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private String recuperarToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        return header.substring(7);
    }
}
