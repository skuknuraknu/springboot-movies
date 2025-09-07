package com.gugugaga.movie.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties( prefix = "app.video")
public class VideoProperties {
    private String storagePath = "videos";
    private String hlsPath = "hls";
    private int segmentDuration = 10;
    private long maxFileSize = 5L * 1024 * 1024 * 1024; // 5GB
    private String[] supportedFormats = {"mp4", "avi", "mov", "mkv", "webm"};

    public static class Quality {
        private String name;
        private String resolution;
        private String bitrate;
        private String maxrate;
        private String bufsize;
        
        public Quality() {}
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getResolution() {
            return resolution;
        }
        
        public void setResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public String getBitrate() {
            return bitrate;
        }
        
        public void setBitrate(String bitrate) {
            this.bitrate = bitrate;
        }
        
        public String getMaxrate() {
            return maxrate;
        }
        
        public void setMaxrate(String maxrate) {
            this.maxrate = maxrate;
        }
        
        public String getBufsize() {
            return bufsize;
        }
        
        public void setBufsize(String bufsize) {
            this.bufsize = bufsize;
        }
    }
    
    private List<Quality> qualities = Arrays.asList(
        createQuality("360p", "640x360", "800k", "856k", "1200k"),
        createQuality("720p", "1280x720", "2500k", "2675k", "3750k"),
        createQuality("1080p", "1920x1080", "5000k", "5350k", "7500k")
    );
    
    public String getStoragePath() {
        return storagePath;
    }
    
    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }
    
    public String getHlsPath() {
        return hlsPath;
    }
    
    public void setHlsPath(String hlsPath) {
        this.hlsPath = hlsPath;
    }
    
    public int getSegmentDuration() {
        return segmentDuration;
    }
    
    public void setSegmentDuration(int segmentDuration) {
        this.segmentDuration = segmentDuration;
    }
    
    public long getMaxFileSize() {
        return maxFileSize;
    }
    
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }
    
    public String[] getSupportedFormats() {
        return supportedFormats;
    }
    
    public void setSupportedFormats(String[] supportedFormats) {
        this.supportedFormats = supportedFormats;
    }
    
    public List<Quality> getQualities() {
        return qualities;
    }
    
    public void setQualities(List<Quality> qualities) {
        this.qualities = qualities;
    }
    
    private Quality createQuality(String name, String resolution, String bitrate, String maxrate, String bufsize) {
        Quality quality = new Quality();
        quality.setName(name);
        quality.setResolution(resolution);
        quality.setBitrate(bitrate);
        quality.setMaxrate(maxrate);
        quality.setBufsize(bufsize);
        return quality;
    }
}
