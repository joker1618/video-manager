package stuff;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import xxx.joker.libs.core.adapter.JkProcess;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.runtime.JkEnvironment;
import xxx.joker.libs.core.web.JkWeb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static xxx.joker.libs.core.util.JkConsole.display;
import static xxx.joker.libs.core.util.JkStrings.strf;

public class TempTest {

    @Test
    public void dw() throws IOException {
        String url = "https://ev.ypncdn.com/201908/11/15511108/720p_1500k_15511108/YouPorn_-_girlfriend-blowjob-in-afternoon-cum-in-mouth.mp4?rate=350k\u0026burst=1600k\u0026validfrom=1569720800\u0026validto=1569735200\u0026hash=WKAGK9wa45b7oAkj4gp7Tvnio6I%3D";
//        String url = "https://ev.phncdn.com/videos/201901/06/200451641/720P_1500K_200451641.mp4?validfrom=1569721469&validto=1569728669&rate=207k&burst=1200k&hash=Lf39P9%2F9MBfA8%2FayJb2IpKaZo%2Bk%3D";
        Path outPath = JkEnvironment.getHomeFolder().resolve("Desktop/dwweb").resolve(url.replaceAll("\\?.*", "").replaceAll("^.*/", ""));
        JkWeb.downloadResource(url, outPath);
        display("END");
    }
    @Test
    public void dwhtml() throws IOException {
        String url = "https://www.pornhub.com/view_video.php?viewkey=ph5c32589432137";
        Path outPath = JkEnvironment.getHomeFolder().resolve("Desktop/dwweb/html").resolve(url.replaceAll("^.*viewkey=", ""));
        JkFiles.writeFile(outPath, JkWeb.downloadHtml(url));
        display("END");
    }
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
        List<Integer> l1 = Arrays.asList(1, 2, 3, 4);
        List<Integer> l2 = Arrays.asList(1, 2, 3, 4);
        List<Integer> l3 = Arrays.asList(2, 4, 3, 1);
        TreeSet<Integer> set1 = new TreeSet<>(l1);
        TreeSet<Integer> set3 = new TreeSet<>(l3);
        display("set {}", set1.equals(set3));
        display("l12 {}", l1.equals(l2));
        display("l13 {}", l1.equals(l3));

    }



}
