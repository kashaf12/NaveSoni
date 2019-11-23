package www.kfstudio.com.navesoni.models;

import java.util.Date;

public class FileModal {

    private String category;
    private String imageName;
    private String thumbnailNam;
    private Date time;
    private String downloadUrl;
    private String imageUrl;


    public FileModal(String category, String imageName, String thumbnailNam, Date time, String downloadUrl, String imageUrl) {
        this.category = category;
        this.imageName = imageName;
        this.thumbnailNam = thumbnailNam;
        this.time = time;
        this.downloadUrl = downloadUrl;
        this.imageUrl = imageUrl;
    }

    public FileModal() {

    }

    public String getThumbnailNam() {
        return thumbnailNam;
    }

    public void setThumbnailNam(String thumbnailNam) {
        this.thumbnailNam = thumbnailNam;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
