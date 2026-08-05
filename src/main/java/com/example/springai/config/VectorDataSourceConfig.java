package com.example.springai.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class VectorDataSourceConfig {

    @Bean(name = "vectorDataSource")
    public DataSource vectorDataSource(
            @Value("${app.datasource.vector.url}")
            String url,

            @Value("${app.datasource.vector.username}")
            String username,

            @Value("${app.datasource.vector.password}")
            String password,

            @Value("${app.datasource.vector.driver-class-name}")
            String driverClassName
    ) {
        return DataSourceBuilder.create()
                .driverClassName(driverClassName)
                .url(url)
                .username(username)
                .password(password)
                .build();
    }
}