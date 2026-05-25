package br.com.loja_online.security;

import br.com.loja_online.model.Login;
import br.com.loja_online.repository.LoginRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final LoginRepository loginRepository;

    public SecurityFilter(TokenService tokenService, LoginRepository loginRepository) {
        this.tokenService = tokenService;
        this.loginRepository = loginRepository;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String token = recuperarToken(request);

            if (token != null) {
                String login = tokenService.getSubject(token);

                Login usuario = loginRepository.findByLogin(login)
                        .orElseThrow(() -> new RuntimeException("Usuario não encontrado"));

                var autenticacao = new UsernamePasswordAuthenticationToken(
                        usuario, null, usuario.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(autenticacao);
            }
        } catch (RuntimeException ex) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);

    }

    private String recuperarToken(HttpServletRequest request) {
        var header = request.getHeader("Authorization");
        if(header == null || !header.startsWith("Bearer ")) return null;
        return header.replace("Bearer ", "");
    }

}

