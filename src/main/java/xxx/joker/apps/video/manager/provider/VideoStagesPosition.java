package xxx.joker.apps.video.manager.provider;

import javafx.stage.Screen;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class VideoStagesPosition {

	private String name;
	private int nrow;
	private int ncol;
	private List<VPos> videoPosList;


	public VideoStagesPosition(String name) {
		this.name = name;
		this.videoPosList = new ArrayList<>();
	}

	public VideoStagesPosition(String name, int nrow, int ncol) {
		this.name = name;
		this.nrow = nrow;
		this.ncol = ncol;
		this.videoPosList = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public int getNrow() {
		return nrow;
	}

	public int getNcol() {
		return ncol;
	}

	public int getNumStages() {
		return videoPosList.size();
	}

	public void addVideoPos(int row, int col, int rspan, int cspan) {
		videoPosList.add(new VPos(row, col, rspan, cspan));
	}

	public static class VPos {
		private int idxRow, idxCol, rowsSpan, colsSpan;
		VPos(int idxRow, int idxCol, int rowsSpan, int colsSpan) {
			this.idxRow = idxRow;
			this.idxCol = idxCol;
			this.rowsSpan = rowsSpan;
			this.colsSpan = colsSpan;
		}

		public int getIdxRow() {
			return idxRow;
		}

		public void setIdxRow(int idxRow) {
			this.idxRow = idxRow;
		}

		public int getIdxCol() {
			return idxCol;
		}

		public void setIdxCol(int idxCol) {
			this.idxCol = idxCol;
		}

		public int getRowsSpan() {
			return rowsSpan;
		}

		public void setRowsSpan(int rowsSpan) {
			this.rowsSpan = rowsSpan;
		}

		public int getColsSpan() {
			return colsSpan;
		}

		public void setColsSpan(int colsSpan) {
			this.colsSpan = colsSpan;
		}
	}

	public void setStagesPosition(List<? extends Stage> stages) {
		double ww = Screen.getPrimary().getVisualBounds().getWidth() / ncol;
		double hh = Screen.getPrimary().getVisualBounds().getHeight() / nrow;
		for(int i = 0; i < stages.size() && i < videoPosList.size(); i++) {
			Stage stage = stages.get(i);
			VPos vpos = videoPosList.get(i);
			stage.setX(vpos.idxCol * ww);
			stage.setWidth(ww * vpos.colsSpan);
			stage.setY(vpos.idxRow * hh);
			stage.setHeight(hh * vpos.rowsSpan);
		}
	}

	public List<VPos> getVideoPosList() {
		return videoPosList;
	}
}
