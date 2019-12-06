package xxx.joker.libs.repo.wrapper;

import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkReflection;
import xxx.joker.libs.repo.design.RepoEntity;
import xxx.joker.libs.repo.design.annotation.directive.NoPrimaryKey;
import xxx.joker.libs.repo.design.annotation.marker.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

import static xxx.joker.libs.core.util.JkConvert.toList;

public class RepoWClazz {

    private final Class<? extends RepoEntity> eClazz;
    private final LinkedHashMap<String, RepoWField> byNameMap;

    public RepoWClazz(Class<? extends RepoEntity> eClazz) {
        this.eClazz = eClazz;

        // Find all fields annotated with one of the following
        List<Field> fields = JkReflection.findAllFields(eClazz);
        List<Class<? extends Annotation>> expAnn = Arrays.asList(
                EntityID.class, EntityPK.class, CreationTm.class, ForeignID.class, EntityField.class
        );
        List<RepoWField> fwList = JkStreams.mapFilter(fields, RepoWField::new, fw -> fw.containsAnnotation(expAnn));
        Map<String, RepoWField> fwMap = JkStreams.toMapSingle(fwList, RepoWField::getFieldName);
        this.byNameMap = new LinkedHashMap<>(fwMap);
    }

    public Class<? extends RepoEntity> getEClazz() {
        return eClazz;
    }

    public RepoWField getField(String fieldName) {
        return byNameMap.get(fieldName);
    }
    @SafeVarargs
    public final List<RepoWField> getFields(Predicate<RepoWField>... filters) {
        return JkStreams.filter(byNameMap.values(), filters);
    }

    public List<RepoWField> getFieldsByAnnotation(Class<? extends Annotation> annotationClass) {
        return JkStreams.filter(byNameMap.values(), fw -> fw.containsAnnotation(annotationClass));
    }

    public List<RepoWField> getFieldsPK() {
        if(isNoPrimaryKey()) {
            return getFieldsByAnnotation(EntityID.class);
        } else {
            return getFieldsByAnnotation(EntityPK.class);
        }
    }

    public boolean isNoPrimaryKey() {
        return eClazz.isAnnotationPresent(NoPrimaryKey.class);
    }

    public List<String> getFieldNames() {
        return toList(byNameMap.keySet());
    }



}


