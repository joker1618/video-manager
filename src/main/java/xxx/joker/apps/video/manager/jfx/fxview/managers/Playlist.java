package xxx.joker.apps.video.manager.jfx.fxview.managers;

import xxx.joker.apps.video.manager.jfx.fxmodel.FxVideo;

import java.util.*;

public class Playlist {

    private List<FxVideo> videoList;
    private final Random random;
    private List<FxVideo> toPlay;
    private List<FxVideo> playHistoryList;
    private Map<Integer, List<FxVideo>> playHistory;

    public Playlist(List<FxVideo> videoList) {
        this.videoList = videoList;
        this.random = new Random(System.currentTimeMillis());
        this.toPlay = new ArrayList<>(videoList);
        this.playHistoryList = new ArrayList<>();
        this.playHistory = new HashMap<>();
    }

    public synchronized FxVideo nextVideo() {
        if(toPlay.isEmpty()) {
            toPlay.addAll(videoList);
        }
        int index = random.nextInt(toPlay.size());
        FxVideo fxVideo = toPlay.remove(index);
        playHistoryList.add(fxVideo);
        return fxVideo;
    }

    public synchronized FxVideo nextVideo(Integer stageId) {
        if(toPlay.isEmpty()) {
            toPlay.addAll(videoList);
        }
        int index = random.nextInt(toPlay.size());
        FxVideo fxVideo = toPlay.remove(index);
        playHistory.putIfAbsent(stageId, new ArrayList<>());
        playHistory.get(stageId).add(fxVideo);
        return fxVideo;
    }

    public synchronized FxVideo previousVideo(Integer stageId) {
        List<FxVideo> histList = playHistory.getOrDefault(stageId, Collections.emptyList());
        if(histList.isEmpty())   return null;
        if(histList.size() == 1) return histList.get(0);
        histList.remove(histList.size() - 1);
        return histList.get(histList.size() - 1);
    }

}
