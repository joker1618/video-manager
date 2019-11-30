package xxx.joker.apps.video.manager.datalayer.entities;


import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;

public class Category extends SimpleRepoEntity {

    @EntityPK
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}
