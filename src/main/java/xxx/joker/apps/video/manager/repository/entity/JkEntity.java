package xxx.joker.apps.video.manager.repository.entity;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;

public interface JkEntity extends Comparable<JkEntity> {

    String getPrimaryKey();

    Long getEntityID();
    void setEntityID(long entityID);

    LocalDateTime getInsertTstamp();
    void setInsertTstamp(LocalDateTime insertTstamp);

    @Override
    default int compareTo(JkEntity o) {
        return StringUtils.compare(getPrimaryKey(), o.getPrimaryKey());
    }


}
