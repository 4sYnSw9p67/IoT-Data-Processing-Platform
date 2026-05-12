package iot.platform.security.web;

import iot.platform.security.config.SecurityProperties;
import iot.platform.security.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    public static final String INGEST_ROLE = "ROLE_INGEST";
    private static final String INGEST_PATH_PREFIX = "/api/ingest";

    private final ApiKeyService apiKeyService;
    private final SecurityProperties securityProperties;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(INGEST_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String presented = request.getHeader(securityProperties.getApiKey().getHeaderName());
        apiKeyService.authenticate(presented).ifPresentOrElse(apiKey -> {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    apiKey.getOwnerUserId(),
                    null,
                    List.of(new SimpleGrantedAuthority(INGEST_ROLE)));
            SecurityContextHolder.getContext().setAuthentication(auth);
            log.debug("API key {} authenticated for user {}", apiKey.getId(), apiKey.getOwnerUserId());
        }, () -> log.debug("API key auth failed for path {}", request.getRequestURI()));
        chain.doFilter(request, response);
    }
}
