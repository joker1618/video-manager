package xxx.joker.apps.video.manager.jfx.model.beans;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.SimpleObjectProperty;
import xxx.joker.apps.video.manager.model.entity.Category;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class SortFilter extends ObjectBinding<Predicate<Video>> {

	private SimpleObjectProperty<Boolean> trigger = new SimpleObjectProperty<>(false);
	private SimpleObjectProperty<Boolean> cataloged = new SimpleObjectProperty<>();
	private SimpleObjectProperty<Boolean> toBeSplit = new SimpleObjectProperty<>();

	private Map<Category,SimpleObjectProperty<Boolean>> categoryMap = new HashMap<>();

	public SortFilter() {
		bind(cataloged, toBeSplit, trigger);
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

	public boolean testFilter(Video video) {
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
