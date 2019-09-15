package xxx.joker.apps.video.manager.jfx.model.beans;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;
import xxx.joker.apps.video.manager.datalayer.entities.Category;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.utils.JkStrings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class SortFilterOld extends ObjectBinding<Predicate<Video>> {

	private SimpleStringProperty videoName = new SimpleStringProperty("");
	private SimpleObjectProperty<Boolean> trigger = new SimpleObjectProperty<>(false);
	private SimpleObjectProperty<Boolean> cataloged = new SimpleObjectProperty<>();

	private Map<Category,SimpleObjectProperty<Boolean>> categoryMap = new HashMap<>();

	public SortFilterOld() {
		bind(videoName, cataloged, trigger);
		VideoModelImpl.getInstance().getCategories().forEach(cat -> setCategory(cat, null));
	}

	public void triggerSort() {
		trigger.setValue(!trigger.getValue());
	}

	public void setCataloged(Boolean cataloged) {
		this.cataloged.setValue(cataloged);
	}

	public void setCategory(Category category, Boolean radioValue) {
		if(!categoryMap.containsKey(category)) {
			SimpleObjectProperty<Boolean> sop = new SimpleObjectProperty<>();
			categoryMap.put(category, sop);
			bind(sop);
		}
		this.categoryMap.get(category).setValue(radioValue);
	}

    public SimpleStringProperty videoNameProperty() {
        return videoName;
    }

    public boolean testFilter(Video video) {
		List<String> filters = JkStrings.splitList(videoName.get(), "|");
		boolean resNameFilter = false;
		for (int i = 0; i < filters.size() && !resNameFilter; i++) {
			String filter = filters.get(i);
			boolean begin = filter.startsWith("^");
			boolean end = filter.endsWith("$");
			filter = filter.replaceAll("^\\^", "").replaceAll("\\$$", "").trim();
			if(!filter.isEmpty()) {
				String vtitle = video.getTitle();
				if (!begin && !end) {
					// only contains
					if (StringUtils.containsIgnoreCase(vtitle, filter)) {
						resNameFilter = true;
					}
				} else {
					if (begin && StringUtils.startsWithIgnoreCase(vtitle, filter)) {
						resNameFilter = true;
					}
					if (end && StringUtils.endsWithIgnoreCase(vtitle, filter)) {
						resNameFilter = true;
					}
				}
			}
		}
		if(StringUtils.isNotBlank(videoName.get()) && !resNameFilter) {
			return false;
		}

		if(cataloged.getValue() != null && ((cataloged.get() && video.getCategories().isEmpty()) || (!cataloged.get() && !video.getCategories().isEmpty()))) {
			return false;
		}
		for(Category cat : categoryMap.keySet()) {
			Boolean value = categoryMap.get(cat).getValue();
			if(value != null && value != video.getCategories().contains(cat)) {
				return false;
			}
		}

		return true;
	}

	@Override
	protected Predicate<Video> computeValue() {
		return this::testFilter;
	}
}
