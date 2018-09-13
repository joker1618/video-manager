package xxx.joker.apps.video.manager.jfx.controller;

import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.data.beans.Video;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoBuilder;
import xxx.joker.apps.video.manager.jfx.controller.videoplayer.JkVideoStage;
import xxx.joker.apps.video.manager.jfx.model.VideoModel;
import xxx.joker.apps.video.manager.jfx.model.VideoModelImpl;
import xxx.joker.apps.video.manager.main.SceneManager;
import xxx.joker.apps.video.manager.provider.StagePosProvider;
import xxx.joker.apps.video.manager.provider.VideoStagesPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiVideoPane extends BorderPane implements CloseablePane {

	private static final Logger logger = LoggerFactory.getLogger(MultiVideoPane.class);

	private List<JkVideoStage> stages;
	private List<Video> videos;
	private List<Video> toReproduce;
	private final Random random;

	private final VideoModel model = VideoModelImpl.getInstance();

	public MultiVideoPane() {
		setCenter(createPane());

		this.random = new Random(System.currentTimeMillis());
		this.videos = new ArrayList<>(model.getSelectedVideos());
		this.toReproduce = new ArrayList<>(videos);

		JkVideoBuilder pbuilder = new JkVideoBuilder();
		 pbuilder.setDecoratedStage(false);
		 pbuilder.setShowBorder(true);
		 pbuilder.setShowClose(true);
		 pbuilder.setCloseEvent(e -> {
		 	stages.forEach(Stage::close);
		 	SceneManager.displayHomepage();
		 });
		 pbuilder.setEventRightMouseClick(e -> stages.forEach(Stage::toFront));

		this.stages = new ArrayList<>();
		VideoStagesPosition stagesPosition = StagePosProvider.getStagesPosition(model.getPlayOptions().getMultiVideoName());
		for(int i = 0; i < stagesPosition.getNumStages(); i++) {
			List<Video> hlist = new ArrayList<>();
			hlist.add(getNextRandomVideo());

			pbuilder.setSupplierPrevious(() -> getPreviousRandomVideo(hlist));
			pbuilder.setSupplierNext(() -> {
				Video v = getNextRandomVideo();
				hlist.add(v);
				return v;
			});

			JkVideoStage stage = pbuilder.createStage();
			stage.playVideo(hlist.get(0));

			stages.add(stage);
		}

		stagesPosition.setStagesPosition(stages);
	}

	private Pane createPane() {
		Button btnToFront = new Button("TO FRONT");
		btnToFront.setOnAction(e -> stages.forEach(Stage::toFront));

		Button btnToBack = new Button("TO BACK");
		btnToBack.setOnAction(e -> stages.forEach(Stage::toBack));

		Button btnExit = new Button("EXIT");
		btnExit.setOnAction(e -> SceneManager.displayHomepage());

		VBox vbox = new VBox(btnToFront, btnToBack, btnExit);
		vbox.getStyleClass().add("container");

		getStylesheets().add(getClass().getResource("/css/MultiVideoPane.css").toExternalForm());

		return vbox;
	}

	private Video getNextRandomVideo() {
		synchronized (random) {
			if(toReproduce.isEmpty()) {
				toReproduce.addAll(videos);
			}

			int idx = random.nextInt(toReproduce.size());
			Video video = toReproduce.remove(idx);
			video.incrementPlayTimes();

			return video;
		}
	}

	private Video getPreviousRandomVideo(List<Video> hist) {
		synchronized (random) {
			if(hist.size() > 1) {
				hist.remove(hist.size() - 1);
			}

			Video video = hist.get(hist.size() - 1);
			video.incrementPlayTimes();

			return video;
		}
	}

	@Override
	public void closePane() {
		synchronized (random) {
			if(!stages.isEmpty()) {
				logger.debug("Closing {} video stages", stages.size());
				stages.forEach(JkVideoStage::close);
				stages.clear();
				videos.clear();
				toReproduce.clear();
			}
		}
	}
}
