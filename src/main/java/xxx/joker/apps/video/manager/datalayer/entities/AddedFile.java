package xxx.joker.apps.video.manager.datalayer.entities;

import xxx.joker.libs.datalayer.design.EntityPK;
import xxx.joker.libs.datalayer.design.RepoEntity;

public class AddedFile extends RepoEntity {

    @EntityPK
    private String md5;

    public AddedFile() {
    }

    public AddedFile(String md5) {
        this.md5 = md5;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
