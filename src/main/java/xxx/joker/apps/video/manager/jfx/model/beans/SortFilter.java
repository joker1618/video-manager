package xxx.joker.apps.video.manager.jfx.model.beans;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.libs.javalibs.utils.JkFiles;
import xxx.joker.libs.javalibs.utils.JkStrings;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SortFilter extends ObjectBinding<Predicate<Video>> {

	private SimpleStringProperty videoName = new SimpleStringProperty("");
	private SimpleObjectProperty<Boolean> trigger = new SimpleObjectProperty<>(false);
	private SimpleObjectProperty<Boolean> cataloged = new SimpleObjectProperty<>();
	private SimpleObjectProperty<Boolean> toBeSplit = new SimpleObjectProperty<>();

	private Map<Category,SimpleObjectProperty<Boolean>> categoryMap = new HashMap<>();

	public SortFilter() {
		bind(videoName, cataloged, toBeSplit, trigger);
		VideoModelImpl.getInstance().getCategories().forEach(cat -> setCategory(cat, null));
	}

	public void triggerSort() {
		trigger.setValue(!trigger.getValue());
	}

	public void setCataloged(Boolean cataloged) {
		this.cataloged.setValue(cataloged);
	}

	public void setToBeSplit(Boolean toBeSplit) {
		this.toBeSplit.setValue(toBeSplit);
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
        String nameFilter = videoName.get();
        boolean begin = nameFilter.startsWith("^");
        boolean end = nameFilter.startsWith("$");
        nameFilter = nameFilter.replaceAll("^\\^", "").replaceAll("\\$$", "").trim();
        if(!nameFilter.isEmpty()) {
            String vtitle = JkFiles.getFileName(video.getPath());
            if (!begin && !end) {
                // only contains
                if (!StringUtils.containsIgnoreCase(vtitle, nameFilter)) {
                    return false;
                }
            } else {
                if (begin && !StringUtils.startsWithIgnoreCase(vtitle, nameFilter)) {
                    return false;
                }
                if (end && !StringUtils.endsWithIgnoreCase(vtitle, nameFilter)) {
                    return false;
                }
            }
        }

        if(cataloged.getValue() != null && cataloged.get() != video.isCataloged()) {
			return false;
		}
		if(toBeSplit.getValue() != null && toBeSplit.get() != video.isToBeSplit()) {
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
