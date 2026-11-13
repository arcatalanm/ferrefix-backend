package ferrefix.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/media/productos/**", "/media/maquinarias/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/media/productos/**", "/media/maquinarias/**").permitAll()
                        .requestMatchers("/api/v1/auth/**", "/api/v1/admin/usuarios",
                                "/api/v1/catalogo/**", "/catalogo/v3/api-docs/**",
                                "/api/v1/ordenes/**", "/ordenes/v3/api-docs/**",
                                "/api/v1/arriendos/**", "/arriendos/v3/api-docs/**",
                                "/auth/v3/api-docs/**", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        .anyRequest().denyAll())
                .build();
    }
}
