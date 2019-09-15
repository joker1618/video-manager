package xxx.joker.apps.video.manager.fxlayer.fxview.videoplayer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.commonOK.Config;
import xxx.joker.apps.video.manager.fxlayer.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.fxlayer.fxview.builders.SnapshotManager;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.javafx.JfxUtil;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.function.Consumer;

import static xxx.joker.libs.core.javafx.JfxControls.createImageView;
import static xxx.joker.libs.core.utils.JkStrings.strf;

public class JfxVideoPlayer extends BorderPane {

	private static Logger logger = LoggerFactory.getLogger(JfxVideoPlayer.class);

	private Label lblHeading;
	private FxVideo fxVideo;
	private PlayerConfig playerConfig;

	private final Pane topPane;

	protected MediaView mediaView;

	private final Pane bottomPane;
	private Button btnPlay;
	private Button btnNext;
	private Label lblActualTime;
	private Slider sliderTime;
	private Slider sliderVolume;
	private Label lblVolume;

	private boolean isMediaTerminated;


	protected JfxVideoPlayer(FxVideo fxVideo, PlayerConfig playerConfig) {
		this.fxVideo = fxVideo;
		this.playerConfig = playerConfig;

		setCenter(createMediaViewPane());

		this.topPane = createTopPane();
		if(playerConfig.visibleHeading) {
			setTop(topPane);
		}

		this.bottomPane = createMediaBarPane();
		if(playerConfig.visiblePlayerBar) {
			setBottom(bottomPane);
		}

		if(playerConfig.isShowBorder()) {
			getStyleClass().add("borderedRoot");
		}

		getStylesheets().add(getClass().getResource("/css_legacy/JkVideoPlayer.css").toExternalForm());
	}

	public void play() {
		if(mediaView.getMediaPlayer().getStatus() != MediaPlayer.Status.PLAYING) {
			mediaView.getMediaPlayer().play();
		}
	}

	public void closePlayer() {
		MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
		if(mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
			logger.debug("close video player");
			mediaPlayer.stop();
			mediaPlayer.dispose();
		}
	}

	public MediaView getMediaView() {
		return mediaView;
	}

	protected PlayerConfig getPlayerConfig() {
		playerConfig.setVolume(mediaView.getMediaPlayer().getVolume());
		playerConfig.setVisibleHeading(getTop() != null);
		playerConfig.setVisiblePlayerBar(getBottom() != null);
		return playerConfig;
	}

	public FxVideo getFxVideo() {
		return fxVideo;
	}

	private Pane createTopPane() {
		this.lblHeading = new Label(fxVideo.getVideo().getTitle());

		HBox headingBox = new HBox();
		headingBox.getStyleClass().add("headingBox");

//		AtomicBoolean okSnap = new AtomicBoolean(!VideoModelImpl.getInstance().findSnapshots(videoWrapper.getVideo()).isEmpty());
//		URL url = getClass().getResource(strf("/icons/camera-{}.png", okSnap.get() ? "green" : "red"));
		URL url = getClass().getResource(strf("/icons/camera.png"));
		Image iconCamera = new Image(url.toExternalForm());
		ImageView imageView = new ImageView(iconCamera);
		imageView.setPreserveRatio(false);
		imageView.setFitWidth(30);
		imageView.setFitHeight(30);

		Button btnCamera = new Button();
		btnCamera.setGraphic(imageView);
//		int snapSize = 500;
		btnCamera.setOnAction(e -> {
//			Pair<Path, Long> pair = takeVideoSnapshot(snapSize);
			new SnapshotManager().takeSnapAndAddToModel(this);
			playerConfig.runBtnCameraListener();
		});

		HBox hBoxCamera = new HBox(btnCamera);
		hBoxCamera.getStyleClass().add("boxSnapshot");
		headingBox.getChildren().add(hBoxCamera);

		Pane fill1 = new Pane();
		Pane fill2 = new Pane();
		Arrays.asList(fill1, fill2).forEach(f -> HBox.setHgrow(f, Priority.ALWAYS));
		headingBox.getChildren().addAll(fill1, lblHeading, fill2);

		if(playerConfig.isShowCloseButton()) {
//			Button btnClose = new Button("X");
			Button btnClose = new Button();
			btnClose.setGraphic(createImageView(Paths.get("C:\\Users\\fbarbano\\Desktop\\icons\\close.png"), 30, 30));
			btnClose.setOnAction(playerConfig.getCloseEvent());
			headingBox.getChildren().add(btnClose);
		}

		return headingBox;
	}

