package es.us.dp1.l4_01_24_25.upstream.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import es.us.dp1.l4_01_24_25.upstream.configuration.jwt.AuthEntryPointJwt;
import es.us.dp1.l4_01_24_25.upstream.configuration.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    private AuthEntryPointJwt unauthorizedHandler;
    private String ADMIN = "ADMIN";

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.disable()))
            .exceptionHandling((exceptionHandling) -> exceptionHandling.authenticationEntryPoint(unauthorizedHandler))
            .authorizeHttpRequests(authorizeRequests -> authorizeRequests
            .requestMatchers(AntPathRequestMatcher.antMatcher("/")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/oups")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/resources/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/static/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/webjars/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui.html")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-resources/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/v3/api-docs/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/h2-console/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/auth/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/developers")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/plan")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/matches/dashboard")).permitAll()
            
            .requestMatchers(AntPathRequestMatcher.antMatcher("/ws-upstream/**")).permitAll()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/achievements/**")).authenticated()
            .requestMatchers(HttpMethod.GET, "/api/v1/users/{id}/**").authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/usersachievements/unlockrules/**")).authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/local/ws-upstream/**")).authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/matches/**")).authenticated()
            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/salmons")).authenticated()

            .requestMatchers(AntPathRequestMatcher.antMatcher("/api/v1/users/**")).hasAuthority(ADMIN)

            .anyRequest().authenticated()
        )
            .addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();
        configuration.setAllowedOrigins(java.util.Arrays.asList("http://localhost:3000", "https://localhost:8080"));
        configuration.setAllowedMethods(java.util.Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(java.util.Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(java.util.Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        org.springframework.web.cors.UrlBasedCorsConfigurationSource source = new org.springframework.web.cors.UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }   

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}