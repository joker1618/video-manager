package xxx.joker.libs.repo.jpa.persistence;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import xxx.joker.libs.core.format.JkFormatter;
import xxx.joker.libs.core.runtime.wrapper.TypeWrapper;
import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.wrapper.RepoWField;

import static xxx.joker.libs.core.util.JkConvert.toLong;

public class DaoFK implements Comparable<DaoFK> {

    private long sourceID = -1;
    private String fieldName;
    // only for map
    private int idxKey = -1;
    // only for map (id when Pair.key=true, else string fmt), null for collection
    private Pair<Boolean, String> mapKey;
    // only for collection or map with collection as a value
    private int idxValue = -1;
    // id for collection, id when Pair.key=true, else string fmt for map
    private Pair<Boolean, String> value;

    public DaoFK() {

    }

    public static DaoFK ofSingle(RepoEntity source, RepoWField fw, RepoEntity dep) {
        return ofCollection(source, fw, dep, -1);
    }
    public static DaoFK ofCollection(RepoEntity source, RepoWField fw, RepoEntity dep, int idxValue) {
        DaoFK daoFK = new DaoFK();
        daoFK.sourceID = source.getEntityId();
        daoFK.fieldName = fw.getFieldName();
        daoFK.idxValue = idxValue;
        daoFK.value = Pair.of(true, String.valueOf(dep.getEntityId()));
        return daoFK;
    }
    public static DaoFK ofMapSingle(RepoEntity source, RepoWField fw, int idxKey, Object key, Object value) {
        return ofMapColl(source, fw, idxKey, key, -1, value);
    }
    public static DaoFK ofMapColl(RepoEntity source, RepoWField fw, int idxKey, Object key, int idxValue, Object value) {
        DaoFK daoFK = new DaoFK();
        daoFK.sourceID = source.getEntityId();
        daoFK.fieldName = fw.getFieldName();
        daoFK.idxKey = idxKey;
        daoFK.idxValue = idxValue;

        JkFormatter fmt = JkFormatter.get();
        TypeWrapper twKey = fw.getParamType(0);
        boolean isKeyEntity = twKey.instanceOf(RepoEntity.class);
        if(isKeyEntity) {
            daoFK.mapKey = Pair.of(true, String.valueOf(((RepoEntity)key).getEntityId()));
        } else {
            daoFK.mapKey = Pair.of(false, fmt.formatValue(key, twKey));
        }

        TypeWrapper twValue = fw.getParamType(1);
        boolean isValueEntity = twValue.instanceOfFlat(RepoEntity.class);
        if(isValueEntity) {
            daoFK.value = Pair.of(true, String.valueOf(((RepoEntity)value).getEntityId()));
        } else if(twValue.isCollection()){
            daoFK.value = Pair.of(false, fmt.formatValue(value, twValue.getParamType(0)));
        } else {
            daoFK.value = Pair.of(false, fmt.formatValue(value, twValue));
        }

        return daoFK;
    }

    public long getSourceID() {
        return sourceID;
    }
    public String getFieldName() {
        return fieldName;
    }

    public boolean isSingleDependency() {
        return mapKey == null && idxValue == -1;
    }
    public boolean isCollection() {
        return mapKey == null && idxValue != -1;
    }
    public boolean isMapEntry() {
        return mapKey != null;
    }
    public boolean isMapKeyID() {
        return mapKey.getKey();
    }
    public boolean isMapValueID() {
        return mapKey != null && value.getKey();
    }

    public Long getSingleDepID() {
        return !isSingleDependency() ? null : toLong(value.getValue());
    }
    public Long getCollectionDepID() {
        return !isCollection() ? null : toLong(value.getValue());
    }
    public Long getMapKeyAsID() {
        return isMapKeyID() ? toLong(mapKey.getValue()) : null;
    }
    public Long getMapValueAsID() {
        return isMapValueID() ? toLong(value.getValue()) : null;
    }

    public Pair<Boolean, String> getMapKey() {
        return mapKey;
    }
    public Pair<Boolean, String> getValue() {
        return value;
    }

    public int getIdxKey() {
        return idxKey;
    }
    public int getIdxValue() {
        return idxValue;
    }


    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int compareTo(DaoFK o) {
        int res = Long.compare(sourceID, o.sourceID);
        if(res != 0)    return res;

        res = StringUtils.compareIgnoreCase(fieldName, o.fieldName);
        if(res != 0)    return res;

        res = idxKey - o.idxKey;
        if(res != 0)    return res;

        res = idxValue - o.idxValue;
        if(res != 0)    return res;

        return StringUtils.compareIgnoreCase(value.getValue(), o.value.getValue());
    }
}
