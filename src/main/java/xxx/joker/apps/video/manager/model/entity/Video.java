package xxx.joker.apps.video.manager.model.entity;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.javalibs.repository.entity.*;
import xxx.joker.libs.javalibs.datetime.JkTime;
import xxx.joker.libs.javalibs.media.analysis.JkMediaAnalyzer;
import xxx.joker.libs.javalibs.media.analysis.JkVideoInfo;
import xxx.joker.libs.javalibs.utils.JkBytes;
import xxx.joker.libs.javalibs.utils.JkEncryption;
import xxx.joker.libs.javalibs.utils.JkFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Video extends JkDefaultEntity {

    @JkEntityField(index = 0)
    private Path path;
    @JkEntityField(index = 10)
    private String videoTitle;
    @JkEntityField(index = 1)
    private String md5;
    @JkEntityField(index = 2)
    private long size;
    @JkEntityField(index = 3)
    private int width;
    @JkEntityField(index = 4)
    private int height;
    @JkEntityField(index = 5)
    private long duration;
    @JkEntityField(index = 6, collectionType = Category.class)
    private Set<Category> categories;
    @JkEntityField(index = 7)
    private int playTimes;
    @JkEntityField(index = 8)
    private boolean toBeSplit;
    @JkEntityField(index = 9)
    private boolean cataloged;


    public Video() {
        this.categories = new TreeSet<>();
    }

    public static Video createFromPath(Path path) throws Exception {
        Video video = new Video();
        video.md5 = JkEncryption.getMD5(JkBytes.getBytes(path));
        video.path = path;
        video.videoTitle = JkFiles.getFileName(path);
        video.size = Files.size(path);
        JkVideoInfo vinfo = JkMediaAnalyzer.analyzeVideo(path);
        video.width = vinfo.getWidth();
        video.height = vinfo.getHeight();
        video.duration = vinfo.getDuration();
        return video;
    }


    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getVideoTitle() {
        return videoTitle;
    }

    public void setVideoTitle(String videoTitle) {
        this.videoTitle = videoTitle;
    }

    public String getURL() {
        return JkFiles.toUrlString(path);
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isCataloged() {
        return cataloged;
    }

    public void setCataloged(boolean cataloged) {
        this.cataloged = cataloged;
    }

    public void setToBeSplit(boolean toBeSplit) {
        this.toBeSplit = toBeSplit;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getFormat() {
        return (double) width / height;
    }

    public JkTime getDuration() {
        return JkTime.of(duration);
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public int getPlayTimes() {
        return playTimes;
    }

    public boolean isToBeSplit() {
        return toBeSplit;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setDuration(JkTime duration) {
        this.duration = duration.getTotalMillis();
    }

    public void addCategories(Collection<Category> categories) {
        this.categories.addAll(categories);
    }

    public void setPlayTimes(int playTimes) {
        this.playTimes = playTimes;
    }

    public void incrementPlayTimes() {
        this.playTimes++;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String getPrimaryKey() {
        return getPath().toString().toLowerCase();
    }

}
