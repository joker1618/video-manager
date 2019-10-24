package xxx.joker.apps.video.manager.jfx.fxview.videoplayer;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import xxx.joker.apps.video.manager.common.Config;
import xxx.joker.apps.video.manager.datalayer.entities.Video;
import xxx.joker.apps.video.manager.jfx.fxmodel.FxVideo;
import xxx.joker.apps.video.manager.jfx.fxview.managers.SnapshotManager;
import xxx.joker.apps.video.manager.jfx.fxview.provider.IconProvider;
import xxx.joker.libs.core.datetime.JkDuration;
import xxx.joker.libs.core.files.JkFiles;
import xxx.joker.libs.core.javafx.JfxUtil;

import java.nio.file.Path;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static xxx.joker.libs.core.javafx.JfxControls.createHBox;

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

	private IconProvider iconProvider;

	private SimpleBooleanProperty playerMinimal = new SimpleBooleanProperty(false);

	protected JfxVideoPlayer(FxVideo fxVideo, PlayerConfig playerConfig) {
		this.fxVideo = fxVideo;
		this.playerConfig = playerConfig;
		this.iconProvider = new IconProvider();

		setCenter(createMediaViewPane());

		this.topPane = createTopPane();
		if(playerConfig.visibleHeading) {
			setTop(topPane);
		}

		this.bottomPane = createMediaBarPane();
		if(playerConfig.visiblePlayerBar) {
			setBottom(bottomPane);
		}

		bottomProperty().addListener(obs -> playerConfig.runBottomPropertyListener());
		double minWidthPlayer = 285d;
		widthProperty().addListener((obs,o,n) -> playerMinimal.set(minWidthPlayer > n.doubleValue()));

		if(playerConfig.isShowBorder()) {
			getStyleClass().add("borderedRoot");
		}

		getStylesheets().add(getClass().getResource("/css/JfxVideoPlayer.css").toExternalForm());
	}

	public void play() {
		if(!isPlaying()) {
			mediaView.getMediaPlayer().play();
		}
	}
	public boolean isPlaying() {
		return mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING;
	}
	public void pause() {
		if(mediaView.getMediaPlayer().getStatus() != MediaPlayer.Status.PAUSED) {
			mediaView.getMediaPlayer().pause();
		}
	}

	public void seek(long milli) {
		mediaView.getMediaPlayer().seek(Duration.millis(milli));
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

	public void setShowTopBottom(boolean showTop, boolean showBottom) {
		if(getTop() == null && showTop) {
			setTop(topPane);
		} else if(getTop() != null && !showTop) {
			setTop(null);
		}
		if(getBottom() == null && showBottom) {
			setBottom(bottomPane);
		} else if(getBottom() != null && !showBottom) {
			setBottom(null);
		}
	}
	private Pane createTopPane() {
		this.lblHeading = new Label(fxVideo.getVideo().getTitle());

		HBox headingBox = new HBox();
		headingBox.getStyleClass().add("headingBox");

		HBox hBoxIcons = createHBox("boxIcons");
		double bsize = 30d;

		if(playerConfig.isVisibleBtnCamera()) {
			Button btnCamera = new Button();
			btnCamera.setGraphic(iconProvider.getIcon(IconProvider.CAMERA, bsize, false));
			btnCamera.setOnAction(e -> {
				new SnapshotManager().takeSnapAndAddToModel(this);
				playerConfig.runBtnCameraListener();
			});
			hBoxIcons.getChildren().add(btnCamera);
		}

		if(playerConfig.isVisibleBtnMark()) {
			Button btnMark = new Button();
			ImageView ivMarked = iconProvider.getIcon(fxVideo.getVideo().isMarked() ? IconProvider.MARKED_GREEN : IconProvider.UNMARKED, bsize, false);
			btnMark.setGraphic(ivMarked);
			btnMark.setOnAction(e -> {
				Video v = fxVideo.getVideo();
				v.setMarked(!v.isMarked());
				ivMarked.setImage(iconProvider.getIconImage(v.isMarked() ? IconProvider.MARKED_GREEN : IconProvider.UNMARKED));
				playerConfig.runBtnMarkRunnable();
			});
			hBoxIcons.getChildren().add(btnMark);
		}

		if(playerConfig.isVisibleBtnCamera() || playerConfig.isVisibleBtnMark()) {
			headingBox.getChildren().add(hBoxIcons);
		}

		Pane fill1 = new Pane();
		Pane fill2 = new Pane();
		Arrays.asList(fill1, fill2).forEach(f -> HBox.setHgrow(f, Priority.ALWAYS));
		headingBox.getChildren().addAll(fill1, lblHeading, fill2);

		if(playerConfig.isShowCloseButton()) {
			Button btnClose = new Button();
			btnClose.setGraphic(iconProvider.getIcon(IconProvider.CLOSE, bsize));
			btnClose.setOnAction(e -> {
				playerConfig.runCloseRunnable();
				JfxUtil.getStage(btnClose).close();
			});
			headingBox.getChildren().add(btnClose);
		}

		ObservableList<Node> children = headingBox.getChildren();
		int indexLblHeading = children.indexOf(lblHeading);
		playerMinimal.addListener((obs,o,n) -> {
			if(n)	children.remove(lblHeading);
			else	children.add(indexLblHeading, lblHeading);
		});

		return headingBox;
	}

	public void setPlayerCaption(String caption) {
		lblHeading.setText(caption);
	}

	public Pair<Path, JkDuration> takeVideoSnapshot(int snapSquareSize) {
		boolean playing = mediaView.getMediaPlayer().getStatus() == MediaPlayer.Status.PLAYING;
		if(playing) {
			mediaView.getMediaPlayer().pause();
		}
		Duration currentTime = mediaView.getMediaPlayer().getCurrentTime();
		JkDuration snapTime = JkDuration.of(currentTime);
		Path snapPath = Config.createSnapshotOutPath(fxVideo.getVideo(), snapTime);
		double fw = mediaView.getFitWidth();
		double fh = mediaView.getFitHeight();
		mediaView.setFitWidth(snapSquareSize);
		mediaView.setFitHeight(snapSquareSize);
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
						setShowTopBottom(vpos < 0.7, true);
					} else {
						setShowTopBottom(false, false);
					}
				} else if(playerConfig.getLeftMouseType() == PlayerConfig.LeftMouseType.PLAY) {
					btnPlay.fire();
				}
				if(playerConfig.getLeftMouseListener() != null) {
					playerConfig.getLeftMouseListener().accept(this, playerConfig.getLeftMouseType());
				}

			} else if(e.getButton() == MouseButton.MIDDLE) {
				playerConfig.consumeMiddleMouseClickEvent(e);

			} else if(e.getButton() == MouseButton.SECONDARY) {
				playerConfig.runRightMouseClickEvent(e);
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

		double btnSize = 40d;

		// Play button
		btnPlay = new Button();
		ImageView ivPlayPause = iconProvider.getIcon(IconProvider.PLAY, btnSize);
		btnPlay.setGraphic(ivPlayPause);
		mediaBar.getChildren().add(btnPlay);

		// Label for time progress
		lblActualTime = new Label("");

		// time slider
		sliderTime = new Slider();
		HBox.setHgrow(sliderTime, Priority.ALWAYS);
		sliderTime.setMinWidth(50);
		sliderTime.setMaxWidth(Double.MAX_VALUE);

		// Previous and next buttons
		List<Button> btnList = new ArrayList<>();
		if(playerConfig.getPreviousAction() != null) {
			Button btnPrevious = new Button();
			btnPrevious.setGraphic(iconProvider.getIcon(IconProvider.PREVIOUS, btnSize));
			btnPrevious.setOnAction(playerConfig.getPreviousAction());
			btnList.add(btnPrevious);
		}
		if(playerConfig.getNextAction() != null) {
			btnNext = new Button();
			btnNext.setGraphic(iconProvider.getIcon(IconProvider.NEXT, btnSize));
			btnNext.setOnAction(playerConfig.getNextAction());
			btnList.add(btnNext);
		}
		if(!btnList.isEmpty()) {
			mediaBar.getChildren().add(createHBox("lessSpacingBox", btnList));
		}

		List<Pane> panesNotMinimal = new ArrayList<>();

		// Label for total time
		Label lblTotalTime = new Label();
		lblTotalTime.getStyleClass().add("center-left");
		Video video = fxVideo.getVideo();
		if(video.getLength() != null) {
			lblTotalTime.setText(video.getLength().toStringElapsed(false, ChronoUnit.MINUTES));
		} else {
			ReadOnlyObjectProperty<Duration> totTimeProp = mediaView.getMediaPlayer().totalDurationProperty();
			ChangeListener<Duration> totEvent = (obs, o, n) -> {
				if(n != null && n.toMillis() > 0 && video.getLength() == null) {
					video.setLength(JkDuration.of(n));
					lblTotalTime.setText(video.getLength().toStringElapsed(false, ChronoUnit.MINUTES));
				}
			};
			totTimeProp.addListener(totEvent);
		}
//		Label lblTotalTime = new Label();
//		mediaView.getMediaPlayer().totalDurationProperty().addListener((obs,o,n) -> lblTotalTime.setText(JkDuration.of(n).toStringElapsed(false, ChronoUnit.MINUTES)));
		HBox hboxTime = createHBox("lessSpacingBox", lblActualTime, sliderTime, lblTotalTime);
		HBox.setHgrow(hboxTime, Priority.ALWAYS);
		panesNotMinimal.add(hboxTime);
//		mediaBar.getChildren().add(hboxTime);

		List<Button> seekButtons = getSeekButtons(btnSize);
		if(!seekButtons.isEmpty()) {
			HBox hbox = createHBox("lessSpacingBox");
			hbox.getChildren().setAll(seekButtons);
			panesNotMinimal.add(hbox);
//			mediaBar.getChildren().add(hbox);
		}

		// volume
		sliderVolume = new Slider();
		sliderVolume.setPrefWidth(100);
		sliderVolume.setMaxWidth(Region.USE_PREF_SIZE);
		sliderVolume.setMinWidth(30);
		lblVolume = new Label("");
		HBox hboxVol = createHBox("lessSpacingBox", sliderVolume, lblVolume);
//		mediaBar.getChildren().add(hboxVol);
		panesNotMinimal.add(hboxVol);

		mediaBar.getChildren().addAll(panesNotMinimal);
		playerMinimal.addListener((obs, o, n) -> {
			if(n)	mediaBar.getChildren().removeAll(panesNotMinimal);
			else	mediaBar.getChildren().addAll(panesNotMinimal);
		});

		initMediaBarBindings(ivPlayPause);

		return mediaBar;
	}
	
	private List<Button> getSeekButtons(double btnSize) {
		List<Button> seekButtons = new ArrayList<>();
		if(playerConfig.getBackward30Milli() != null) {
			Button btnBack30 = new Button();
			btnBack30.setGraphic(iconProvider.getIcon(IconProvider.BACKWARD_30, btnSize));
			btnBack30.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toSubtract = Duration.millis(playerConfig.getBackward30Milli());
				mp.seek(currentTime.subtract(toSubtract));
			});
			seekButtons.add(btnBack30);
		}
		if(playerConfig.getBackward10Milli() != null) {
			Button btnBack10 = new Button();
			btnBack10.setGraphic(iconProvider.getIcon(IconProvider.BACKWARD_10, btnSize));
			btnBack10.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toSubtract = Duration.millis(playerConfig.getBackward10Milli());
				mp.seek(currentTime.subtract(toSubtract));
			});
			seekButtons.add(btnBack10);
		}
		if(playerConfig.getBackward5Milli() != null) {
			Button btnBack5 = new Button();
			btnBack5.setGraphic(iconProvider.getIcon(IconProvider.BACKWARD_5, btnSize));
			btnBack5.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toSubtract = Duration.millis(playerConfig.getBackward5Milli());
				mp.seek(currentTime.subtract(toSubtract));
			});
			seekButtons.add(btnBack5);
		}
		if(playerConfig.getForward5Milli() != null) {
			Button btnFor5 = new Button();
			btnFor5.setGraphic(iconProvider.getIcon(IconProvider.FORWARD_5, btnSize));
			btnFor5.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toAdd = Duration.millis(playerConfig.getForward5Milli());
				mp.seek(currentTime.add(toAdd));
			});
			seekButtons.add(btnFor5);
		}
		if(playerConfig.getForward10Milli() != null) {
			Button btnFor10 = new Button();
			btnFor10.setGraphic(iconProvider.getIcon(IconProvider.FORWARD_10, btnSize));
			btnFor10.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toAdd = Duration.millis(playerConfig.getForward10Milli());
				mp.seek(currentTime.add(toAdd));
			});
			seekButtons.add(btnFor10);
		}
		if(playerConfig.getForward30Milli() != null) {
			Button btnFor30 = new Button();
			btnFor30.setGraphic(iconProvider.getIcon(IconProvider.FORWARD_30, btnSize));
			btnFor30.setOnAction(e -> {
				MediaPlayer mp = mediaView.getMediaPlayer();
				Duration currentTime = mp.getCurrentTime();
				Duration toAdd = Duration.millis(playerConfig.getForward30Milli());
				mp.seek(currentTime.add(toAdd));
			});
			seekButtons.add(btnFor30);
		}
		return seekButtons;
	}

	private void initMediaBarBindings(ImageView ivPlayPause) {
		MediaPlayer mediaPlayer = mediaView.getMediaPlayer();

		Image imgPlay = iconProvider.getIconImage(IconProvider.PLAY);
		Image imgPause = iconProvider.getIconImage(IconProvider.PAUSE);

		// play button
		btnPlay.setOnAction(event -> {
			logger.trace("button PLAY action");

			MediaPlayer.Status status = mediaPlayer.getStatus();
			logger.trace("player status: {},  terminated media: {}", status, isMediaTerminated);

			if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
				return;
			}

			boolean isSetPauseImage = false;
			if (isMediaTerminated) {
				mediaPlayer.seek(mediaPlayer.getStartTime());
				isMediaTerminated = false;
				status = MediaPlayer.Status.PAUSED;
				isSetPauseImage = true;
			}

			if (status == MediaPlayer.Status.PAUSED
					|| status == MediaPlayer.Status.READY
					|| status == MediaPlayer.Status.STOPPED) {
				// rewind the movie if we're sitting at the end
				mediaPlayer.play();
				if(isSetPauseImage) {
					ivPlayPause.setImage(imgPause);
				}
			} else {
				mediaPlayer.pause();
			}
		});

		// MediaPlayer events
		mediaPlayer.setOnReady(() -> {
			logger.trace("player event: READY");
			updateValues();
		});
		mediaPlayer.setOnPlaying(() -> {
			logger.trace("player event: PLAYING");
			ivPlayPause.setImage(imgPause);
		});
		mediaPlayer.setOnPaused(() -> {
			logger.trace("player event: PAUSE");
			ivPlayPause.setImage(imgPlay);
		});
		mediaPlayer.setOnEndOfMedia(() -> {
			logger.trace("player event: END OF MEDIA");
			isMediaTerminated = true;
			mediaPlayer.pause();
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
		private boolean visibleBtnCamera;
		private boolean visibleBtnMark;
		private EventHandler<ActionEvent> previousAction;
		private EventHandler<ActionEvent> nextAction;
		private LeftMouseType leftMouseType;
		private BiConsumer<JfxVideoPlayer, LeftMouseType> leftMouseListener;
		private Consumer<MouseEvent> middleMouseClickEvent;
		private Consumer<MouseEvent> rightMouseClickEvent;
		private Runnable closeRunnable;
		private Runnable btnCameraRunnable;
		private Runnable btnMarkRunnable;
		private Long backward5Milli;
		private Long backward10Milli;
		private Long backward30Milli;
		private Long forward5Milli;
		private Long forward10Milli;
		private Long forward30Milli;
		private Runnable bottomPropertyListener;

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
			conf.leftMouseListener = leftMouseListener;
			conf.middleMouseClickEvent = middleMouseClickEvent;
			conf.rightMouseClickEvent = rightMouseClickEvent;
			conf.closeRunnable = closeRunnable;
			conf.btnCameraRunnable = btnCameraRunnable;
			conf.btnMarkRunnable = btnMarkRunnable;
			conf.visibleBtnCamera = visibleBtnCamera;
			conf.visibleBtnMark = visibleBtnMark;
			conf.backward5Milli = backward5Milli;
			conf.backward10Milli = backward10Milli;
			conf.backward30Milli = backward30Milli;
			conf.forward5Milli = forward5Milli;
			conf.forward10Milli = forward10Milli;
			conf.forward30Milli = forward30Milli;
			conf.bottomPropertyListener = bottomPropertyListener;
			return conf;
		}

		public void runBottomPropertyListener() {
			if(bottomPropertyListener != null){
				bottomPropertyListener.run();
			}
		}

		public void setBottomPropertyListener(Runnable bottomPropertyListener) {
			this.bottomPropertyListener = bottomPropertyListener;
		}

		public void runBtnCameraListener() {
			if(btnCameraRunnable != null) {
				btnCameraRunnable.run();
			}
		}

		public void runBtnMarkRunnable() {
			if(btnMarkRunnable != null) {
				btnMarkRunnable.run();
			}
		}

		public void setBtnMarkRunnable(Runnable btnMarkRunnable) {
			this.btnMarkRunnable = btnMarkRunnable;
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

		public BiConsumer<JfxVideoPlayer, LeftMouseType> getLeftMouseListener() {
			return leftMouseListener;
		}

		public void setLeftMouseListener(BiConsumer<JfxVideoPlayer, LeftMouseType> leftMouseListener) {
			this.leftMouseListener = leftMouseListener;
		}

		public boolean isShowCloseButton() {
			return showCloseButton;
		}

		public void setShowCloseButton(boolean showCloseButton) {
			this.showCloseButton = showCloseButton;
		}

		public Long getBackward5Milli() {
			return backward5Milli;
		}

		public void setBackward5Milli(Long backward5Milli) {
			this.backward5Milli = backward5Milli;
		}

		public Long getBackward10Milli() {
			return backward10Milli;
		}

		public void setBackward10Milli(Long backward10Milli) {
			this.backward10Milli = backward10Milli;
		}

		public Long getBackward30Milli() {
			return backward30Milli;
		}

		public void setBackward30Milli(Long backward30Milli) {
			this.backward30Milli = backward30Milli;
		}

		public Long getForward5Milli() {
			return forward5Milli;
		}

		public void setForward5Milli(Long forward5Milli) {
			this.forward5Milli = forward5Milli;
		}

		public Long getForward10Milli() {
			return forward10Milli;
		}

		public void setForward10Milli(Long forward10Milli) {
			this.forward10Milli = forward10Milli;
		}

		public Long getForward30Milli() {
			return forward30Milli;
		}

		public void setForward30Milli(Long forward30Milli) {
			this.forward30Milli = forward30Milli;
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

		public boolean isVisibleBtnCamera() {
			return visibleBtnCamera;
		}

		public void setVisibleBtnCamera(boolean visibleBtnCamera) {
			this.visibleBtnCamera = visibleBtnCamera;
		}

		public boolean isVisibleBtnMark() {
			return visibleBtnMark;
		}

		public void setVisibleBtnMark(boolean visibleBtnMark) {
			this.visibleBtnMark = visibleBtnMark;
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

		public void runRightMouseClickEvent(MouseEvent event) {
			if(rightMouseClickEvent != null) {
				rightMouseClickEvent.accept(event);
			}
		}

		public void setRightMouseClickEvent(Consumer<MouseEvent> rightMouseClickEvent) {
			this.rightMouseClickEvent = rightMouseClickEvent;
		}

		public void runCloseRunnable() {
			if(closeRunnable != null) {
				closeRunnable.run();
			}
		}

		public void setCloseRunnable(Runnable closeRunnable) {
			this.closeRunnable = closeRunnable;
		}
	}
}
