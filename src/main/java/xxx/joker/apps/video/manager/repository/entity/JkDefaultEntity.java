package xxx.joker.apps.video.manager.repository.entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public abstract class JkDefaultEntity implements JkEntity {

    protected Long entityID;

    @Override
    public final Long getEntityID() {
        return entityID;
    }

    @Override
    public final void setEntityID(long entityID) {
        this.entityID = entityID;
    }

    @Override
    public int hashCode() {
        return getComparatorKey().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JkEntity other = (JkEntity) o;
        return StringUtils.equals(getComparatorKey(), other.getComparatorKey());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public int compareTo(JkEntity o) {
        return StringUtils.compare(getComparatorKey(), o.getComparatorKey());
    }

}
