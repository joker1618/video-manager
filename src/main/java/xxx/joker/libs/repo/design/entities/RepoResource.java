package xxx.joker.libs.repo.design.entities;

import xxx.joker.libs.repo.config.RepoConfig;
import xxx.joker.libs.repo.design.SimpleRepoEntity;
import xxx.joker.libs.repo.design.annotation.marker.EntityField;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;

import java.nio.file.Path;

public final class RepoResource extends SimpleRepoEntity {

    @EntityField(refType = RepoConfig.REF_TYPE_FIELD_RESOURCE_PATH)
    private Path path;

    @EntityPK
    private String name;
    @EntityPK
    private RepoTags tags;
    @EntityField
    private String md5;
    @EntityField
    private ResourceType type;
    @EntityField
    private Long size;


    public RepoResource() {
    }

    public boolean match(String name, RepoTags tags) {
        return this.name.equals(name) && this.tags.equals(tags);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RepoTags getTags() {
        return tags;
    }

    public void setTags(RepoTags tags) {
        this.tags = tags;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }
}
