package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.directive.CascadeDelete;
import xxx.joker.libs.repo.design.annotation.marker.EntityField;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;
import xxx.joker.libs.repo.design.entities.RepoResource;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

public class Video extends SimpleRepoEntity {

    @EntityField
    private String title;
    @EntityPK
    private String md5;
    @EntityField
    private long size;
    @EntityField
    private int width;
    @EntityField
    private int height;
    @EntityField
    private JkDuration length;
    @EntityField
    private Set<Category> categories;
    @EntityField
    private Set<JkDuration> snapTimes;
    @EntityField
    private boolean marked;
    @EntityField
    @CascadeDelete
    private RepoResource videoResource;
//    @CascadeDelete
//    @EntityField
//    private Set<VideoSnap> snapshots;


    public Video() {
//        this.categories = new TreeSet<>();
    }

    public static Comparator<Video> titleComparator() {
        return Comparator.comparing(v -> v.getTitle().toLowerCase());
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

    public RepoResource getVideoResource() {
        return videoResource;
    }

    public void setVideoResource(RepoResource videoResource) {
        this.videoResource = videoResource;
    }

//    public Set<VideoSnap> getSnapshots() {
//        return snapshots;
//    }
//
//    public void setSnapshots(Set<VideoSnap> snapshots) {
//        this.snapshots = snapshots;
//    }
}
