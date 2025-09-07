package com.gugugaga.movie.controller;
import com.gugugaga.movie.config.MovieConfiguration;
import com.gugugaga.movie.dto.CreateMovieRequest;
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
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.RandomAccessFile;
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
    private final MovieConfiguration movieConfig;

    public MovieController(MovieService movieService, VideoStreamingService videoStreamingService, MovieConfiguration movieConfig) {
        this.movieService = movieService;
        this.videoStreamingService = videoStreamingService;
        this.movieConfig = movieConfig;
    }
    @GetMapping
    public ResponseEntity<?> getAllMovies(){
        try {
            List<Movie> movies = movieService.getAllMovies();
            if ( movies.isEmpty() ) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(Map.of(
                        "success", false,
                        "message", movieConfig.getMessages().getNoMoviesFound()
                ));
            }
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", movies
            ));
        } catch ( Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body( Map.of(
                    "success", false,
                    "message", movieConfig.getMessages().getGetDataError(),
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
                    "message", movieConfig.getMessages().getInvalidId()
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
    public ResponseEntity<?> createMovie(@Valid @RequestBody CreateMovieRequest movie) {
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
                    "message", movieConfig.getMessages().getUserNotAuthenticated(),
                    "debug", movieConfig.getSecurity().getUserIdHeader() + " header: " + userIdHeader
                ));
            }
            Long userId;
            try {
                userId = Long.valueOf(userIdHeader.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "success", false,
                    "message", movieConfig.getMessages().getInvalidUserIdFormat(),
                    "debug", movieConfig.getSecurity().getUserIdHeader() + " header: " + userIdHeader
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
    
    
    @RequestMapping(value = "/{movieId}/stream", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.HEAD})
    public ResponseEntity<StreamingResponseBody> streamVideo( @PathVariable Long movieId, @RequestHeader( value = "Range", required = false) String rangeHeader, @RequestHeader(value = "X-User-Id", required = false) String userId, @RequestParam(value = "userId", required = false) String userIdParam, @RequestParam(value = "token", required = false) String tokenParam, HttpServletRequest req) {
        try {
            // Get user ID from header or query parameter
            String effectiveUserId = userId != null ? userId : userIdParam;
            if (effectiveUserId == null || effectiveUserId.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
            Long convertedUserId = Long.valueOf(effectiveUserId.trim());
            VideoInfo videoInfo = videoStreamingService.getVideoInfo(movieId, convertedUserId);
            
            if (videoInfo == null) {
                return ResponseEntity.notFound().build();
            }
            File videoFile = videoInfo.getFile();
            long fileSize = videoFile.length();
            String contentType = videoInfo.getContentType();
            
            // Handle HEAD requests - return headers only
            if ("HEAD".equals(req.getMethod())) {
                return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Length", String.valueOf(fileSize))
                    .header("Accept-Ranges", "bytes")
                    .header("Cache-Control", "public, max-age=" + movieConfig.getStreaming().getCacheMaxAgeSeconds())
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Range, Content-Type, Accept, Origin, X-Requested-With, X-User-Id")
                    .header("Access-Control-Expose-Headers", "Content-Length, Content-Range, Accept-Ranges")
                    .build();
            }
            
            // Handle range requests for video seeking
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(videoFile, rangeHeader, fileSize, contentType);
            }
            // Full file streaming
            return createFullStreamResponse(videoFile, fileSize, contentType);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    private ResponseEntity<StreamingResponseBody> createFullStreamResponse(File videoFile, long fileSize, String contentType) {
        StreamingResponseBody responseBody = outputStream -> {
            try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(videoFile), movieConfig.getStreaming().getBufferSizeBytes())) {
                byte[] buffer = new byte[movieConfig.getStreaming().getBufferSizeBytes()]; // Configurable chunks for better performance
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            } catch (Exception e) {
                throw new RuntimeException(movieConfig.getMessages().getStreamingError(), e);
            }
        };
        
        return ResponseEntity.ok()
            .header("Content-Type", contentType)
            .header("Content-Length", String.valueOf(fileSize))
            .header("Accept-Ranges", "bytes")
            .header("Cache-Control", "public, max-age=" + movieConfig.getStreaming().getCacheMaxAgeSeconds())
            .header("Access-Control-Allow-Origin", "*")
            .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
            .header("Access-Control-Allow-Headers", "Range, Content-Type, Accept, Origin, X-Requested-With, X-User-Id")
            .header("Access-Control-Expose-Headers", "Content-Length, Content-Range, Accept-Ranges")
            .body(responseBody);
    }

    private ResponseEntity<StreamingResponseBody> handleRangeRequest(File videoFile, String rangeHeader, long fileSize, String contentType) {
        try {
            // Parse range header: "bytes=start-end"
            String rangeValue = rangeHeader.replace("bytes=", "").trim();
            String[] ranges = rangeValue.split("-");
            
            long tempStart = 0;
            long tempEnd = fileSize - 1;
            
            try {
                if (!ranges[0].isEmpty()) {
                    tempStart = Long.parseLong(ranges[0]);
                }
                if (ranges.length > 1 && !ranges[1].isEmpty()) {
                    tempEnd = Long.parseLong(ranges[1]);
                }
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
            }

            // Validate range bounds
            if (tempStart < 0 || tempEnd >= fileSize || tempStart > tempEnd) {
                return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header("Content-Range", "bytes */" + fileSize)
                    .build();
            }
            
            final long start = tempStart;
            final long end = tempEnd;
            long contentLength = end - start + 1;
            
            StreamingResponseBody responseBody = outputStream -> {
                try (RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r")) {
                    randomAccessFile.seek(start);

                    byte[] buffer = new byte[16384]; // 16KB chunks for better performance
                    long bytesToRead = contentLength;
                    
                    while (bytesToRead > 0) {
                        int chunkSize = (int) Math.min(buffer.length, bytesToRead);
                        int bytesRead = randomAccessFile.read(buffer, 0, chunkSize);
                        if (bytesRead == -1) break;
                        
                        outputStream.write(buffer, 0, bytesRead);
                        bytesToRead -= bytesRead;
                    }
                    outputStream.flush();
                } catch (Exception e) {
                    throw new RuntimeException("Error streaming video range", e);
                }
            };
            
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header("Content-Type", contentType)
                .header("Content-Length", String.valueOf(contentLength))
                .header("Content-Range", String.format("bytes %d-%d/%d", start, end, fileSize))
                .header("Accept-Ranges", "bytes")
                .header("Cache-Control", "public, max-age=" + movieConfig.getStreaming().getCacheMaxAgeSeconds())
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                .header("Access-Control-Allow-Headers", "Range, Content-Type, Accept, Origin, X-Requested-With, X-User-Id")
                .header("Access-Control-Expose-Headers", "Content-Length, Content-Range, Accept-Ranges")
                .body(responseBody);
                
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Temporary endpoint for testing video streaming without authentication
    @RequestMapping(value = "/{movieId}/stream-test", method = {org.springframework.web.bind.annotation.RequestMethod.GET, org.springframework.web.bind.annotation.RequestMethod.HEAD, org.springframework.web.bind.annotation.RequestMethod.OPTIONS})
    public ResponseEntity<StreamingResponseBody> streamVideoTest(@PathVariable Long movieId, @RequestHeader(value = "Range", required = false) String rangeHeader, HttpServletRequest req) {
        
        System.out.println("=== VIDEO STREAMING DEBUG ===");
        System.out.println("Movie ID: " + movieId);
        System.out.println("Method: " + req.getMethod());
        System.out.println("Range Header: " + rangeHeader);
        
        // Handle OPTIONS preflight request
        if ("OPTIONS".equals(req.getMethod())) {
            return ResponseEntity.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                .header("Access-Control-Allow-Headers", "Range, Content-Type, Accept, Origin, X-Requested-With")
                .header("Access-Control-Expose-Headers", "Content-Length, Content-Range, Accept-Ranges")
                .header("Access-Control-Max-Age", "3600")
                .build();
        }
        
        try {
            // Use dummy user ID for testing - REMOVE IN PRODUCTION
            Long testUserId = movieConfig.getTesting().getTestUserId();
            VideoInfo videoInfo = videoStreamingService.getVideoInfo(movieId, testUserId);
            
            if (videoInfo == null) {
                System.out.println("ERROR: VideoInfo is null");
                return ResponseEntity.notFound().build();
            }
            File videoFile = videoInfo.getFile();
            long fileSize = videoFile.length();
            String contentType = videoInfo.getContentType();
            
            System.out.println("Video File Path: " + videoFile.getAbsolutePath());
            System.out.println("File Exists: " + videoFile.exists());
            System.out.println("File Size: " + fileSize + " bytes");
            System.out.println("Content Type: " + contentType);
            System.out.println("File Name: " + videoFile.getName());
            
            // Handle HEAD requests - return headers only
            if ("HEAD".equals(req.getMethod())) {
                return ResponseEntity.ok()
                    .header("Content-Type", contentType)
                    .header("Content-Length", String.valueOf(fileSize))
                    .header("Accept-Ranges", "bytes")
                    .header("Cache-Control", "public, max-age=" + movieConfig.getStreaming().getCacheMaxAgeSeconds())
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, HEAD, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Range, Content-Type, Accept, Origin, X-Requested-With")
                    .header("Access-Control-Expose-Headers", "Content-Length, Content-Range, Accept-Ranges")
                    .build();
            }
            
            // Handle range requests for video seeking
            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                return handleRangeRequest(videoFile, rangeHeader, fileSize, contentType);
            }
            // Full file streaming
            return createFullStreamResponse(videoFile, fileSize, contentType);
        } catch (Exception e) {
            System.out.println("ERROR in stream-test: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Debug endpoint to check movie and file info
    @GetMapping("/{movieId}/debug")
    public ResponseEntity<?> debugMovieInfo(@PathVariable Long movieId) {
        try {
            Optional<Movie> movieOpt = movieService.getMovieById(movieId);
            if (movieOpt.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                    "found", false,
                    "message", "Movie not found with ID: " + movieId
                ));
            }
            
            Movie movie = movieOpt.get();
            String videoFileName = movie.getVideoFileName();
            
            // Check file path configuration
            java.lang.reflect.Field field = videoStreamingService.getClass().getDeclaredField("videoStoragePath");
            field.setAccessible(true);
            String videoStoragePath = (String) field.get(videoStreamingService);
            
            File videoFile = new File(videoStoragePath, videoFileName != null ? videoFileName : "NULL");
            
            return ResponseEntity.ok(Map.of(
                "movie", Map.of(
                    "id", movie.getId(),
                    "title", movie.getTitle(),
                    "videoFileName", videoFileName != null ? videoFileName : "NULL"
                ),
                "file", Map.of(
                    "videoStoragePath", videoStoragePath,
                    "fullPath", videoFile.getAbsolutePath(),
                    "exists", videoFile.exists(),
                    "size", videoFile.exists() ? videoFile.length() : 0,
                    "canRead", videoFile.canRead()
                ),
                "availableFiles", java.util.Arrays.asList(new File(videoStoragePath).list())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", e.getMessage(),
                "stackTrace", java.util.Arrays.toString(e.getStackTrace())
            ));
        }
    }
}