	public void setPlayerCaption(String caption) {
		lblHeading.setText(caption);
	}

	public Pair<Path, JkDuration> takeVideoSnapshot(int snapSize) {
		boolean playing = mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING;
		if(playing) {
			mediaView.getMediaPlayer().pause();
		}
		Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
		JkDuration snapTime = JkDuration.of(currentTime);
		Path snapPath = Config.createSnapshotOutPath(fxVideo.getVideo(), snapTime);
		double fw = mediaView.getFitWidth();
		double fh = mediaView.getFitHeight();
		mediaView.setFitWidth(snapSize);
		mediaView.setFitHeight(snapSize);
		JfxUtil.takeSnapshot(mediaView, snapPath);
		mediaView.setFitWidth(fw);
		mediaView.setFitHeight(fh);
		logger.info("Snapshot taken at {} for {}", snapTime.toStringElapsed(true), fxVideo.getVideo().getTitle());
		if(playing) {
			mediaView.getMediaPlayer().play();
		}
		return Pair.of(snapPath, snapTime);
	}

	private Pane createMediaViewPane() {
		// Create media view
		mediaView = new MediaView();
		Media media = new Media(JkFiles.toURL(fxVideo.getPath()));
		MediaPlayer mediaPlayer = new MediaPlayer(media);
		mediaPlayer.setAutoPlay(false);
		mediaPlayer.setCycleCount(1);
		mediaPlayer.setVolume(playerConfig.getVolume());
		mediaView.setMediaPlayer(mediaPlayer);

		// Create container pane
		Pane mvPane = new Pane() {};
		mvPane.getStyleClass().add("mediaViewPane");
		ChangeListener<Number> fitListener = getMediaViewFitListener(mvPane);
		mvPane.widthProperty().addListener(fitListener);
		mvPane.heightProperty().addListener(fitListener);

		// Create HBox (needed to center the media view)
		HBox hbox = new HBox(this.mediaView);
		hbox.prefWidthProperty().bind(mvPane.widthProperty());
		hbox.prefHeightProperty().bind(mvPane.heightProperty());
		hbox.setAlignment(Pos.CENTER);
		mvPane.getChildren().add(hbox);

		mvPane.setOnMouseClicked(e -> {
			if(e.getButton() == MouseButton.PRIMARY) {
				if(playerConfig.getLeftMouseType() == PlayerConfig.LeftMouseType.SHOW_HIDE) {
					if(getBottom() == null) {
						double vpos = e.getY() / mvPane.getHeight();
						if(vpos < 0.7)	setTop(topPane);
						setBottom(bottomPane);
					} else {
						setTop(null);
						setBottom(null);
					}
				} else {
					btnPlay.fire();
				}

			} else if(e.getButton() == MouseButton.MIDDLE) {
				playerConfig.consumeMiddleMouseClickEvent(e);

			} else if(e.getButton() == MouseButton.SECONDARY) {
				playerConfig.consumeRightMouseClickEvent(e);
			}
		});

		return mvPane;
	}
	private ChangeListener<Number> getMediaViewFitListener(Pane mvPane) {
		return (obs, old, nez) -> {
			if (mvPane.getWidth() > 0d && mvPane.getHeight() > 0d) {
				mediaView.setFitWidth(mvPane.getWidth());
				mediaView.setFitHeight(mvPane.getHeight());
//				logger.debug("mv fit: {}x{}", mvPane.getWidth(), mvPane.getHeight());
			}
		};
	}

