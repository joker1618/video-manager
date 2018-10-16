package xxx.joker.apps.video.manager.repository;

import xxx.joker.apps.video.manager.repository.entity.JkEntity;
import xxx.joker.libs.javalibs.utils.JkReflection;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

class RepoUtil {

    private static final List<Class<?>> ALLOWED_CLASSES = Arrays.asList(
            boolean.class,
            int.class,
            long.class,
            float.class,
            double.class,

            Boolean.class,        Boolean[].class,
            Integer.class,        Integer[].class,
            Long.class,           Long[].class,
            Float.class,          Float[].class,
            Double.class,         Double[].class,
            File.class,           File[].class,
            Path.class,           Path[].class,
            LocalTime.class,      LocalTime[].class,
            LocalDate.class,      LocalDate[].class,
            LocalDateTime.class,  LocalDateTime[].class,
            String.class,         String[].class,

            List.class,
            Set.class
    );

    public static boolean isClassAllowed(Class<?> clazz) {
        return ALLOWED_CLASSES.contains(clazz) || JkReflection.isOfType(clazz, JkEntity.class);
    }

}