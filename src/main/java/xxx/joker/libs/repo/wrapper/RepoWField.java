package xxx.joker.libs.repo.wrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.runtime.wrapper.FieldWrapper;
import xxx.joker.libs.core.runtime.wrapper.TypeWrapper;
import xxx.joker.libs.repo.config.RepoConfig;
import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.design.annotation.directive.CascadeDelete;
import xxx.joker.libs.repo.design.annotation.marker.*;

import java.lang.reflect.Field;
import java.util.*;

public class RepoWField extends FieldWrapper {

    private static final Logger LOG = LoggerFactory.getLogger(RepoWField.class);

    public RepoWField(Field field) {
        super(field);
    }

    public boolean isEntity() {
        return instanceOf(RepoEntity.class);
    }
    public boolean isEntityFlat() {
        return instanceOfFlat(RepoEntity.class);
    }
    public boolean isEntityID() {
        return containsAnnotation(EntityID.class);
    }
    public boolean isCreationTm() {
        return containsAnnotation(CreationTm.class);
    }
    public boolean isEntityPK() {
        return containsAnnotation(EntityPK.class);
    }
    public boolean isForeignID() {
        return containsAnnotation(ForeignID.class);
    }
    public boolean isResourcePath() {
        EntityField ann = field.getAnnotation(EntityField.class);
        return ann != null && ann.refType().equals(RepoConfig.REF_TYPE_FIELD_RESOURCE_PATH);
    }
    public boolean isCascadeDelete() {
        return containsAnnotation(CascadeDelete.class);
    }

    public void initNullValue(Object instance) {
        if (getValue(instance) == null) {
            Object o = null;

            if (isList()) {
                if (isOfClass(LinkedList.class)) {
                    o = new LinkedList<>();
                } else if (isOfClass(ArrayList.class, List.class)) {
                    o = new ArrayList<>();
                }

            } else if (isSet()) {
                if (isOfClass(HashSet.class)) {
                    o = new HashSet<>();
                } else if (isOfClass(LinkedHashSet.class)) {
                    o = new LinkedHashSet<>();
                } else if (isOfClass(TreeSet.class)) {
                    o = new TreeSet<>();
                } else if (isOfClass(Set.class)) {
                    o = getParamTypes().get(0).isComparable() ? new TreeSet<>() : new LinkedHashSet<>();
                }

            } else if (isMap()) {
                if (isOfClass(HashMap.class)) {
                    o = new HashMap<>();
                } else if (isOfClass(LinkedHashMap.class)) {
                    o = new LinkedHashMap<>();
                } else if (isOfClass(TreeMap.class)) {
                    o = new TreeMap<>();
                } else if (isOfClass(Map.class)) {
                    o = getParamTypes().get(0).isComparable() ? new TreeMap<>() : new LinkedHashMap<>();
                }
            }

            if (o != null) {
                setValue(instance, o);
            }
        }
    }

    public Class<?> getCollType() {
        return isCollection() ? getTypeClass(0) : null;
    }


    public List<Class<?>> retrieveEntityParametrizedTypes() {
        return retrieveEntityParametrizedTypes(this);
    }

    private List<Class<?>> retrieveEntityParametrizedTypes(TypeWrapper tw) {
        List<Class<?>> toRet = new ArrayList<>();
        if (tw.instanceOf(RepoEntity.class)) {
            toRet.add(tw.getTypeClass());
        }
        for (TypeWrapper twChild : tw.getParamTypes()) {
            toRet.addAll(retrieveEntityParametrizedTypes(twChild));
        }
        return toRet;
    }

    @Override
    public String toString() {
        return field.getName();
    }

}