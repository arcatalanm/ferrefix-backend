package ferrefix.ordenes.config;

import static ferrefix.ordenes.exception.ApiSecurityError.error;
import ferrefix.ordenes.service.AuthClient;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {
    @Bean
    CorsConfigurationSource cors(@Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String origins) {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(origins.split(",")).map(String::strip).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowCredentials(true);
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }


    @Bean
    SecurityFilterChain security(HttpSecurity http, AuthClient authClient) throws Exception {
        return http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable).requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/api/v1/ordenes/carrito", "/api/v1/ordenes/carrito/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/pedidos").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/cotizaciones").permitAll()
                        .requestMatchers("/api/v1/ordenes/cotizaciones").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/pedidos").authenticated()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                        // Endpoints de pedidos (pasarela de pagos)
                        .requestMatchers("/api/v1/ordenes/pagos/webpay/retorno").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/pedidos/*").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/ordenes/pedidos/*/pago/webpay").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e.authenticationEntryPoint((q, r, x) -> error(r, 401))
                        .accessDeniedHandler((q, r, x) -> error(r, 403)))
                .addFilterBefore(new PerfilAuthenticationFilter(authClient), UsernamePasswordAuthenticationFilter.class).build();
    }
}
