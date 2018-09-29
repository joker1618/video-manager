package xxx.joker.apps.video.manager.model.entity;

import xxx.joker.libs.javalibs.datamodel.entity.JkComparableEntity;
import xxx.joker.libs.javalibs.datamodel.entity.JkEntityField;
import xxx.joker.libs.javalibs.datamodel.entity.JkComparableEntity;
import xxx.joker.libs.javalibs.repository.JkDefaultRepoTable;
import xxx.joker.libs.javalibs.repository.JkRepoField;

public class Category extends JkComparableEntity {

    @JkEntityField(index = 0)
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
        return name != null ? name.toLowerCase() : "";
    }

}
