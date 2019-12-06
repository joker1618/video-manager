package xxx.joker.libs.repo.design;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.format.JkFormatter;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkStrings;
import xxx.joker.libs.repo.design.annotation.marker.CreationTm;
import xxx.joker.libs.repo.design.annotation.marker.EntityID;
import xxx.joker.libs.repo.wrapper.RepoWClazz;
import xxx.joker.libs.repo.wrapper.RepoWField;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xxx.joker.libs.core.util.JkConvert.toDouble;
import static xxx.joker.libs.core.util.JkStrings.strf;
import static xxx.joker.libs.repo.config.RepoConfig.PK_SEP;

public abstract class SimpleRepoEntity implements RepoEntity {

    private static final Map<Class<?>, List<RepoWField>> PK_FIELDS = Collections.synchronizedMap(new HashMap<>());

    // The formatter is used for the primary key, in case of a pk field is an instance of RepoEntity
    private static final JkFormatter FMT;
    static {
        FMT = JkFormatter.get();
        FMT.setInstanceFormat(SimpleRepoEntity.class, SimpleRepoEntity::getPrimaryKey);
    }

    @EntityID
    protected Long entityId;
    @CreationTm
    protected JkDateTime creationTm;

    protected SimpleRepoEntity() {
        if(!PK_FIELDS.containsKey(getClass())) {
            synchronized (PK_FIELDS) {
                if(!PK_FIELDS.containsKey(getClass())) {
                    RepoWClazz cw = new RepoWClazz(getClass());
                    PK_FIELDS.put(getClass(), cw.getFieldsPK());
                }
            }
        }
    }

    @Override
    public final int hashCode() {
        return getPrimaryKey().hashCode();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleRepoEntity other = (SimpleRepoEntity) o;
        return StringUtils.equalsIgnoreCase(getPrimaryKey(), other.getPrimaryKey());
    }

    @Override
    public final String getPrimaryKey() {
        return JkStreams.join(PK_FIELDS.get(getClass()), PK_SEP, fw -> FMT.formatValue(fw.getValue(this), fw)).toLowerCase();
    }

    @Override
    public String toString() {
        return strFull();
    }

    @Override
    public final Long getEntityId() {
        return entityId;
    }

    @Override
    public final JkDateTime getCreationTm() {
        return creationTm;
    }

    @Override
    public final int compareTo(RepoEntity o) {
        if(getClass() != o.getClass()) {
            return getClass().getName().compareTo(o.getClass().getName());
        }
        String[] arr1 = JkStrings.splitArr(getPrimaryKey(), PK_SEP);
        String[] arr2 = JkStrings.splitArr(o.getPrimaryKey(), PK_SEP);
        List<RepoWField> fwList = PK_FIELDS.get(getClass());
        for(int i = 0; i < fwList.size(); i++) {
            RepoWField fw = fwList.get(i);
            int res;
            if(fw.isNumber()) {
                Double n1 = Double.parseDouble(arr1[i]);
                Double n2 = Double.parseDouble(arr2[i]);
                res = n1.compareTo(n2);
            } else {
                res = StringUtils.compare(arr1[i], arr2[i]);
            }
            if(res != 0)    return res;
        }
        return 0;
    }

    @Override
    public String strShort() {
        return strf("{}[{},{}]", getClass().getSimpleName(), entityId, getPrimaryKey());
    }

    @Override
    public String strMini() {
        return strf("[{},{}]", entityId, getPrimaryKey());
    }

    @Override
    public String strFull() {
        return strFull(ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public String strFull(ToStringStyle sstyle) {
        return ReflectionToStringBuilder.toString(this, sstyle, false, false);
    }

    @Override
    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

     @Override
    public final void setCreationTm() {
        this.creationTm = JkDateTime.now();
    }

    @Override
    public final void setCreationTm(JkDateTime creationTm) {
        this.creationTm = creationTm;
    }

}
