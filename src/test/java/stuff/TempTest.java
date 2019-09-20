package stuff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import xxx.joker.libs.core.adapter.JkProcess;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.lambdas.JkStreams;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.utils.JkConsole.display;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class TempTest {

	@Test
	public void splitFile() throws IOException {
        Path baseFolder = Paths.get("src/test/resources");
        String fname = "ert.mp4";
        List<Path> files = JkFiles.findFiles(baseFolder, false, p -> !p.getFileName().toString().equals(fname));
        files.forEach(JkFiles::delete);
        String exePath = "lib/ffmpeg.exe";
        JkProcess ex = JkProcess.execute(baseFolder, "{} -ss 00:00:00 -t 00:00:10 -i {} -acodec copy -vcodec copy small.1.{}", exePath, fname, fname);
        display(ex.toStringResult(0));
        ex = JkProcess.execute(baseFolder, "{} -ss 00:00:12 -t 00:00:10 -i {} -acodec copy -vcodec copy small.2.{}", exePath, fname, fname);
        display(ex.toStringResult(0));
    }


	@Test
	public void joinFiles() throws IOException {
        Path baseFolder = Paths.get("src\\test\\resources");
        String fileTxtName = "fileList.txt";
        Path fileTxt = baseFolder.resolve(fileTxtName);
        JkFiles.delete(fileTxt);
        String fnamePrefix = "small";
        List<Path> files = JkFiles.findFiles(baseFolder, false, p -> p.getFileName().toString().startsWith(fnamePrefix));
        JkFiles.writeFile(fileTxt, JkStreams.map(files, f -> strf("file {}", f.getFileName().toString())));
        String exePath = "lib/ffmpeg.exe";
        String outFname = "ert2.mp4";
        JkProcess ex = JkProcess.execute(baseFolder, "{} -f concat -i {} -acodec copy -vcodec copy {}", exePath, fileTxtName, outFname);
        display(ex.toStringResult(0));

    }


    @Test
    public void test2() throws IOException {
        Path exePath = Paths.get("lib/ffmpeg.exe");
        display(Files.exists(exePath));
    }



}
