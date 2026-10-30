package com.piseth.example.spring.phone_shop.config.security;

import com.piseth.example.spring.phone_shop.config.jwt.FilterChainExceptionHandler;
import com.piseth.example.spring.phone_shop.config.jwt.JwtLoginFilter;
import com.piseth.example.spring.phone_shop.config.jwt.TokenVerifyFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private FilterChainExceptionHandler filterChainExceptionHandler;
    private AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(filterChainExceptionHandler, JwtLoginFilter.class)
                .addFilterAfter(new TokenVerifyFilter(), JwtLoginFilter.class)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                            "/login",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/v3/api-docs.yaml"
                        ).permitAll()
                        .requestMatchers("/", "/index.html", "/css/**", "/js/**")
                        .permitAll()
//                        .requestMatchers(HttpMethod.PUT, "/brands/**")
//                        .hasAuthority(PermissionEnum.BRAND_WRITE.getDescription())
//                        .requestMatchers("/brands")
//                        .hasRole("SALE") // "SALE"
                        .requestMatchers(HttpMethod.POST, "/brands")
                        .hasAnyAuthority("brand:write")
                        .requestMatchers(HttpMethod.GET, "/brands")
                        .hasAnyAuthority("brand:read")
                        .anyRequest()
                        .authenticated())
                .addFilter(new JwtLoginFilter(authenticationManager(authenticationConfiguration)))
                .authenticationProvider(getAuthenticationProvider());
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider getAuthenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }
}
