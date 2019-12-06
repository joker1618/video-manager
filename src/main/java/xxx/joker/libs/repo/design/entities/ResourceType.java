package xxx.joker.libs.repo.design.entities;

import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.test.JkTests;
import xxx.joker.libs.core.util.JkConvert;

import java.nio.file.Path;
import java.util.List;

public enum ResourceType {

    IMAGE("gif", "jpeg", "jpg", "png", "tif", "tiff", "jif", "jfif"),
    MUSIC("mp3"),
    VIDEO("mp4", "avi", "mpeg", "mpg"),
    HTML("html"),
    TEXT("txt", "csv"),
    OTHER
    ;

    private List<String> extensions;

    ResourceType(String... extensions) {
        this.extensions = JkConvert.toList(extensions);
    }

    public static ResourceType fromExtension(Path path) {
        return fromExtension(JkFiles.getExtension(path));
    }
    public static ResourceType fromExtension(String extension) {
        if(!extension.isEmpty()) {
            for (ResourceType rut : values()) {
                if (JkTests.containsIgnoreCase(rut.extensions, extension)) {
                    return rut;
                }
            }
        }
        return OTHER;
    }

}
