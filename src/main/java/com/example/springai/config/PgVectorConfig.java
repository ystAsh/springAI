package com.example.springai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class PgVectorConfig {

    @Bean(name = "vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate(
            @Qualifier("vectorDataSource")
            DataSource vectorDataSource
    ) {
        return new JdbcTemplate(
                vectorDataSource
        );
    }

    @Bean
    public VectorStore vectorStore(
            @Qualifier("vectorJdbcTemplate")
            JdbcTemplate vectorJdbcTemplate,
            EmbeddingModel embeddingModel
    ) {
        return PgVectorStore.builder(
                        vectorJdbcTemplate,
                        embeddingModel
                )
                .dimensions(768)
                .initializeSchema(false)
                .build();
    }
}