	private Pane createMediaBarPane() {
		HBox mediaBar = new HBox();
		mediaBar.getStyleClass().add("mediaBarPane");
		BorderPane.setAlignment(mediaBar, Pos.CENTER);

		int btnSize = 40;

		// Play button
//		btnPlay = new Button(">");
		btnPlay = new Button();
		Image imgPlay = new Image(getClass().getResource("/icons/play.png").toExternalForm());
		ImageView ivPlayPause = createImageView(imgPlay, btnSize, btnSize);
		btnPlay.setGraphic(ivPlayPause);
		mediaBar.getChildren().add(btnPlay);

		// Label for time progress
		lblActualTime = new Label("");

		// time slider
		sliderTime = new Slider();
		HBox.setHgrow(sliderTime, Priority.ALWAYS);
		sliderTime.setMinWidth(50);
		sliderTime.setMaxWidth(Double.MAX_VALUE);

		// Label for total time
//		String totTime = videoWrapper.getVideo().getLength().toStringElapsed(false, ChronoUnit.MINUTES);
		Label lblTotalTime = new Label();
		mediaView.getMediaPlayer().totalDurationProperty().addListener((obs,o,n) -> lblTotalTime.setText(JkDuration.of(n).toStringElapsed(false, ChronoUnit.MINUTES)));
		HBox hboxTime = new HBox(lblActualTime, sliderTime, lblTotalTime);
		hboxTime.getStyleClass().add("lessSpacingBox");
		HBox.setHgrow(hboxTime, Priority.ALWAYS);
		mediaBar.getChildren().add(hboxTime);
		
		// Previous and next buttons
		if(playerConfig.getNextAction() != null) {
//			Button btnPrevious = new Button("<<");
			Button btnPrevious = new Button();
			btnPrevious.setGraphic(createImageView(new Image(getClass().getResource("/icons/previous.png").toExternalForm()), btnSize, btnSize));
			btnPrevious.setOnAction(playerConfig.getPreviousAction());
//			btnNext = new Button(">>");
			btnNext = new Button();
			btnNext.setGraphic(createImageView(new Image(getClass().getResource("/icons/next.png").toExternalForm()), btnSize, btnSize));
			btnNext.setOnAction(playerConfig.getNextAction());
			HBox hboxAdditional = new HBox(btnPrevious, btnNext);
			hboxAdditional.getStyleClass().add("lessSpacingBox");
			mediaBar.getChildren().add(hboxAdditional);
		}

		// volume slider
		sliderVolume = new Slider();
		sliderVolume.setPrefWidth(100);
		sliderVolume.setMaxWidth(Region.USE_PREF_SIZE);
		sliderVolume.setMinWidth(30);

		// label for volume %
		lblVolume = new Label("");

		HBox hboxVol = new HBox(sliderVolume, lblVolume);
		hboxVol.getStyleClass().add("lessSpacingBox");
		mediaBar.getChildren().add(hboxVol);

		initMediaBarBindings(ivPlayPause);

		return mediaBar;
	}

	private void initMediaBarBindings(ImageView ivPlayPause) {
		MediaPlayer mediaPlayer = mediaView.getMediaPlayer();

		// play button
		btnPlay.setOnAction(event -> {
			logger.trace("button PLAY action");

			MediaPlayer.Status status = mediaPlayer.getStatus();
			logger.trace("player status:{},  terminated media: {}", status, isMediaTerminated);

			if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
				return;
			}

			if (isMediaTerminated) {
				mediaPlayer.seek(mediaPlayer.getStartTime());
				isMediaTerminated = false;
				status = MediaPlayer.Status.PAUSED;
			}

			if (status == MediaPlayer.Status.PAUSED
					|| status == MediaPlayer.Status.READY
					|| status == MediaPlayer.Status.STOPPED) {
				// rewind the movie if we're sitting at the end
				mediaPlayer.play();
//				btnPlay.setText("||");
			} else {
				mediaPlayer.pause();
//				btnPlay.setText(">");
			}
		});


