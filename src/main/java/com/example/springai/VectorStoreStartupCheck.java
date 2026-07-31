package com.example.springai.config;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class VectorStoreStartupCheck implements ApplicationRunner {

    private final VectorStore vectorStore;

    public VectorStoreStartupCheck(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println(
                "VectorStore bean loaded: " + vectorStore.getClass().getName()
        );
    }
}