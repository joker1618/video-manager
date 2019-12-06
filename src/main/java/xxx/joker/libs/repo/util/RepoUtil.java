package xxx.joker.libs.repo.util;

import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.format.csv.JkCsv;
import xxx.joker.libs.core.format.JkFormatter;
import xxx.joker.libs.core.format.JkOutput;
import xxx.joker.libs.core.runtime.JkRuntime;
import xxx.joker.libs.repo.design.RepoEntity;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

import static xxx.joker.libs.core.format.csv.CsvConst.SEP_FIELD;
import static xxx.joker.libs.core.util.JkConvert.toList;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class RepoUtil {

    private static JkFormatter csvParser = JkFormatter.get();
    static {
        // csvParser configs
        csvParser.setClassFormat(JkDateTime.class, d -> d.format("yyyyMMdd_HHmmss"));
        csvParser.setClassFormat(LocalDateTime.class, d -> JkDateTime.of(d).format("yyyyMMdd_HHmmss"));
        csvParser.setInstanceFormat(RepoEntity.class, RepoEntity::strMini);
    }

    public static String toStringEntities(Collection<? extends RepoEntity> coll) {
        if(coll.isEmpty())  return "";
        List<String> collLines = csvParser.formatCsv(coll);
        RepoEntity repoEntity = toList(coll).get(0);
        return strf("*** {} ({}) ***\n{}", repoEntity.getClass(), coll.size(), JkOutput.columnsView(collLines));
    }

    public static List<Class<?>> scanPackages(Class<?> launcherClazz, String... pkgsArr) {
        return scanPackages(launcherClazz, toList(pkgsArr));
    }

    public static List<Class<?>> scanPackages(Class<?> launcherClazz, Collection<String> pkgsArr) {
        Set<Class<?>> classes = new HashSet<>();
        toList(pkgsArr).forEach(pkg -> classes.addAll(JkRuntime.findClasses(launcherClazz, pkg)));
        return toList(classes);
    }

    public static Path rewriteDbWithoutCreationTm(Path dbFolder) {
        Path outFolder = JkFiles.safePath(dbFolder.toString() + "-debug-" + System.currentTimeMillis());
        List<Path> paths = JkFiles.find(dbFolder, false);
        paths.forEach(ip -> {
            removeColumnFromDbFile(ip, outFolder.resolve(ip.getFileName()), "creationTm");
        });
        return outFolder;
    }

    private static void removeColumnFromDbFile(Path inputPath, Path outPath, String... colToRemove) {
        JkCsv csv = JkCsv.readFile(inputPath, SEP_FIELD.getSeparator());
        for (String col : colToRemove)
            csv.removeCol(col);
        csv.persist(outPath);
    }

}
