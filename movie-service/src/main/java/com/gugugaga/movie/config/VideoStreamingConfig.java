package com.gugugaga.movie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableConfigurationProperties
public class VideoStreamingConfig {
    @Bean
    public TaskExecutor videoProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("video-processing-");
        executor.initialize();
        return executor;
    }

    @Bean
    @ConfigurationProperties(prefix = "app.video")
    public VideoProperties videoProperties() {
        return new VideoProperties();
    }
}
