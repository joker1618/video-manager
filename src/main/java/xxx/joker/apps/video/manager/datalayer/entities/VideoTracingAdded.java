package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;

public class VideoTracingAdded extends SimpleRepoEntity {

    @EntityPK
    private String md5;

    public VideoTracingAdded() {
    }

    public VideoTracingAdded(Video video) {
        this.md5 = video.getMd5();
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
