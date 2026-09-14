package com.javalab.management;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login.html", "/login", "/app.css", "/app.js", "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                    .formLogin(form -> form.loginPage("/login.html").loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true).failureUrl("/login.html?error").permitAll())
                .logout(logout -> logout.logoutUrl("/logout")
                        .logoutSuccessUrl("/login.html?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID"));
        return http.build();
    }

    @Bean
    InMemoryUserDetailsManager users() {
        return new InMemoryUserDetailsManager(
                User.withUsername("manager01").password("{noop}123456").roles("MANAGER").build(),
                User.withUsername("admin01").password("{noop}123456").roles("ADMIN").build(),
                User.withUsername("warehouse01").password("{noop}123456").roles("WAREHOUSE").build(),
                User.withUsername("customer01").password("{noop}123456").roles("CUSTOMER").build());
    }
}
