package com.gugugaga.movie.service;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gugugaga.movie.entity.Movie;
import com.gugugaga.movie.entity.VideoInfo;
import com.gugugaga.movie.exception.VideoNotFoundException;
import com.gugugaga.movie.repository.MovieRepository;

@Service
public class VideoStreamingService {
    @Autowired
    private MovieRepository movieRepository;

    @Value("${app.video.storage.path:videos}")
    private String videoStoragePath;

    @Value("${app.video.hls.path:/tes/hls}")
    private String hlsStoragePath;

    public VideoInfo getVideoInfo( Long movieId, Long userId ) {
        Movie movie = movieRepository.findById(movieId).orElseThrow(() -> new IllegalArgumentException("Film tidak ditemukan"));
        
        // Business logic (subscription check, payment verification, etc.)
        // .....

        File videoFile = new File( videoStoragePath, movie.getVideoFileName() );
        // Check if the file actually exists
        if (videoFile == null || !videoFile.exists() || !videoFile.isFile()) {
            throw new VideoNotFoundException("Video tidak ditemukan");
        }
        return VideoInfo.builder()
            .file(videoFile)
            .contentType(getContentType(videoFile.getName()))
            .movie(movie)
            .build();
    }
    private String getContentType(String filename) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
        switch (extension) {
            case "mp4": return "video/mp4";
            case "avi": return "video/x-msvideo";
            case "mov": return "video/quicktime";
            case "mkv": return "video/x-matroska";
            case "webm": return "video/webm";
            default: return "application/octet-stream";
        }
    }
}
