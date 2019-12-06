package xxx.joker.libs.repo.jpa.persistence;


import xxx.joker.libs.repo.design.RepoEntity;

import java.util.ArrayList;
import java.util.List;

public class DaoDTO {

    private RepoEntity entity;
    private List<DaoFK> foreignKeys;

    public DaoDTO() {
        this.foreignKeys = new ArrayList<>();
    }
    public DaoDTO(RepoEntity entity) {
        this.entity = entity;
        this.foreignKeys = new ArrayList<>();
    }

    public RepoEntity getEntity() {
        return entity;
    }

    public void setEntity(RepoEntity entity) {
        this.entity = entity;
    }

    public List<DaoFK> getForeignKeys() {
        return foreignKeys;
    }

    public void setForeignKeys(List<DaoFK> foreignKeys) {
        this.foreignKeys = foreignKeys;
    }
}
