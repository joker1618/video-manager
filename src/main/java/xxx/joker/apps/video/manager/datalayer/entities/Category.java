package xxx.joker.apps.video.manager.datalayer.entities;


import xxx.joker.libs.datalayer.design.RepoEntity;
import xxx.joker.libs.datalayer.design.RepoField;

public class Category extends RepoEntity {

    @RepoField
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

    @Override
    public String getPrimaryKey() {
        return name.toLowerCase();
    }

}
