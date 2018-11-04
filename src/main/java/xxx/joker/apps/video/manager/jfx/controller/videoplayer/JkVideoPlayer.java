package xxx.joker.apps.video.manager.jfx.controller.videoplayer;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xxx.joker.apps.video.manager.model.entity.Video;
import xxx.joker.libs.core.datetime.JkTime;
import xxx.joker.libs.core.utils.JkFiles;

import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.function.Consumer;

public class JkVideoPlayer extends BorderPane {

	private static Logger logger = LoggerFactory.getLogger(JkVideoPlayer.class);
	
	private Video video;
	private PlayerConfig playerConfig;

	private final Pane topPane;

	private MediaView mediaView;

	private final Pane bottomPane;
	private Button btnPlay;
	private Button btnNext;
	private Label lblActualTime;
	private Slider sliderTime;
	private Slider sliderVolume;
	private Label lblVolume;

	private boolean isMediaTerminated;


	protected JkVideoPlayer(Video video, PlayerConfig playerConfig) {
		this.video = video;
		this.playerConfig = playerConfig;

		this.topPane = createTopPane();
		if(playerConfig.visibleHeading) {
			setTop(topPane);
		}

		setCenter(createMediaViewPane());

		this.bottomPane = createMediaBarPane();
		if(playerConfig.visiblePlayerBar) {
			setBottom(bottomPane);
		}

		if(playerConfig.isShowBorder()) {
			getStyleClass().add("borderedRoot");
		}
		getStylesheets().add(getClass().getResource("/css/JkVideoPlayer.css").toExternalForm());
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

	public PlayerConfig getPlayerConfig() {
		playerConfig.setVolume(mediaView.getMediaPlayer().getVolume());
		playerConfig.setVisibleHeading(getTop() != null);
		playerConfig.setVisiblePlayerBar(getBottom() != null);
		return playerConfig;
	}

	public Video getVideo() {
		return video;
	}

	private Pane createTopPane() {
		Label lblHeading = new Label(JkFiles.getFileName(video.getPath()));

		HBox headingBox = new HBox();
		headingBox.getStyleClass().add("headingBox");

		Pane fill1 = new Pane();
		Pane fill2 = new Pane();
		Arrays.asList(fill1, fill2).forEach(f -> HBox.setHgrow(f, Priority.ALWAYS));
		headingBox.getChildren().addAll(fill1, lblHeading, fill2);

		if(playerConfig.isShowCloseButton()) {
			Button btnClose = new Button("X");
			btnClose.setOnAction(playerConfig.getCloseEvent());
			headingBox.getChildren().add(btnClose);
		}

		return headingBox;
	}

	private Pane createMediaViewPane() {
		// Create media view
		mediaView = new MediaView();
		Media media = new Media(video.getURL());
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
				if(getBottom() == null) {
					double vpos = e.getY() / mvPane.getHeight();
					if(vpos < 0.8)	setTop(topPane);
					setBottom(bottomPane);
				} else {
					setTop(null);
					setBottom(null);
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
			if (video != null && mvPane.getWidth() > 0d && mvPane.getHeight() > 0d) {
				double videoFormat = (double) video.getWidth() / video.getHeight();
				double paneFormat = mvPane.getWidth() / mvPane.getHeight();
				double fitWidth = (paneFormat < videoFormat) ? mvPane.getWidth() : (mvPane.getHeight() * videoFormat);
				mediaView.setFitWidth(fitWidth);
			}
		};
	}

	private Pane createMediaBarPane() {
		HBox mediaBar = new HBox();
		mediaBar.getStyleClass().add("mediaBarPane");
		BorderPane.setAlignment(mediaBar, Pos.CENTER);

		// Play button
		btnPlay = new Button(">");
		mediaBar.getChildren().add(btnPlay);

		// Label for time progress
		lblActualTime = new Label("");

		// time slider
		sliderTime = new Slider();
		HBox.setHgrow(sliderTime, Priority.ALWAYS);
		sliderTime.setMinWidth(50);
		sliderTime.setMaxWidth(Double.MAX_VALUE);

		// Label for total time
		String totTime = video.getDuration().toStringElapsed(false, ChronoUnit.MINUTES);
		Label lblTotalTime = new Label(totTime);

		HBox hboxTime = new HBox(lblActualTime, sliderTime, lblTotalTime);
		hboxTime.getStyleClass().add("lessSpacingBox");
		HBox.setHgrow(hboxTime, Priority.ALWAYS);
		mediaBar.getChildren().add(hboxTime);
		
		// Previous and next buttons
		if(playerConfig.getNextAction() != null) {
			Button btnPrevious = new Button("<<");
			btnPrevious.setOnAction(playerConfig.getPreviousAction());
			btnNext = new Button(">>");
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

		initMediaBarBindings();

		return mediaBar;
	}

	private void initMediaBarBindings() {
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
				btnPlay.setText("||");
			} else {
				mediaPlayer.pause();
				btnPlay.setText(">");
			}
		});


		// MediaPlayer events
		mediaPlayer.setOnReady(() -> {
			logger.trace("player event: READY");
			updateValues();
		});
		mediaPlayer.setOnPlaying(() -> {
			logger.trace("player event: PLAYING");
			btnPlay.setText("||");
		});
		mediaPlayer.setOnPaused(() -> {
			logger.trace("player event: PAUSE");
			btnPlay.setText(">");
		});
		mediaPlayer.setOnEndOfMedia(() -> {
			logger.trace("player event: END OF MEDIA");
			isMediaTerminated = true;
			mediaPlayer.pause();
			btnPlay.setText(">");
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
                    JkTime of = JkTime.of((long) currentTime.toMillis());
                    lblActualTime.setText(of.toStringElapsed(false, ChronoUnit.MINUTES));
                    if (!sliderTime.isValueChanging()) {
                        Duration divided = currentTime.divide(video.getDuration().getTotalMillis());
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
		return video.toString();
	}

	static class PlayerConfig {
		private boolean decoratedStage;
		private boolean showBorder;
		private double volume = 1.0;
		private boolean visibleHeading;
		private boolean showCloseButton;
		private boolean visiblePlayerBar;
		private EventHandler<ActionEvent> previousAction;
		private EventHandler<ActionEvent> nextAction;
		private Consumer<MouseEvent> middleMouseClickEvent;
		private Consumer<MouseEvent> rightMouseClickEvent;
		private EventHandler<ActionEvent> closeEvent;

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
			conf.middleMouseClickEvent = middleMouseClickEvent;
			conf.rightMouseClickEvent = rightMouseClickEvent;
			conf.closeEvent = closeEvent;
			return conf;
		}

		public boolean isDecoratedStage() {
			return decoratedStage;
		}

		public void setDecoratedStage(boolean decoratedStage) {
			this.decoratedStage = decoratedStage;
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

		public boolean isShowCloseButton() {
			return showCloseButton;
		}

		public void setShowCloseButton(boolean showCloseButton) {
			this.showCloseButton = showCloseButton;
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
			middleMouseClickEvent.accept(event);
		}

		public void setMiddleMouseClickEvent(Consumer<MouseEvent> middleMouseClickEvent) {
			this.middleMouseClickEvent = middleMouseClickEvent;
		}

		public void consumeRightMouseClickEvent(MouseEvent event) {
			rightMouseClickEvent.accept(event);
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
