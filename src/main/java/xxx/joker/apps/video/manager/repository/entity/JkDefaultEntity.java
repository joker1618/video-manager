package xxx.joker.apps.video.manager.repository.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.time.LocalDateTime;

public abstract class JkDefaultEntity implements JkEntity {

    protected Long entityID;
    protected LocalDateTime insertTstamp;

    @Override
    public int hashCode() {
        return getPrimaryKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JkEntity other = (JkEntity) o;
        return StringUtils.equals(getPrimaryKey(), other.getPrimaryKey());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public final Long getEntityID() {
        return entityID;
    }

    @Override
    public final void setEntityID(long entityID) {
        this.entityID = entityID;
    }

    @Override
    public final LocalDateTime getInsertTstamp() {
        return insertTstamp;
    }

    @Override
    public final void setInsertTstamp(LocalDateTime insertTstamp) {
        this.insertTstamp = insertTstamp;
    }

}
