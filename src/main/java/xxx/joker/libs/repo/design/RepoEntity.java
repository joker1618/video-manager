package xxx.joker.libs.repo.design;

import org.apache.commons.lang3.builder.ToStringStyle;
import xxx.joker.libs.core.datetime.JkDateTime;

import java.time.LocalDateTime;

public interface RepoEntity extends Comparable<RepoEntity> {

    String getPrimaryKey();

    Long getEntityId();
    void setEntityId(Long entityId);

    JkDateTime getCreationTm();
    void setCreationTm();
    void setCreationTm(JkDateTime creationTm);

    String strMini();
    String strShort();
    String strFull();
    String strFull(ToStringStyle sstyle);

}
