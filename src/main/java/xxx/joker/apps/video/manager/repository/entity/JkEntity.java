package xxx.joker.apps.video.manager.repository.entity;

import java.time.LocalDateTime;

public interface JkEntity extends Comparable<JkEntity> {

    String getPrimaryKey();

    Long getEntityID();
    void setEntityID(long entityID);

    LocalDateTime getInsertTstamp();
    void setInsertTstamp(LocalDateTime insertTstamp);

}
