package ferrefix.catalogo.config;

import ferrefix.catalogo.service.AuthClient;
import static ferrefix.catalogo.exception.ApiSecurityError.error;
import java.util.List;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {
    @Bean org.springframework.web.cors.CorsConfigurationSource cors(
        @org.springframework.beans.factory.annotation.Value("${CORS_ALLOWED_ORIGINS:http://localhost:5173}") String origins) {
        var config=new org.springframework.web.cors.CorsConfiguration();
        config.setAllowedOrigins(java.util.Arrays.stream(origins.split(",")).map(String::strip).toList());
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type"));
        var source=new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",config);
        return source;
    }
    @Bean SecurityFilterChain security(HttpSecurity http,AuthClient auth) throws Exception {
        return http.cors(org.springframework.security.config.Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable).logout(AbstractHttpConfigurer::disable)
            .requestCache(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(HttpMethod.GET,"/api/v1/catalogo/productos","/api/v1/catalogo/productos/*","/api/v1/catalogo/ofertas").permitAll()
                .requestMatchers("/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html","/error").permitAll()
                .requestMatchers("/api/v1/catalogo/**").hasRole("ADMIN").anyRequest().denyAll())
            .exceptionHandling(e -> e.authenticationEntryPoint((q,r,x)->error(r,401))
                .accessDeniedHandler((q,r,x)->error(r,403)))
            .addFilterBefore(new PerfilAuthenticationFilter(auth),UsernamePasswordAuthenticationFilter.class).build();
    }
}
