package com.plip.topic.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

	private static final String BEARER_AUTH = "bearerAuth";
	private static final String USER_UUID = "userUuid";

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Topic Service API")
						.description("아지트 토픽 API. 토픽 HTTP는 인증 필수입니다.")
						.version("v1.0.0"))
				.components(new Components()
						.addSecuritySchemes(
								BEARER_AUTH,
								new SecurityScheme()
										.type(SecurityScheme.Type.HTTP)
										.scheme("bearer")
										.bearerFormat("JWT")
										.description(
												"Gateway JWT. 클라이언트는 Bearer를 전달하고, "
														+ "Gateway가 검증 후 X-User-UUID를 주입합니다."
										)
						)
						.addSecuritySchemes(
								USER_UUID,
								new SecurityScheme()
										.type(SecurityScheme.Type.APIKEY)
										.in(SecurityScheme.In.HEADER)
										.name("X-User-UUID")
										.description("topic 직접 호출 시 로그인 사용자 UUID. 없으면 401.")
						))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
				.addSecurityItem(new SecurityRequirement().addList(USER_UUID));
	}
}
