package com.gugugaga.movie.service;
import com.gugugaga.movie.dto.CreateMovieRequest;
import com.gugugaga.movie.entity.Movie;
import com.gugugaga.movie.repository.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }
    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }
    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }
    public Movie createMovie(CreateMovieRequest req) {
        Movie movie = new Movie();
        movie.setTitle(req.getTitle());
        movie.setVideoFileName(req.getVideoFileName());
        movie.setThumbnail(req.getThumbnail());
        movie.setGenre(req.getGenre());
        movie.setReleaseYear(req.getReleaseYear());
        movie.setRating(req.getRating());
        movie.setIsDeleted(false);
        return movieRepository.save(movie);
    }
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }
    public void softDeleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Tidak ada data yang ditemukan.")
        );
        movie.setDeleted(true);
        movie.setDeletedAt(LocalDateTime.now());
        movieRepository.save(movie);
    }
}
