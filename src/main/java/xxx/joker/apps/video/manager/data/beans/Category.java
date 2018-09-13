package xxx.joker.apps.video.manager.data.beans;

import org.apache.commons.lang3.StringUtils;
import xxx.joker.libs.javalibs.dao.csv.CsvEntity;
import xxx.joker.libs.javalibs.dao.csv.CsvField;

public class Category implements Comparable<Category>, CsvEntity {

	@CsvField(index = 0)
	private String name;

	public Category() {
	}

	public Category(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(Category o) {
		return StringUtils.compare(getPrimaryKey(), o.getPrimaryKey());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Category)) return false;
		Category category = (Category) o;
		return compareTo(category) == 0;
	}

	@Override
	public int hashCode() {
		return getPrimaryKey().hashCode();
	}

    @Override
    public String getPrimaryKey() {
        return name != null ? name.toLowerCase() : "";
    }

    @Override
    public String toString() {
	    return getPrimaryKey();
    }
}
