package com.gugugaga.movie.controller;
import com.gugugaga.movie.entity.Movie;
import com.gugugaga.movie.entity.VideoInfo;
import com.gugugaga.movie.service.MovieService;
import com.gugugaga.movie.service.VideoStreamingService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/movies")
public class MovieController {
    private final MovieService movieService;
    private final VideoStreamingService videoStreamingService;

    public MovieController(MovieService movieService, VideoStreamingService videoStreamingService) {
        this.movieService = movieService;
        this.videoStreamingService = videoStreamingService;
    }
    @GetMapping
    public ResponseEntity<?> getAllMovies(){
        try {
            List<Movie> movies = movieService.getAllMovies();
            if ( movies.isEmpty() ) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                        "success", false,
                        "message", "No movies found"
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", movies
            ));
        } catch ( Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat mendapatkan data",
                    "error", e.getMessage()
            ) );
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getMovieById(@PathVariable(required = false) Long id) {
        // 1. Validate
        if ( id == null || id <= 0 ){
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Id tidak boleh kosong dan harus berupa number"
            ));
        }
        try {
            //2. Check if exists
            Optional<Movie> checkData = movieService.getMovieById(id);
            if ( checkData.isEmpty() ) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false,
                        "message", "Data tidak ditemukan",
                        "id", id
                ));
            }
            return ResponseEntity.ok( Map.of(
                    "success", true,
                    "data", checkData.get()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan",
                    "error", e.getMessage()
            ));
        }
    }
    @PostMapping
    public ResponseEntity<?> createMovie(@Valid @RequestBody Movie movie) {
        try {
            Movie savedMovie = movieService.createMovie(movie);
            URI location = URI.create("/api/movies" + savedMovie.getId());
            return ResponseEntity.created(location).body( Map.of(
                    "success", false,
                    "message", "Berhasil menyimpan data",
                    "data", savedMovie
            ));
        } catch ( Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat membuat data",
                    "error", e.getMessage()
            ));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMovie(@PathVariable Long id) {
        try {
           if ( id <= 0 ){
               return ResponseEntity.status(HttpStatus.BAD_REQUEST).body( Map.of(
                       "success", false,
                       "message", "Id tidak ditemukan.",
                       "id", id
               ));
           }
           movieService.softDeleteMovie(id);
           URI location = URI.create("/api/movies");
           return ResponseEntity.created(location).body( Map.of(
                    "success", true,
                    "message", "Berhasil menghapus data"
            ));

        } catch ( Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                    "success", false,
                    "message", "Terjadi kesalahan saat menghapus data",
                    "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/videoInfo")
    public ResponseEntity<?> getVideoInfo( @PathVariable Long id, HttpServletRequest req) {
        try {
            String userIdHeader = req.getHeader("X-User-Id");
            // Check if header exists and is not empty
            if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "User not authenticated - missing user ID",
                    "debug", "X-User-Id header: " + userIdHeader
                ));
            }
            Long userId;
            try {
                userId = Long.valueOf(userIdHeader.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", "Invalid user ID format",
                    "debug", "X-User-Id header: " + userIdHeader
                ));
            }
            VideoInfo videoInfo = videoStreamingService.getVideoInfo(id, userId);
            return ResponseEntity.ok( Map.of(
                "success", true,
                "data", videoInfo,
                "userId", 1L
            ));
        } catch ( Exception e ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                "success", false,
                "message", "Terjadi kesalahan saat mendapatkan data",
                "error", e.getMessage()
            ));
        }
    }
    
}
