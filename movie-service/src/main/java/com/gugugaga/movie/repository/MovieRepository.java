package com.gugugaga.movie.repository;
import com.gugugaga.movie.entity.Movie;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findById(long id);

}
