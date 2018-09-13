package xxx.joker.apps.video.manager.data.beans;

import xxx.joker.libs.javalibs.dao.csv.CsvEntity;
import xxx.joker.libs.javalibs.dao.csv.CsvField;
import xxx.joker.libs.javalibs.datetime.JkTime;
import xxx.joker.libs.javalibs.media.analysis.JkMediaAnalyzer;
import xxx.joker.libs.javalibs.media.analysis.JkVideoInfo;
import xxx.joker.libs.javalibs.utils.JkBytes;
import xxx.joker.libs.javalibs.utils.JkEncryption;
import xxx.joker.libs.javalibs.utils.JkFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

public class Video implements Comparable<Video>, CsvEntity {

	@CsvField(index = 0)
	private Path path;
	@CsvField(index = 1)
	private String md5;
	@CsvField(index = 2)
	private long size;
	@CsvField(index = 3)
	private int width;
	@CsvField(index = 4)
	private int height;
	@CsvField(index = 5)
	private long duration;
	@CsvField(index = 6)
	private Set<Category> categories;
	@CsvField(index = 7)
	private int playTimes;
	@CsvField(index = 8)
	private boolean toBeSplit;
	@CsvField(index = 9)
	private boolean cataloged;


	public Video() {
		this.categories = new TreeSet<>();
	}

	public static Video createFromPath(Path path) throws Exception {
		Video video = new Video();
		video.md5 = JkEncryption.getMD5(JkBytes.getBytes(path));
		video.path = path;
		video.size = Files.size(path);
		JkVideoInfo vinfo = JkMediaAnalyzer.analyzeVideo(path);
		video.width = vinfo.getWidth();
		video.height = vinfo.getHeight();
		video.duration = vinfo.getDuration();
		return video;
	}


    public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public String getURL() {
		return JkFiles.toUrlString(path);
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean isCataloged() {
		return cataloged;
	}

	public void setCataloged(boolean cataloged) {
		this.cataloged = cataloged;
	}

	public void setToBeSplit(boolean toBeSplit) {
		this.toBeSplit = toBeSplit;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public double getFormat() {
		return (double) width / height;
	}

	public JkTime getDuration() {
		return JkTime.of(duration);
	}

	public Set<Category> getCategories() {
		return categories;
	}

	public int getPlayTimes() {
		return playTimes;
	}

	public boolean isToBeSplit() {
		return toBeSplit;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setDuration(JkTime duration) {
		this.duration = duration.getTotalMillis();
	}

	public void addCategories(Collection<Category> categories) {
		this.categories.addAll(categories);
	}

	public void setPlayTimes(int playTimes) {
		this.playTimes = playTimes;
	}

	public void incrementPlayTimes() {
		this.playTimes++;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Override
	public int compareTo(Video o) {
		return path.toAbsolutePath().normalize().compareTo(o.path.toAbsolutePath().normalize());
	}

	@Override
	public String toString() {
		return path.toString();
	}

    @Override
    public String getPrimaryKey() {
        return getMd5();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Video video = (Video) o;
        return getPrimaryKey().equals(video.getPrimaryKey());
    }

    @Override
    public int hashCode() {
        return getPrimaryKey().hashCode();
    }
}
