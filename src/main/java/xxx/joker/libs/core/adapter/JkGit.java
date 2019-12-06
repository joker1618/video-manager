package xxx.joker.libs.core.adapter;

import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.file.JkFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class JkGit {

    private Path gitFolder;
    private String gitUrl;

    public JkGit(Path gitFolder, String gitUrl) {
        this.gitFolder = gitFolder;
        this.gitUrl = gitUrl;
    }

    public JkProcess clone() {
        try {
            Path parent = JkFiles.getParent(gitFolder);
            Files.createDirectories(parent);
            JkFiles.delete(gitFolder);
            return JkProcess.execute(parent, "git clone {} {}", gitUrl, gitFolder.getFileName());

        } catch (IOException e) {
            throw new JkRuntimeException(e);
        }
    }

    public JkProcess pull() {
        return JkProcess.execute(gitFolder, "git pull");
    }

    public List<JkProcess> commitAndPush(String commitMex) {
        List<JkProcess> resList = new ArrayList<>();
        resList.add(JkProcess.execute(gitFolder, "git add --all"));
        resList.add(JkProcess.execute(gitFolder, "git commit -m {}", commitMex));
        resList.add(JkProcess.execute(gitFolder, "git push"));
        return resList;
    }

    public List<JkProcess> setCommitter(String userName, String userMail) {
        List<JkProcess> resList = new ArrayList<>();
        resList.add(JkProcess.execute(gitFolder, "git config user.name \"{}\"", userName));
        resList.add(JkProcess.execute(gitFolder, "git config user.email \"{}\"", userMail));
        return resList;
    }

}
