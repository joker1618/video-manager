package xxx.joker.apps.video.manager.model.entity;


import xxx.joker.apps.video.manager.repository.entity.*;

public class CategoryNW extends JkDefaultEntity {

    @JkEntityField(index = 0)
    private String name;

    public CategoryNW() {
    }

    public CategoryNW(String name) {
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
