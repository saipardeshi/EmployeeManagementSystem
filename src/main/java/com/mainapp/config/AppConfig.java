package com.mainapp.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AppConfig {

	@Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
	private String allowedOrigins;

	@Value("${app.upload-dir:uploads}")
	private String uploadDir;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public WebMvcConfigurer webMvcConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
						.allowedOrigins(allowedOrigins.split(","))
						.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
						.allowedHeaders("*")
						.allowCredentials(true);
			}

			@Override
			public void addResourceHandlers(ResourceHandlerRegistry registry) {
				// Ensure local upload directory exists
				Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
				try {
					Files.createDirectories(uploadPath);
				} catch (Exception ex) {
					System.err.println("Could not create upload directory: " + ex.getMessage());
				}

				String uploadUri = uploadPath.toUri().toString();
				if (!uploadUri.endsWith("/")) {
					uploadUri += "/";
				}

				// Serves saved employee photos at GET /images/<filename>
				registry.addResourceHandler("/images/**")
						.addResourceLocations(uploadUri);

				// Serves static web frontend assets
				registry.addResourceHandler("/**")
						.addResourceLocations("classpath:/static/");
			}
		};
	}
}
