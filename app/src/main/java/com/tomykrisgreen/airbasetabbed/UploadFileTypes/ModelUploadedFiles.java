package com.tomykrisgreen.airbasetabbed.UploadFileTypes;

public class ModelUploadedFiles {
    String pId, pTime, pImage;
    String videoUrl;

    public ModelUploadedFiles() {
    }

    public ModelUploadedFiles(String pId, String pTime, String pImage, String videoUrl) {
        this.pId = pId;
        this.pTime = pTime;
        this.pImage = pImage;
        this.videoUrl = videoUrl;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getpTime() {
        return pTime;
    }

    public void setpTime(String pTime) {
        this.pTime = pTime;
    }

    public String getpImage() {
        return pImage;
    }

    public void setpImage(String pImage) {
        this.pImage = pImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
