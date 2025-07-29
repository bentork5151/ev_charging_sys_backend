package com.bentork.ev_system.config;

import com.bentork.ev_system.service.CustomUserDetailsService;
//import com.bentork.ev_system.config.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint; // ✅ Add this
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
      DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
      provider.setUserDetailsService(userDetailsService);
      provider.setPasswordEncoder(passwordEncoder());
      return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint) // ✅ Use custom entry point
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/user/signup",
                    "/api/user/login",
                    "/api/admin/signup",
                    "/api/admin/login",
                    "/api/user/request-otp",
                    "/api/user/reset-password",
                    "/api/admin/request-otp",
                    "/api/admin/reset-password",
                    "/oauth2/**",
                    "/login/**",
                    "/api/user/google-login-success",
                    "/error"
                ).permitAll()
                .requestMatchers("/api/location/**", "/api/stations/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(daoAuthenticationProvider())
            .oauth2Login(oauth -> oauth
                .defaultSuccessUrl("/api/user/google-login-success", true)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}





//package com.bentork.ev_system.config;
//
//import com.bentork.ev_system.service.CustomUserDetailsService;
//import com.bentork.ev_system.config.JwtAuthenticationFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
//import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.AuthenticationEntryPoint;
//import org.springframework.http.HttpMethod;
//
//@Configuration
//public class SecurityConfig {
//
//    @Autowired
//    private JwtAuthenticationFilter jwtAuthenticationFilter;
//
//    @Autowired
//    private CustomUserDetailsService userDetailsService;
//
//    // ✅ Password encoder bean
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    // ✅ Authentication manager bean
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
//
//    // ✅ Main security filter chain
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//        .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .requestMatchers(
//                    "/api/user/signup", "/api/user/login",
//                    "/api/admin/signup", "/api/admin/login"
//                ).permitAll()
//                .anyRequest().authenticated()
//            )
//            .sessionManagement(session -> session
//                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//            )
//            .authenticationProvider(daoAuthenticationProvider())
//            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    // ✅ DaoAuthenticationProvider with custom UserDetailsService
//    @Bean
//    public DaoAuthenticationProvider daoAuthenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }
//}
