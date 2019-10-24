package xxx.joker.apps.video.manager.datalayer.entities;


import xxx.joker.libs.datalayer.design.EntityPK;
import xxx.joker.libs.datalayer.design.RepoEntity;
import xxx.joker.libs.datalayer.design.EntityField;

public class Category extends RepoEntity {

    @EntityPK
    @EntityField
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
