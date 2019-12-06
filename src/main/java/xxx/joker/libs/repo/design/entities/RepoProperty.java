package xxx.joker.libs.repo.design.entities;

import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.marker.EntityField;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;

public final class RepoProperty extends SimpleRepoEntity {

    @EntityPK
    private String key;
    @EntityField
    private String value;

    public RepoProperty() {
    }
    public RepoProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public String getKey() {
        return key;
    }
	public void setKey(String key) {
        this.key = key;
    }
	public String getValue() {
        return value;
    }
	public Long getLong() {
        return Long.parseLong(value);
    }
	public void setValue(String value) {
        this.value = value;
    }
}
