package com.awesomepizza.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disabilita CSRF per API stateless
            .authorizeHttpRequests(authorize -> authorize
                // Endpoint per i clienti (non autenticati)
                .requestMatchers(HttpMethod.POST, "/api/v1/orders").permitAll() // Creazione ordine
                .requestMatchers(HttpMethod.GET, "/api/v1/orders/{code}").permitAll() // Recupero ordine
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll() // Documentazione API
                // Endpoint per il pizzaiolo (autenticati con ruolo PIZZAIOLO)
                .requestMatchers("/api/v1/pizzaiolo/**").hasRole("PIZZAIOLO")
                // Tutte le altre richieste richiedono autenticazione
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults()); // Abilita l'autenticazione Basic

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails pizzaiolo = User.builder()
            .username("pizzaiolo")
            .password(passwordEncoder().encode("password")) // Codifica la password
            .roles("PIZZAIOLO")
            .build();
        return new InMemoryUserDetailsManager(pizzaiolo);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
