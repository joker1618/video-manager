package xxx.joker.libs.core.runtime;

import xxx.joker.libs.core.util.JkConvert;

import java.nio.file.Path;
import java.nio.file.Paths;

public class JkEnvironment {

    private static final String HOME_FOLDER_KEY = "user.home";
    public static final String APPS_FOLDER_KEY = "apps.folder";

    private static final Path APPS_FOLDER_DEFAULT = getHomeFolder().resolve(".appsFolder");

    public static Path getHomeFolder() {
        return Paths.get(System.getProperty(HOME_FOLDER_KEY));
    }

    public static Path getAppsFolder() {
        String val = System.getProperty(APPS_FOLDER_KEY);
        Path p = val == null ? APPS_FOLDER_DEFAULT : Paths.get(JkConvert.unixToWinPath(val));
        return p;
    }

    public static void setAppsFolder(String folderPath) {
        setAppsFolder(Paths.get(folderPath));
    }
    public static void setAppsFolder(Path folder) {
        System.setProperty(APPS_FOLDER_KEY, folder.toAbsolutePath().normalize().toString());
    }

}
