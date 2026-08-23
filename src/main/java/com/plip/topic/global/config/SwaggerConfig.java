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

	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Topic Service API")
						.description("아지트 토픽 API")
						.version("v1.0.0"))
				.components(new Components().addSecuritySchemes(
						BEARER_AUTH,
						new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description(
										"Gateway JWT 인증. 클라이언트는 Bearer를 전달하고, "
												+ "Gateway가 검증 후 downstream에 X-User-UUID를 주입합니다."
								)
				))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
	}
}
