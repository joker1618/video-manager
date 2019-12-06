package xxx.joker.libs.core.adapter;

import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkStrings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static xxx.joker.libs.core.util.JkStrings.strf;

public class JkProcess {

    private int exitCode;
    private String command;
    private List<String> outputLines;
    private List<String> errorLines;

    public JkProcess(String command, int exitCode, List<String> outputLines, List<String> errorLines) {
        this.exitCode = exitCode;
        this.command = command;
        this.outputLines = outputLines;
        this.errorLines = errorLines;
    }

    public static JkProcess execute(String command, Object... params) {
        return execute(Paths.get("").toAbsolutePath(), command, params);
    }
    public static JkProcess execute(Path baseFolder, String command, Object... params) {
        try {
            String cmdFormatted = strf(command, params);
            List<String> cmdParts = JkStrings.splitList(cmdFormatted, " ");
            ProcessBuilder pb = new ProcessBuilder().command(cmdParts).directory(baseFolder.toFile());

            Process p = pb.start();

            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream());
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream());

            outputGobbler.start();
            errorGobbler.start();

            int exitCode = p.waitFor();

            errorGobbler.join();
            outputGobbler.join();

            return new JkProcess(cmdFormatted, exitCode, outputGobbler.getLines(), errorGobbler.getLines());

        } catch(Throwable t) {
            throw new JkRuntimeException(t);
        }
    }

    public String toStringResult() {
        return toStringResult(null);
    }
    public String toStringResult(Integer successCode) {
        String str = "";
        str += strf("Command: {}\nExit code: {}\n", command, exitCode);
        if(successCode == null || getExitCode() != successCode) {
            str += JkStreams.joinLines(JkStrings.leftPadLines(getErrorLines(), "error>  ", 1));
        }
        str += JkStreams.joinLines(JkStrings.leftPadLines(getOutputLines(), "output> ", 1));
        return str;
    }

    private static class StreamGobbler extends Thread {
        private final InputStream is;
        private final List<String> lines;

        private StreamGobbler(InputStream is) {
            this.is = is;
            this.lines = new ArrayList<>();
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        public List<String> getLines() {
            return lines;
        }
    }


    public int getExitCode() {
        return exitCode;
    }

    public String getCommand() {
        return command;
    }

    public List<String> getOutputLines() {
        return outputLines;
    }

    public List<String> getErrorLines() {
        return errorLines;
    }
}
