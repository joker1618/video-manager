package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.datalayer.design.RepoEntity;
import xxx.joker.libs.datalayer.design.RepoField;

import java.util.Set;

public class Video extends RepoEntity {

    @RepoField
    private String title;
    @RepoField
    private String md5;
    @RepoField
    private long size;
    @RepoField
    private Integer width;
    @RepoField
    private Integer height;
    @RepoField
    private JkDuration length;
    @RepoField
    private Set<Category> categories;
    @RepoField
    private int playTimes;
    @RepoField
    private Set<Long> snapTimes;


    public Video() {
//        this.categories = new TreeSet<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public JkDuration getLength() {
        return length;
    }

    public void setLength(JkDuration length) {
        this.length = length;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public int getPlayTimes() {
        return playTimes;
    }

    public void setPlayTimes(int playTimes) {
        this.playTimes = playTimes;
    }

    public Set<Long> getSnapTimes() {
        return snapTimes;
    }

    public void setSnapTimes(Set<Long> snapTimes) {
        this.snapTimes = snapTimes;
    }

    @Override
    public String getPrimaryKey() {
        return md5;
    }

    public void incrementPlayTimes() {
        playTimes++;
    }
}
