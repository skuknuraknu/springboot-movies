package com.gugugaga.movie.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateMovieRequest {
    @NotBlank(message = "Harap memasukkan title")
    private String title;

    @NotBlank(message = "Harap memasukkan nama file video")
    private String videoFileName;

    @NotBlank(message = "Harap memasukkan thumbnail")
    private String thumbnail;

    @NotBlank(message = "Harap memasukkan genre")
    private String genre;

    @NotNull(message = "Harap memasukkan tahun rilis")
    private int releaseYear;

    @DecimalMin(value = "0.0", inclusive = true, message = "Rating cannot be less than 0.0")
    @DecimalMax(value = "10.0", inclusive = true, message = "Rating cannot be more than 10.0")
    private Double rating;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoFileName() {
        return videoFileName;
    }

    public void setVideoFileName(String videoFileName) {
        this.videoFileName = videoFileName;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
