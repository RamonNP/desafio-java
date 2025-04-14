package br.com.desafiojava.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "br.com.desafiojava.repository")
public class DatabaseConfig {
}
