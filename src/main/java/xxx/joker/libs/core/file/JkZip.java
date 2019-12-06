package xxx.joker.libs.core.file;

import xxx.joker.libs.core.exception.JkRuntimeException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JkZip {

    private static final int BUFFER_SIZE = 1024 * 500;

    public static void unzipArchive(Path archivePath, Path outFolder) {
        byte[] buffer = new byte[BUFFER_SIZE];

        try (FileInputStream fis = new FileInputStream(archivePath.toFile());
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry zipEntry = zis.getNextEntry();
            while(zipEntry != null){
                File newFile = outFolder.resolve(zipEntry.getName()).toFile();
                if(zipEntry.isDirectory()) {
                    Files.createDirectories(newFile.toPath());
                } else {
                    Files.createDirectories(JkFiles.getParent(newFile.toPath()));
                    try(FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();

        } catch (Exception ex) {
            throw new JkRuntimeException(ex, "Error decompressing archive {}", archivePath);
        }
    }

    /**
     * @param filesToZip files and folders
     */
    public static void zipFiles(Path archivePath, Path... filesToZip) {
        zipFiles(archivePath, Arrays.asList(filesToZip));
    }

    public static void zipFiles(Path archivePath, Collection<Path> filesToZip) {
        try {
            Files.createDirectories(JkFiles.getParent(archivePath));
            Path middleOutPath = JkFiles.safePath(archivePath);

            try (FileOutputStream fos = new FileOutputStream(middleOutPath.toFile());
                 ZipOutputStream zipOut = new ZipOutputStream(fos)) {

                for (Path path : filesToZip) {
                    File fileToZip = path.toFile();
                    zipFile(fileToZip, fileToZip.getName(), zipOut);
                }
            }

            if(!JkFiles.areEquals(archivePath, middleOutPath)) {
                JkFiles.move(middleOutPath, archivePath, true);
            }

        } catch (IOException ex) {
            throw new JkRuntimeException(ex, "Error creating ZIP archive {}", archivePath);
        }
    }

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {

        if (fileToZip.isDirectory()) {
            String dirname = fileName.endsWith("/") ? fileName : fileName + "/";
            zipOut.putNextEntry(new ZipEntry(dirname));
            zipOut.closeEntry();
            File[] children = fileToZip.listFiles();
            if(children != null) {
                for (File childFile : children) {
                    zipFile(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
            return;
        }

        try(FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[BUFFER_SIZE];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }

}
