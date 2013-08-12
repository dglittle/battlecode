package tool;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class MapStateDisplayPanel extends JPanel {
	public MapState mapState;
	public int tileSize = 32;

	public MapStateDisplayPanel() {

	}
	
	public static Color colorLerp(Color c, double t0, double v0, double t1, double v1) {
		return new Color((float) MyUtil.U.lerp(t0, v0, t1, v1, (double)c.getRed() / 255),
				(float) MyUtil.U.lerp(t0, v0, t1, v1, (double)c.getGreen() / 255),
				(float) MyUtil.U.lerp(t0, v0, t1, v1, (double)c.getBlue() / 255));
	}

	public static Color makeWhiter(Color c) {
		return colorLerp(c, 0, 0.7, 1, 1);
	}
	public static Color makeDarker(Color c) {
		return colorLerp(c, 0, 0, 1, 0.7);
	}

	public void paint(Graphics g) {
		if (mapState == null) {
			g.drawLine(0, 0, 100, 100);
			return;
		}

		for (int x = 0; x < mapState.bottom.length; x++) {
			for (int y = 0; y < mapState.bottom[x].length; y++) {
				// bottom
				int type = mapState.bottom[x][y];
				if (type == 0) {
					g.setColor(makeWhiter(new Color(1.0f, 0.5f, 0.0f)));
				} else if (type == 1) {
					g.setColor(makeWhiter(new Color(0.0f, 0.0f, 1.0f)));
				}
				g.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
				g.setColor(Color.BLACK);
				g.drawLine(x * tileSize, y * tileSize, x * tileSize, y * tileSize + tileSize - 1);
				g.drawLine(x * tileSize, y * tileSize, x * tileSize + tileSize - 1, y * tileSize);

				// middle
				type = mapState.middle[x][y];
				if (type > 10) {
					g.setColor(makeDarker(Color.RED));
					type -= 10;
				} else {
					g.setColor(makeDarker(Color.GREEN));
				}
				if (type == 1) {
					g.drawLine(x * tileSize + 2, y * tileSize + (tileSize / 2),
							x * tileSize + (tileSize / 2), y * tileSize
									+ (tileSize - 2));
					g.drawLine(x * tileSize + 2, y * tileSize + (tileSize - 2),
							x * tileSize + (tileSize / 2), y * tileSize
									+ (tileSize / 2));
				} else if (type == 2) {
					g.drawOval(x * tileSize + 2, y * tileSize + (tileSize / 2), tileSize / 2, tileSize / 2 - 1);
				}
			}
		}
	}
}
