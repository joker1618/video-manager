package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.datalayer.design.RepoEntity;
import xxx.joker.libs.datalayer.design.RepoField;

import java.util.*;

public class Video extends RepoEntity {

    @RepoField
    private String title;
    @RepoField
    private String md5;
    @RepoField
    private long size;
    @RepoField
    private int width;
    @RepoField
    private int height;
    @RepoField
    private JkDuration length;
    @RepoField
    private Set<Category> categories;
    @RepoField
    private Set<JkDuration> snapTimes;
    @RepoField
    private boolean marked;


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

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public double getFormat() {
        return height == 0 ? 0d : (double) width / height;
    }

    public void setHeight(int height) {
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

    public Set<JkDuration> getSnapTimes() {
        return snapTimes;
    }

    public void setSnapTimes(Collection<JkDuration> snapTimes) {
        this.snapTimes = new TreeSet<>(snapTimes);
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    @Override
    public String getPrimaryKey() {
        return md5;
    }

}
