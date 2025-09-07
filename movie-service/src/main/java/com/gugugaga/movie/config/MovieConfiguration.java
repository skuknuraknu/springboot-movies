package com.gugugaga.movie.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.movie")
@EnableConfigurationProperties
public class MovieConfiguration {
    
    private Streaming streaming = new Streaming();
    private Messages messages = new Messages();
    private Security security = new Security();
    private Testing testing = new Testing();
    
    @Data
    public static class Streaming {
        private int bufferSizeBytes = 16384; // 16KB chunks
        private long cacheMaxAgeSeconds = 3600; // 1 hour
        private String defaultContentType = "video/mp4";
    }
    
    @Data
    public static class Messages {
        // Indonesian error messages - externalized for internationalization
        private String noMoviesFound = "No movies found";
        private String movieNotFound = "Data tidak ditemukan";
        private String invalidId = "Id tidak boleh kosong dan harus berupa number";
        private String generalError = "Terjadi kesalahan";
        private String createError = "Terjadi kesalahan saat membuat data";
        private String deleteError = "Terjadi kesalahan saat menghapus data";
        private String getDataError = "Terjadi kesalahan saat mendapatkan data";
        private String saveSuccess = "Berhasil menyimpan data";
        private String deleteSuccess = "Berhasil menghapus data";
        private String userNotAuthenticated = "User not authenticated - missing user ID";
        private String invalidUserIdFormat = "Invalid user ID format";
        private String videoInfoNull = "VideoInfo is null";
        private String streamingError = "Error streaming video";
        private String streamingRangeError = "Error streaming video range";
    }
    
    @Data
    public static class Security {
        private List<String> corsOrigins = List.of("*");
        private List<String> corsMethods = List.of("GET", "HEAD", "OPTIONS", "POST", "PUT", "DELETE");
        private List<String> corsHeaders = List.of("Range", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-User-Id");
        private List<String> exposedHeaders = List.of("Content-Length", "Content-Range", "Accept-Ranges");
        private String userIdHeader = "X-User-Id";
        private String rangeHeader = "Range";
    }
    
    @Data
    public static class Testing {
        private Long testUserId = 1L;
        private boolean debugMode = false;
    }
}