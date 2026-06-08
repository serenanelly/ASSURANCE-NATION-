package com.assurance.nation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration JPA : repositories, transactions et audit.
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.assurance.nation.repository")
@EnableTransactionManagement
@EnableJpaAuditing
public class JpaConfig {
}
