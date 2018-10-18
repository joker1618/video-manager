package xxx.joker.apps.video.manager.model.entity;


import xxx.joker.libs.javalibs.repository.entity.*;

public class Category extends JkDefaultEntity {

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
