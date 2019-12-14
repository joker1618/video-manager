package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.directive.CascadeDelete;
import xxx.joker.libs.repo.design.annotation.marker.EntityField;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;
import xxx.joker.libs.repo.design.entities.RepoResource;

public class VideoSnap extends SimpleRepoEntity {

    @EntityPK
    private String md5Video;
    @EntityPK
    private JkDuration snapTime;
    @EntityField
    @CascadeDelete
    private RepoResource resource;


    public VideoSnap() {

    }

    public VideoSnap(String md5Video, JkDuration snapTime, RepoResource resource) {
        this.md5Video = md5Video;
        this.snapTime = snapTime;
        this.resource = resource;
    }

    public String getMd5Video() {
        return md5Video;
    }

    public void setMd5Video(String md5Video) {
        this.md5Video = md5Video;
    }

    public JkDuration getSnapTime() {
        return snapTime;
    }

    public void setSnapTime(JkDuration snapTime) {
        this.snapTime = snapTime;
    }

    public RepoResource getResource() {
        return resource;
    }

    public void setResource(RepoResource resource) {
        this.resource = resource;
    }
}
