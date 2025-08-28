package com.gugugaga.movie.entity;

import java.io.File;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoInfo {
    private File file;
    private String contentType;
    private Movie movie;
    private long fileSize;
    private String quality;
}
