package xxx.joker.apps.video.manager.repository.entity;

import org.apache.commons.lang3.StringUtils;

public abstract class JkComparableEntity extends JkDefaultEntity {

    @Override
    public int compareTo(JkEntity o) {
        return StringUtils.compare(getPrimaryKey(), o.getPrimaryKey());
    }

}
