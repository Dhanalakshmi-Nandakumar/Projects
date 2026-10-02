package com.app.secureContactForm;

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
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/contacts/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET,"/contacts").hasAnyRole("user","admin")
                        .requestMatchers(HttpMethod.POST,"/contacts").hasRole("admin")
                        .requestMatchers(HttpMethod.DELETE,"/contacts/**").hasRole("admin")
                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService()
    {
        UserDetails user1= User.withUsername("user1")
                .password(passwordEncoder().encode("1234"))
                .roles("user")
                .build();
        UserDetails admin= User.withUsername("admin")
                .password(passwordEncoder().encode("1234"))
                .roles("admin")
                .build();
        return new InMemoryUserDetailsManager(user1,admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
