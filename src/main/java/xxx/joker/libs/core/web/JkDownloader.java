package xxx.joker.libs.core.web;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.file.JkFiles;
import xxx.joker.libs.core.lambda.JkStreams;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class JkDownloader {

    private static final Logger LOG = LoggerFactory.getLogger(JkDownloader.class);

    private final Path folder;

    public JkDownloader(Path folder) {
        this.folder = folder;
    }

    public String getHtml(String url) {
        List<String> lines = getHtmlLines(url);
        return JkStreams.join(lines, StringUtils.LF);
    }

    public List<String> getHtmlLines(String url) {
        try {
            String fname = createOutName(url) + ".html";

            Path htmlPath = folder.resolve(fname);
            List<String> lines;
            if(!Files.exists(htmlPath)) {
                LOG.info("Downloading html from: {}", url);
                lines = JkWeb.downloadHtmlLines(url);
                JkFiles.writeFile(htmlPath, lines);
            } else {
                lines = JkFiles.readLines(htmlPath);
            }
            return lines;

        } catch(Exception ex) {
            throw new JkRuntimeException(ex);
        }
    }

    public Pair<Boolean, Path> downloadResource(String url) {
        String fname = createOutName(url);
        return downloadResource(url, fname);
    }
    public Pair<Boolean, Path> downloadResource(String url, String outFileName) {
        Path outPath = folder.resolve(outFileName);
        boolean isDw = false;
        if(!Files.exists(outPath)) {
            LOG.info("Downloading resource [{}] to [{}]", url, outPath);
            JkWeb.downloadResource(url, outPath);
            isDw = true;
        }
        return Pair.of(isDw, outPath);
    }

    public Path getFolder() {
        return folder;
    }

    private String createOutName(String url) {
        String fixed = url.replaceAll("[^\\w-.]", "_");
        String fext = JkFiles.getExtension(fixed);
        String fname = JkFiles.getFileName(fixed);
        if(fname.length() > 250) {
            fname = fname.substring(0, 250);
        }
        return fname + "." + fext;
    }

}
