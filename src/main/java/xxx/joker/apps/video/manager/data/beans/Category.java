package xxx.joker.apps.video.manager.data.beans;

import xxx.joker.libs.javalibs.repository.JkDefaultRepoTable;
import xxx.joker.libs.javalibs.repository.JkRepoField;

public class Category  extends JkDefaultRepoTable {

    @JkRepoField(index = 0)
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
