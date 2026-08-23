package com.plip.topic.global.config;

import com.plip.topic.global.security.JwtAuthenticationEntryPoint;
import com.plip.topic.global.security.UserUuidHeaderAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserUuidHeaderAuthenticationFilter userUuidHeaderAuthenticationFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(session ->
						session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(exception ->
						exception.authenticationEntryPoint(jwtAuthenticationEntryPoint))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers(
								"/actuator/health",
								"/actuator/info",
								"/v3/api-docs",
								"/v3/api-docs/**",
								"/swagger-ui.html",
								"/swagger-ui/**"
						).permitAll()
						.requestMatchers("/api/v1/topics", "/api/v1/topics/**").authenticated()
						.anyRequest().permitAll()
				)
				.addFilterBefore(
						userUuidHeaderAuthenticationFilter,
						UsernamePasswordAuthenticationFilter.class
				);

		return http.build();
	}
}
