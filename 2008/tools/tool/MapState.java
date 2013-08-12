package tool;

public class MapState {
	public int[][] bottom;
	public int[][] middle;
	public int[][] top;
	
	public MapState(int x, int y) {
		bottom = new int[x][y];
		middle = new int[x][y];
		top = new int[x][y];
	}
}
