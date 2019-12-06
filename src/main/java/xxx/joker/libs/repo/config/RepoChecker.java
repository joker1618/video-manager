package xxx.joker.libs.repo.config;

import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.repo.design.annotation.marker.CreationTm;
import xxx.joker.libs.repo.design.annotation.marker.EntityID;
import xxx.joker.libs.repo.design.annotation.marker.EntityPK;
import xxx.joker.libs.repo.design.annotation.marker.ForeignID;
import xxx.joker.libs.repo.exceptions.RepoError;
import xxx.joker.libs.repo.wrapper.RepoWClazz;
import xxx.joker.libs.repo.wrapper.RepoWField;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.function.Predicate;

import static xxx.joker.libs.core.util.JkConvert.toList;
import static xxx.joker.libs.repo.config.RepoConfig.*;
import static xxx.joker.libs.repo.exceptions.ErrorType.*;

public class RepoChecker {

    public static void checkEntityClass(RepoWClazz cw) throws RepoError {
        // Check field type
        for (RepoWField fw : cw.getFields()) {
            if(!RepoConfig.isValidFieldType(fw)) {
                throw new RepoError(
                        DESIGN_WRONG_FIELD_TYPE,
                        "Invalid field type [class={}, field={}, type={}]",
                        cw.getEClazz(), fw.getFieldName(), fw.getFieldType()
                );
            }
        }

        // Check ID
        checkField(cw, RepoWField::isEntityID, "@EntityID", true, VALID_TYPE_ENTITY_ID, EntityID.class);
        // Check creation time
        checkField(cw, RepoWField::isCreationTm, "@CreationTm", true, VALID_TYPE_CREATION_TM, CreationTm.class);
        // Check resource path
        checkField(cw, RepoWField::isResourcePath, "@EntityField used as a resource path", false, VALID_TYPE_RESOURCE_PATH, null);
        // Check foreign ID
        checkField(cw, RepoWField::isForeignID, "@ForeignID", false, VALID_TYPE_FOREIGN_ID, ForeignID.class);
        // Check PK
        checkField(cw, RepoWField::isEntityPK, "@EntityPK", false, RepoConfig::isValidTypeForEntityPK, EntityPK.class);
        // Check cascade delete
        checkField(cw, RepoWField::isCascadeDelete, "@CascadeDelete", false, RepoConfig::isValidTypeForCascadeDelete, EntityPK.class);
    }

    private static void checkField(RepoWClazz cw, Predicate<RepoWField> cond, String label, boolean unique, List<Class<?>> validClasses, Class<? extends Annotation> allowed) {
        checkField(cw, cond, label, unique, wf -> wf.isOfClass(validClasses), allowed);
    }
    private static void checkField(RepoWClazz cw, Predicate<RepoWField> cond, String label, boolean unique, Predicate<RepoWField> validClass, Class<? extends Annotation> allowed) {
        String cname = cw.getEClazz().getSimpleName();

        List<RepoWField> fwList = cw.getFields(cond);
        if(unique && fwList.size() != 1)
            throw new RepoError(DESIGN_FIELD_NOT_UNIQUE, "Must be present exactly one field annotated with {} (found={}). [class={}]", label, fwList, cname);

        for (RepoWField fw : fwList) {
            if(!validClass.test(fw))
                throw new RepoError(DESIGN_WRONG_FIELD_TYPE, "Fields type not allowed for {} [class={}, fields={}]", label, cname, fw);
        }

        List<Class<? extends Annotation>> notAllowed = toList(EntityID.class, CreationTm.class, EntityPK.class, ForeignID.class);
        notAllowed.remove(allowed);
        for (RepoWField wf : fwList) {
            List<Class<? extends Annotation>> found = JkStreams.filter(notAllowed, wf::containsAnnotation);
            if(!found.isEmpty())
                throw new RepoError(DESIGN_ANNOTATION_NOT_ALLOWED, "{} cannot appear with: {}", label, found);
        }
    }


}