		// MediaPlayer events
		mediaPlayer.setOnReady(() -> {
			logger.trace("player event: READY");
			updateValues();
		});
		Image imgPlay = new Image(getClass().getResource("/icons/play.png").toExternalForm());
		Image imgPause = new Image(getClass().getResource("/icons/pause.png").toExternalForm());
		mediaPlayer.setOnPlaying(() -> {
			logger.trace("player event: PLAYING");
//			btnPlay.setText("||");
			ivPlayPause.setImage(imgPause);
		});
		mediaPlayer.setOnPaused(() -> {
			logger.trace("player event: PAUSE");
//			btnPlay.setText(">");
			ivPlayPause.setImage(imgPlay);
		});
		mediaPlayer.setOnEndOfMedia(() -> {
			logger.trace("player event: END OF MEDIA");
			isMediaTerminated = true;
			mediaPlayer.pause();
//			btnPlay.setText(">");
			ivPlayPause.setImage(imgPlay);
			if(btnNext != null) {
				btnNext.fire();
			}
		});
		mediaPlayer.currentTimeProperty().addListener(ov -> updateValues());

		// time slider
		sliderTime.setOnMouseClicked(event -> {
			sliderTime.setValueChanging(true);
			double value = (event.getX() / sliderTime.getWidth()) * sliderTime.getMax();
			sliderTime.setValue(value);
			sliderTime.setValueChanging(false);
		});
		sliderTime.valueProperty().addListener(ov -> {
			if(sliderTime.isValueChanging()) {
				Duration seek = mediaPlayer.getMedia().getDuration().multiply(sliderTime.getValue() / 100.0);
				mediaPlayer.seek(seek);
			}
		});
		
