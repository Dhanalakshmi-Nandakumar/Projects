package com.ecommerce.project.security;

import com.ecommerce.project.model.AppRole;
import com.ecommerce.project.model.Role;
import com.ecommerce.project.model.User;
import com.ecommerce.project.repositories.RoleRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.security.jwt.AuthEntryPointJwt;
import com.ecommerce.project.security.jwt.AuthTokenFilter;
import com.ecommerce.project.security.services.UserDetailsImpl;
import com.ecommerce.project.security.services.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.querydsl.binding.OptionalValueBinding;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {
    @Autowired
    UserDetailsServiceImpl userDetailsServiceImpl;
    @Autowired
    private AuthEntryPointJwt unAuthorizedHandler;
    @Autowired
    private AuthTokenFilter authTokenFilter;

    @Bean
    public CommandLineRunner initData(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            Role userRole = roleRepository
                    .findByRoleName(AppRole.ROLE_USER)
                    .orElseGet(() -> {
                        Role userRole1 = new Role(AppRole.ROLE_USER);
                        return roleRepository.save(userRole1);
                    });

            Role sellerRole = roleRepository
                    .findByRoleName(AppRole.ROLE_SELLER)
                    .orElseGet(() -> {
                        Role sellerRole1 = new Role(AppRole.ROLE_SELLER);
                        return roleRepository.save(sellerRole1);
                    });

            Role adminRole = roleRepository
                    .findByRoleName(AppRole.ROLE_ADMIN)
                    .orElseGet(() -> {
                        Role adminRole1 = new Role(AppRole.ROLE_ADMIN);
                        return roleRepository.save(adminRole1);
                    });

            Set<Role> userRoles = Set.of(userRole);
            Set<Role> sellerRoles = Set.of(sellerRole);
            Set<Role> adminRoles = Set.of(adminRole);

            // Create user
            if (!userRepository.existsByUserName("user1")) {
                User user1 = new User(
                        passwordEncoder.encode("password1"),
                        "user1@gmail.com",
                        "user1"
                );

                user1.setRoles(userRoles);
                userRepository.save(user1);
            }

            // Create seller
            if (!userRepository.existsByUserName("seller1")) {
                User seller1 = new User(
                        passwordEncoder.encode("password2"),
                        "user2@gmail.com",
                        "seller1"
                );

                seller1.setRoles(sellerRoles);
                userRepository.save(seller1);
            }

            // Create admin
            if (!userRepository.existsByUserName("admin1")) {
                User admin1 = new User(
                        passwordEncoder.encode("password2"),
                        "user3@gmail.com",
                        "admin1"
                );

                admin1.setRoles(adminRoles);
                userRepository.save(admin1);
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider()
    {
        DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider(userDetailsServiceImpl);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {

        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unAuthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/auth/**").permitAll()
                                .requestMatchers("/v3/api-docs/**").permitAll()
                                .requestMatchers("/h2-console/**").permitAll()
//                                .requestMatchers("/api/admin/**").permitAll()
//                                .requestMatchers("/api/public/**").permitAll()
                                .requestMatchers("/swagger-ui/**").permitAll()
                                .requestMatchers("/api/test/**").permitAll()
                                .requestMatchers("/images/**").permitAll()
                                .anyRequest().authenticated()
                );

        http.authenticationProvider(daoAuthenticationProvider());

        http.addFilterBefore(authTokenFilter, UsernamePasswordAuthenticationFilter.class);
        http.headers(headers->headers.frameOptions(
                HeadersConfigurer.FrameOptionsConfig::sameOrigin));
        return http.build();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web -> web.ignoring().requestMatchers("/v2/api-docs",
                "/configuration/ui",
                "/swagger-resources/**",
                "/configuration/security",
                "/swagger-ui.html",
                "/webjars/**"));
    }
}
