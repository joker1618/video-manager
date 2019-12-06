package xxx.joker.libs.core.file;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.core.datetime.JkDateTime;
import xxx.joker.libs.core.exception.JkRuntimeException;
import xxx.joker.libs.core.lambda.JkStreams;
import xxx.joker.libs.core.util.JkBytes;
import xxx.joker.libs.core.util.JkConvert;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static xxx.joker.libs.core.util.JkStrings.strf;

/**
 * Created by f.barbano on 26/05/2018.
 */

public class JkFiles {

	private static final String NEWLINE = StringUtils.LF;
	private static final String TEMP_FILE = "generic.file.util.temp";

	/* WRITE methods */
	// Append
	public static void appendToFile(Path outputPath, String data) {
		appendToFile(outputPath, data, null);
	}
	public static void appendToFile(Path outputPath, String data, Charset encoding) {
		appendToFile(outputPath, data, encoding, false);
	}
	public static void appendToFile(Path outputPath, String data, boolean finalNewline) {
		appendToFile(outputPath, Collections.singletonList(data), null, finalNewline);
	}
	public static void appendToFile(Path outputPath, String data, Charset encoding, boolean finalNewline) {
		appendToFile(outputPath, Collections.singletonList(data), encoding, finalNewline);
	}

	public static void appendToFile(Path outputPath, List<String> lines) {
		appendToFile(outputPath, lines, null);
	}
	public static void appendToFile(Path outputPath, List<String> lines, Charset encoding) {
		appendToFile(outputPath, lines, encoding, !lines.isEmpty());
	}
	public static void appendToFile(Path outputPath, List<String> lines, boolean finalNewline) {
		appendToFile(outputPath, lines, null, finalNewline);
	}
	public static void appendToFile(Path outputPath, List<String> lines, Charset encoding, boolean finalNewline) {
	    try {
            Files.createDirectories(outputPath.toAbsolutePath().getParent());
            BufferedWriter writer = null;

            try {
                if (encoding == null) {
                    writer = Files.newBufferedWriter(outputPath, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } else {
                    writer = Files.newBufferedWriter(outputPath, encoding, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                }

                for (int i = 0; i < lines.size(); i++) {
                    if (i > 0) writer.write(NEWLINE);
                    writer.write(lines.get(i));
                }

                if (finalNewline) {
                    writer.write(NEWLINE);
                }

            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

        } catch (IOException ex) {
            throw new JkRuntimeException(ex);
        }
	}

	// Write
	public static void writeFile(Path outputPath, String content) {
		writeFile(outputPath, content, true);
	}
	public static void writeFile(Path outputPath, String content, boolean overwrite) {
		writeFile(outputPath, content, overwrite, null);
	}
	public static void writeFile(Path outputPath, String content, boolean overwrite, Charset encoding) {
		writeFile(outputPath, content, overwrite, encoding, false);
	}
	public static void writeFile(Path outputPath, String content, boolean overwrite, boolean finalNewLine) {
		writeFile(outputPath, Arrays.asList(content), overwrite, null, finalNewLine);
	}
	public static void writeFile(Path outputPath, String content, boolean overwrite, Charset encoding, boolean finalNewLine) {
		writeFile(outputPath, Arrays.asList(content), overwrite, encoding, finalNewLine);
	}

	public static void writeFile(Path outputPath, List<String> lines) {
		writeFile(outputPath, lines, true);
	}
	public static void writeFile(Path outputPath, List<String> lines, boolean overwrite) {
		writeFile(outputPath, lines, overwrite, null);
	}
	public static void writeFile(Path outputPath, List<String> lines, boolean overwrite, Charset encoding) {
		writeFile(outputPath, lines, overwrite, encoding, !lines.isEmpty());
	}
	public static void writeFile(Path outputPath, List<String> lines, boolean overwrite, boolean finalNewline) {
		writeFile(outputPath, lines, overwrite, null, finalNewline);
	}
	public static void writeFile(Path outputPath, List<String> lines, boolean overwrite, Charset encoding, boolean finalNewline) {
	    try {
            if (Files.exists(outputPath) && !overwrite) {
                throw new JkRuntimeException("File [" + outputPath.normalize().toString() + "] already exists");
            }
            Files.deleteIfExists(outputPath);
            appendToFile(outputPath, lines, encoding, finalNewline);

        } catch (IOException ex) {
            throw new JkRuntimeException(ex);
        }
	}

	public static void writeFile(Path outputPath, byte[] bytes) {
		writeFile(outputPath, bytes, true);
	}
	public static void writeFile(Path outputPath, byte[] bytes, boolean overwrite) {
	    try {
            if (Files.exists(outputPath) && !overwrite) {
                throw new JkRuntimeException("File [" + outputPath.normalize().toString() + "] already exists");
            }

            Files.createDirectories(outputPath.toAbsolutePath().getParent());
            Files.deleteIfExists(outputPath);
            Files.createFile(outputPath);

            try (OutputStream writer = new FileOutputStream(outputPath.toFile())) {
                writer.write(bytes);
            }

        } catch (IOException ex) {
            throw new JkRuntimeException(ex);
        }
	}

	// Insert at the beginning
	public static void insertFirstToFile(Path outputPath, String content) {
		insertFirstToFile(outputPath, content, null);
	}
	public static void insertFirstToFile(Path outputPath, String content, Charset encoding) {
		insertFirstToFile(outputPath, Collections.singletonList(content), encoding);
	}

	public static void insertFirstToFile(Path outputPath, List<String> lines) {
		insertFirstToFile(outputPath, lines, null);
	}
	public static void insertFirstToFile(Path outputPath, List<String> lines, Charset encoding) {
		try {
            if (!Files.exists(outputPath)) {
                writeFile(outputPath, lines, false, encoding);
            } else {
                Path tempFile = Paths.get(TEMP_FILE);
                writeFile(tempFile, lines, true, encoding);
                appendToFile(tempFile, Files.readAllLines(outputPath, encoding));
                Files.move(tempFile, outputPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException ex) {
            throw new JkRuntimeException(ex);
        }
	}


	/* READ methods */
	public static String read(Path filePath) {
		return JkStreams.joinLines(readLines(filePath, false, false));
	}

	public static List<String> readLines(InputStream is) {
		try {
			try (InputStreamReader isr = new InputStreamReader(is);
				 BufferedReader reader = new BufferedReader(isr)) {

				List<String> lines = new ArrayList<>();
				String line;
				while ((line = reader.readLine()) != null) {
					lines.add(line);
				}

				return lines;
			}

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}
	@SafeVarargs
	public static List<String> readLines(Path filePath, Predicate<String>... filters) {
		return readLines(filePath, false, false, filters);
	}
	@SafeVarargs
	public static List<String> readLinesNotBlank(Path filePath, Predicate<String>... filters) {
		return readLines(filePath, true, false, filters);
	}
	@SafeVarargs
	public static List<String> readLines(Path filePath, boolean removeBlankLines, boolean trimLines, Predicate<String>... filters) {
		try {
			List<String> lines = Files.readAllLines(filePath);

			if(removeBlankLines) 	lines = JkStreams.filter(lines, StringUtils::isNotBlank);
			if(trimLines) 			lines = JkStreams.map(lines, String::trim);

			Stream<String> stream = lines.stream();
			for(Predicate<String> filter : filters) {
				stream = stream.filter(filter);
			}

			return stream.collect(Collectors.toList());

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}

	public static byte[] readBytes(Path path) {
		try {
			int size = (int) Files.size(path);
			return readBytes(path, 0, size);
		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}
	public static byte[] readBytes(Path path, long start, int length) {
		try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r")) {
			return readBytes(raf, start, length);
		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}
	public static byte[] readBytes(RandomAccessFile raf, long start, int length) {
		try {
			byte[] toRet = new byte[length];
			raf.seek(start);
			int counter = raf.read(toRet);
			if (counter == length) {
				return toRet;
			}

			toRet = Arrays.copyOfRange(toRet, 0, counter);
			while (counter < length) {
				int rem = length - counter;
				byte[] arr = new byte[rem];
				int read = raf.read(arr);
				toRet = JkBytes.mergeArrays(toRet, Arrays.copyOfRange(arr, 0, read));
				counter += read;
			}

			return toRet;

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}



	/* FIND methods */
	@SafeVarargs
	public static Path findFile(Path root, boolean recursive, Predicate<Path>... filterConds) {
		List<Path> files = findFiles(root, recursive ? Integer.MAX_VALUE : 1, filterConds);
		return files.size() != 1 ? null : files.get(0);
	}
	@SafeVarargs
	public static List<Path> findFiles(Path root, int maxDepth, Predicate<Path>... filterConds) {
		List<Predicate<Path>> conds = JkConvert.toList(filterConds);
		conds.add(0, Files::isRegularFile);
		return findContent1(root, maxDepth, conds);
	}
	@SafeVarargs
	public static List<Path> findFiles(Path root, boolean recursive, Predicate<Path>... filterConds) {
		return findFiles(root, recursive ? Integer.MAX_VALUE : 1, filterConds);
	}
	@SafeVarargs
	public static Path findFolder(Path root, boolean recursive, Predicate<Path>... filterConds) {
		List<Path> files = findFolders(root, recursive ? Integer.MAX_VALUE : 1, filterConds);
		return files.size() != 1 ? null : files.get(0);
	}
	@SafeVarargs
	public static List<Path> findFolders(Path root, int maxDepth, Predicate<Path>... filterConds) {
		List<Predicate<Path>> conds = JkConvert.toList(filterConds);
		conds.add(0, Files::isDirectory);
		return findContent1(root, maxDepth, conds);
	}
	@SafeVarargs
	public static List<Path> findFolders(Path root, boolean recursive, Predicate<Path>... filterConds) {
		return findFolders(root, recursive ? Integer.MAX_VALUE : 1, filterConds);
	}
	@SafeVarargs
	public static List<Path> find(Path root, int maxDepth, Predicate<Path>... filterConds) {
		return findContent1(root, maxDepth, Arrays.asList(filterConds));
	}
	@SafeVarargs
	public static List<Path> find(Path root, boolean recursive, Predicate<Path>... filterConds) {
		return findContent1(root, recursive ? Integer.MAX_VALUE : 1, Arrays.asList(filterConds));
	}
	private static List<Path> findContent1(Path root, int maxDepth, List<Predicate<Path>> filterConds) {
		try {
			if (Files.notExists(root)) {
				return Collections.emptyList();
			}
			Stream<Path> stream = Files.find(root, maxDepth, (p, a) -> !JkFiles.areEquals(p, root));
			for (Predicate<Path> pred : filterConds) {
				stream = stream.filter(pred);
			}
			return stream.distinct().sorted().collect(Collectors.toList());

		} catch (IOException e) {
			throw new JkRuntimeException(e);
		}
	}


	/* REMOVE methods */
	public static boolean delete(Path toDelPath, Path... otherPaths) {
		boolean res = delete1(toDelPath);
		for (Path p : otherPaths) {
			res |= delete1(p);
		}
		return res;
	}
	public static boolean deleteContent(Path toDelPath, Path... otherPaths) {
		List<Path> plist = JkConvert.toList(otherPaths);
		plist.add(0, toDelPath);
		boolean res = false;
		for (Path p : plist) {
			List<Path> pchilds = JkFiles.find(p, true);
			for (Path pchild : pchilds) {
				res |= delete1(pchild);
			}
		}
		return res;
	}
	private static boolean delete1(Path toDelPath) {
		if(!Files.exists(toDelPath)) {
			return false;
		}

		try {
			if(Files.isRegularFile(toDelPath)) {
				Files.delete(toDelPath);

			} else {
				List<Path> files = JkFiles.find(toDelPath, true);
				Map<Boolean, List<Path>> filesMap = JkStreams.toMap(files, Files::isRegularFile);
				// Remove all files before
				for(Path file : filesMap.getOrDefault(true, Collections.emptyList())) {
					Files.delete(file);
				}
				// Remove all subfolder, beginning with leaves
				List<Path> subFolders = JkStreams.reverseOrder(filesMap.getOrDefault(false, Collections.emptyList()), Comparator.comparing(Path::getNameCount));
				for(Path subFolder : subFolders) {
					Files.delete(subFolder);
				}
				// Remove folder in input (pathToDel)
				Files.delete(toDelPath);
			}

			return true;

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}


	/* COPY-MOVE methods */
	public static void copy(Path sourcePath, Path targetPath) {
		copy1(sourcePath, targetPath, true, false);
	}
	public static void copy(Path sourcePath, Path targetPath, boolean overwrite) {
		copy1(sourcePath, targetPath, overwrite, false);
	}
	public static Path copySafe(Path sourcePath, Path targetPath) {
		return copy1(sourcePath, targetPath, false, true);
	}
	public static Path copyInFolder(Path sourcePath, Path targetFolder) {
		return copyInFolder(sourcePath, targetFolder, false);
	}
	public static Path copyInFolder(Path sourcePath, Path targetFolder, boolean safeCopy) {
		Path targetPath = targetFolder.resolve(sourcePath.getFileName());
		copy1(sourcePath, targetPath, true, safeCopy);
		return targetPath;
	}
	private static Path copy1(Path sourcePath, Path targetPath, boolean overwrite, boolean safePath) {
		try {
			if (!Files.exists(sourcePath)) {
				throw new FileNotFoundException(strf("Source file [%s] not exists!", sourcePath));
			}

			if (safePath) targetPath = safePath(targetPath);

			if (Files.exists(targetPath)) {
				if(!overwrite) {
					throw new FileAlreadyExistsException(strf("Unable to move [%s] to [%s]: target path already exists", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
				if(Files.isDirectory(targetPath) && Files.isRegularFile(sourcePath)) {
					throw new FileAlreadyExistsException(strf("Unable to move file [%s] to [%s]: target path is a directory", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
				if(Files.isDirectory(sourcePath) && Files.isRegularFile(targetPath)) {
					throw new FileAlreadyExistsException(strf("Unable to move folder [%s] to [%s]: target path is a file", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
			}

			delete(targetPath);

			if(Files.isRegularFile(sourcePath)) {
				Files.createDirectories(getParent(targetPath));
				Files.copy(sourcePath, targetPath);

			} else {
				Files.createDirectories(targetPath);
				Path absSource = sourcePath.toAbsolutePath();
				Path absTarget = targetPath.toAbsolutePath();
				List<Path> files = find(absSource, true);
				Map<Boolean, List<Path>> filesMap = JkStreams.toMap(files, Files::isRegularFile);
				// Create all folders before
				for(Path folder : filesMap.getOrDefault(false, Collections.emptyList())) {
					Path targetSubFolder = absTarget.resolve(absSource.relativize(folder));
					Files.createDirectories(targetSubFolder);
				}
				// Copy all files
				for(Path sourceFile : filesMap.getOrDefault(true, Collections.emptyList())) {
					Path targetFile = absTarget.resolve(absSource.relativize(sourceFile));
					Files.copy(sourceFile, targetFile);
				}
			}

			return targetPath;

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}

	public static Path move(Path sourcePath, Path targetPath) {
		return move1(sourcePath, targetPath, true, false);
	}
	public static Path move(Path sourcePath, Path targetPath, boolean overwrite) {
		return move1(sourcePath, targetPath, overwrite, false);
	}
	public static Path moveSafe(Path sourcePath, Path targetPath) {
		return move1(sourcePath, targetPath, false, true);
	}
	public static Path moveInFolder(Path sourcePath, Path targetFolder) {
		Path targetPath = targetFolder.resolve(sourcePath.getFileName());
		return move1(sourcePath, targetPath, true, false);
	}
	private static Path move1(Path sourcePath, Path targetPath, boolean overwrite, boolean safePath) {
		try {
			if (!Files.exists(sourcePath)) {
				throw new FileNotFoundException(strf("Source path [%s] not exists!", sourcePath.toAbsolutePath()));
			}

			if (safePath) targetPath = safePath(targetPath);

			if (Files.exists(targetPath)) {
				if(!overwrite) {
					throw new FileAlreadyExistsException(strf("Unable to move [%s] to [%s]: target path already exists", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
				if(Files.isDirectory(targetPath) && Files.isRegularFile(sourcePath)) {
					throw new FileAlreadyExistsException(strf("Unable to move file [%s] to [%s]: target path is a directory", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
				if(Files.isDirectory(sourcePath) && Files.isRegularFile(targetPath)) {
					throw new FileAlreadyExistsException(strf("Unable to move folder [%s] to [%s]: target path is a file", sourcePath.toAbsolutePath(), targetPath.toAbsolutePath()));
				}
			}

			delete(targetPath);
			Files.createDirectories(getParent(targetPath));
			Files.move(sourcePath, targetPath);

			return targetPath;

		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}


	/* MISCELLANEA methods */
	public static long sizeOf(Path path) {
		try {
			return Files.size(path);
		} catch (IOException e) {
			return -1L;
		}
	}    

	public static String getFileName(Path path) {
		String fn = path.toAbsolutePath().normalize().getFileName().toString();
		if(Files.isDirectory(path)) return fn;
		String ext = getExtension(path);
		return fn.replaceAll("\\." + ext + "$", "");
	}
	public static String getFileName(String uriString) {
		String fnRes = getResourceFilename(uriString);
		String ext = getExtension(fnRes, true);
		return fnRes.replaceAll(ext+"$", "");
	}

	public static String getExtension(Path path) {
		return getExtension(path, false);
	}
	public static String getExtension(Path path, boolean holdDot) {
		return getExtension(path.normalize().toString(), holdDot);
	}
	public static String getExtension(String uriString) {
		return getExtension(uriString, false);
	}
	public static String getExtension(String uriString, boolean holdDot) {
		String fnRes = getResourceFilename(uriString);
		int index = fnRes.lastIndexOf('.');
		boolean found = false;
		for(int i = index - 1; !found && i >= 0; i--) {
			found |= fnRes.charAt(i) != '.';
		}
		int adder = holdDot ? 0 : 1;
		return !found ? "" : fnRes.substring(index + adder);
	}
	private static String getResourceFilename(String str) {
		return str.replaceAll("[\\\\/]*$", "").replaceAll(".*[\\\\/]", "");
	}

	public static Path getParent(Path path) {
		Path parent = path.normalize().getParent();
		if(parent == null) {
			parent = path.toAbsolutePath().normalize().getParent();
		}
		return parent;
	}
	public static File getParent(File file) {
		Path parent = getParent(file.toPath());
		return parent == null ? null : parent.toFile();
	}

	public static Path safePath(String targetPath) {
		return safePath(Paths.get(targetPath));
	}
	public static Path safePath(Path targetPath) {
		if(!Files.exists(targetPath)) {
			return targetPath;
		}

		String fname = getFileName(targetPath);
		String fext = getExtension(targetPath);
		if(fext != null)	fext = "." + fext;
		else				fext = "";
		Path fparent = getParent(targetPath);

		Path newPath = null;
		for(int i = 1; newPath == null || Files.exists(newPath); i++) {
			newPath = fparent.resolve(strf("%s.%02d%s", fname, i, fext));
		}

		return newPath;
	}

	public static JkDateTime getLastModifiedTime(Path source) {
		try {
			FileTime ftime = Files.getLastModifiedTime(source);
			return JkDateTime.of(ftime.toMillis());
		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}
	public static void setLastModifiedTime(Path source, LocalDateTime ldt) {
		try {
			long totalMillis = JkDateTime.of(ldt).totalMillis();
			FileTime ftime = FileTime.fromMillis(totalMillis);
			Files.setLastModifiedTime(source, ftime);
		} catch (IOException ex) {
			throw new JkRuntimeException(ex);
		}
	}

	/* CONVERSIONS */
	public static Path[] toPaths(String[] source) {
		Path[] toRet = new Path[source.length];
		for(int i = 0; i < source.length; i++) {
			toRet[i] = Paths.get(source[i]);
		}
		return toRet;
	}
	public static Path toPath(URI sourceURI) {
		String path = sourceURI.getPath();
		return Paths.get(path.replaceAll("^/", ""));
	}
	public static Path toPath(URL sourceURL) {
		String path = sourceURL.getPath();
		return Paths.get(path.replaceAll("^/", ""));
	}

	public static String toURL(String source) {
		return toURL(Paths.get(source));
	}
	public static String toURL(Path source) {
		try {
			return source.toUri().toURL().toExternalForm();
		} catch (MalformedURLException e) {
			return null;
		}
	}


	/* TESTS */
	public static boolean areEquals(Path p1, Path p2) {
		if(p1 == null) return p2 == null;
		return p2 != null && p1.toAbsolutePath().normalize().equals(p2.toAbsolutePath().normalize());
	}

	public static int compare(Path p1, Path p2) {
		String s1 = p1.toAbsolutePath().normalize().toString();
		String s2 = p2.toAbsolutePath().normalize().toString();
		return StringUtils.compareIgnoreCase(s1, s2);
	}

	public static boolean containsPath(List<Path> source, Path toFind) {
		for(Path p : source) {
			if(areEquals(p, toFind)) {
				return true;
			}
		}
		return false;
	}

}
