package xxx.joker.apps.video.manager.repository.entity;

public interface JkEntity extends Comparable<JkEntity> {

    Long getEntityID();
    void setEntityID(long entityID);

    String getComparatorKey();

}