		// volume slider
		sliderVolume.setOnMouseClicked(event -> {
			sliderVolume.setValueChanging(true);
			double value = (event.getX() / sliderVolume.getWidth()) * sliderVolume.getMax();
			sliderVolume.setValue(value);
			sliderVolume.setValueChanging(false);
		});
		sliderVolume.valueProperty().addListener(ov -> {
			if (sliderVolume.isValueChanging()) {
				mediaPlayer.setVolume(sliderVolume.getValue() / 100.0);
				lblVolume.setText(((int) sliderVolume.getValue()) + "%");
			}
		});
	}

	private void updateValues() {
		Platform.runLater(
			() -> {
				MediaPlayer mediaPlayer = mediaView.getMediaPlayer();
				if(mediaPlayer != null) {
					Duration currentTime = mediaPlayer.getCurrentTime();
					Duration totTime = mediaPlayer.getTotalDuration();
                    JkDuration of = JkDuration.of((long) currentTime.toMillis());
                    lblActualTime.setText(of.toStringElapsed(false, ChronoUnit.MINUTES));
                    if (!sliderTime.isValueChanging()) {
                        Duration divided = currentTime.divide(totTime.toMillis());
                        sliderTime.setValue(divided.toMillis() * 100.0);
                    }
                    if (!sliderVolume.isValueChanging()) {
                        sliderVolume.setValue((int) Math.round(mediaPlayer.getVolume() * 100.0));
                        lblVolume.setText(((int) sliderVolume.getValue()) + "%");
                    }
                }
			}
		);
	}

	@Override
	public String toString() {
		return fxVideo.toString();
	}


	public static class PlayerConfig {
		private boolean decoratedStage;
		private boolean showBorder;
		private double volume = 1.0;
		private boolean showCloseButton;
		private boolean visibleHeading;
		private boolean visiblePlayerBar;
		private EventHandler<ActionEvent> previousAction;
		private EventHandler<ActionEvent> nextAction;
		private LeftMouseType leftMouseType;
		private Consumer<MouseEvent> middleMouseClickEvent;
		private Consumer<MouseEvent> rightMouseClickEvent;
		private EventHandler<ActionEvent> closeEvent;
		private Runnable btnCameraRunnable;

		public enum LeftMouseType { PLAY, SHOW_HIDE }

		public PlayerConfig cloneConfigs() {
			PlayerConfig conf = new PlayerConfig();
			conf.decoratedStage = decoratedStage;
			conf.showBorder = showBorder;
			conf.volume = volume;
			conf.visibleHeading = visibleHeading;
			conf.showCloseButton = showCloseButton;
			conf.visiblePlayerBar = visiblePlayerBar;
			conf.previousAction = previousAction;
			conf.nextAction = nextAction;
			conf.leftMouseType = leftMouseType;
			conf.middleMouseClickEvent = middleMouseClickEvent;
			conf.rightMouseClickEvent = rightMouseClickEvent;
			conf.closeEvent = closeEvent;
			conf.btnCameraRunnable = btnCameraRunnable;
			return conf;
		}

		public void runBtnCameraListener() {
			btnCameraRunnable.run();
		}

		public void setBtnCameraRunnable(Runnable btnCameraRunnable) {
			this.btnCameraRunnable = btnCameraRunnable;
		}

		public boolean isDecoratedStage() {
			return decoratedStage;
		}

		public void setDecoratedStage(boolean decoratedStage) {
			this.decoratedStage = decoratedStage;
		}

		public boolean isShowCloseButton() {
			return showCloseButton;
		}

		public void setShowCloseButton(boolean showCloseButton) {
			this.showCloseButton = showCloseButton;
		}

		public boolean isShowBorder() {
			return showBorder;
		}

		public void setShowBorder(boolean showBorder) {
			this.showBorder = showBorder;
		}

		public double getVolume() {
			return volume;
		}

		public void setVolume(double volume) {
			this.volume = volume;
		}

		public boolean isVisibleHeading() {
			return visibleHeading;
		}

		public void setVisibleHeading(boolean visibleHeading) {
			this.visibleHeading = visibleHeading;
		}

		public void setLeftMouseType(LeftMouseType leftMouseType) {
			this.leftMouseType = leftMouseType;
		}

		public boolean isVisiblePlayerBar() {
			return visiblePlayerBar;
		}

		public void setVisiblePlayerBar(boolean visiblePlayerBar) {
			this.visiblePlayerBar = visiblePlayerBar;
		}

		public EventHandler<ActionEvent> getPreviousAction() {
			return previousAction;
		}

		public void setPreviousAction(EventHandler<ActionEvent> previousAction) {
			this.previousAction = previousAction;
		}

		public EventHandler<ActionEvent> getNextAction() {
			return nextAction;
		}

		public void setNextAction(EventHandler<ActionEvent> nextAction) {
			this.nextAction = nextAction;
		}

		public void consumeMiddleMouseClickEvent(MouseEvent event) {
			if(middleMouseClickEvent != null) {
				middleMouseClickEvent.accept(event);
			}
		}

		public void setMiddleMouseClickEvent(Consumer<MouseEvent> middleMouseClickEvent) {
			this.middleMouseClickEvent = middleMouseClickEvent;
		}

		public LeftMouseType getLeftMouseType() {
			return leftMouseType;
		}

		public void consumeRightMouseClickEvent(MouseEvent event) {
			if(rightMouseClickEvent != null) {
				rightMouseClickEvent.accept(event);
			}
		}

		public void setRightMouseClickEvent(Consumer<MouseEvent> rightMouseClickEvent) {
			this.rightMouseClickEvent = rightMouseClickEvent;
		}

		public EventHandler<ActionEvent> getCloseEvent() {
			return closeEvent;
		}

		public void setCloseEvent(EventHandler<ActionEvent> closeEvent) {
			this.closeEvent = closeEvent;
		}
	}
}
