package com.evanseverson.utils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DrawingFrame extends JFrame {

	private final List<DrawingSet> drawingSets = new LinkedList<>();
	protected final JPanel canvas;

	public DrawingFrame(int width, int height) {
		canvas = new JPanel();
		canvas.setSize(width, height);
		pack();
		setVisible(true);
		setSize(width, height);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	protected void addComponents() {
		add(canvas);
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		synchronized (drawingSets) {
			if (drawingSets.size() > 0) {
				for (DrawingSet.Drawing drawing : drawingSets.get(0).drawings) {
					drawing.draw(g2);
				}
				drawingSets.remove(0);
			}
		}
	}

	public DrawingSet createDrawingSet() {
		return new DrawingSet();
	}

	private enum DrawingType {
		CLEAR, POINT, LINE, RECTANGLE, FILL_RECTANGLE, ELLIPSE, FILL_ELLIPSE, SET_COLOR, SET_THICKNESS
	}

	public class DrawingSet {

		private class Drawing {

			DrawingType type;
			int[] params;

			private Drawing(DrawingType type, int... params) {
				if ((type == DrawingType.CLEAR && params.length != 0)
						|| (type == DrawingType.LINE && params.length != 5)
						|| (type == DrawingType.LINE && params.length != 5)
						|| (type == DrawingType.RECTANGLE && params.length != 4)
						|| (type == DrawingType.FILL_RECTANGLE && params.length != 4)
						|| (type == DrawingType.ELLIPSE && params.length != 4)
						|| (type == DrawingType.FILL_ELLIPSE && params.length != 4)
						|| (type == DrawingType.SET_COLOR && params.length != 4)
						|| (type == DrawingType.SET_THICKNESS && params.length != 1)
				) {
					throw new IllegalArgumentException("Incorrect number of arguments for type " + type.name() + "; got " + params.length + ".");
				}
				this.type = type;
				this.params = params;
			}

			private void draw(Graphics2D g) {
				switch (type) {
					case CLEAR: {
						g.clearRect(0, 0, getWidth(), getHeight());
						break;
					}
					case POINT: {
						int thickness = params[0];
						int x = params[1];
						int y = params[2];
						if (thickness != 0) {
							g.setStroke(new BasicStroke(thickness));
						}
						g.drawLine(x, y, x, y);
						break;
					}
					case LINE: {
						int thickness = params[0];
						int x1 = params[1];
						int y1 = params[2];
						int x2 = params[3];
						int y2 = params[4];
						if (thickness != 0) {
							g.setStroke(new BasicStroke(thickness));
						}
						g.drawLine(x1, y1, x2, y2);
						break;
					}
					case RECTANGLE: {
						int x1 = params[0];
						int y1 = params[1];
						int x2 = params[2];
						int y2 = params[3];
						g.drawRect(x1, y1, x2 - x1, y2 - y1);
						break;
					}
					case FILL_RECTANGLE: {
						int x1 = params[0];
						int y1 = params[1];
						int x2 = params[2];
						int y2 = params[3];
						g.fillRect(x1, y1, x2 - x1, y2 - y1);
						break;
					}
					case ELLIPSE: {
						int x = params[0];
						int y = params[1];
						int width = params[2];
						int height = params[3];
						g.drawOval(x, y, width, height);
						break;
					}
					case FILL_ELLIPSE: {
						int x = params[0];
						int y = params[1];
						int width = params[2];
						int height = params[3];
						g.fillOval(x, y, width, height);
						break;
					}
					case SET_COLOR: {
						int red = params[0];
						int green = params[1];
						int blue = params[2];
						int alpha = params[3];
						g.setColor(new Color(red, green, blue, alpha));
						break;
					}
					case SET_THICKNESS: {
						int thickness = params[0];
						g.setStroke(new BasicStroke(thickness));
						break;
					}
				}
			}
		}

		List<Drawing> drawings = new ArrayList<>();

		private DrawingSet() {
		}

		public void apply() {
			synchronized (drawingSets) {
				drawingSets.add(this);
			}
			repaint();
		}

		public DrawingSet clear() {
			drawings.add(new Drawing(DrawingType.CLEAR, new int[]{}));
			return this;
		}

		public DrawingSet drawPoint(int x, int y) {
			return drawPoint(0, x, y);
		}

		public DrawingSet drawPoint(int thickness, int x, int y) {
			drawings.add(new Drawing(DrawingType.POINT, thickness, x, y));
			return this;
		}

		public DrawingSet drawLine(int x1, int y1, int x2, int y2) {
			return drawLine(0, x1, y1, x2, y2);
		}

		public DrawingSet drawLine(int thickness, int x1, int y1, int x2, int y2) {
			drawings.add(new Drawing(DrawingType.LINE, thickness, x1, y1, x2, y2));
			return this;
		}

		public DrawingSet drawRectangle(int x1, int y1, int x2, int y2) {
			drawings.add(new Drawing(DrawingType.RECTANGLE, x1, y1, x2, y2));
			return this;
		}

		public DrawingSet fillRectangle(int x1, int y1, int x2, int y2) {
			drawings.add(new Drawing(DrawingType.FILL_RECTANGLE, x1, y1, x2, y2));
			return this;
		}

		public DrawingSet drawEllipse(int x, int y, int width, int height) {
			drawings.add(new Drawing(DrawingType.ELLIPSE, x, y, width, height));
			return this;
		}

		public DrawingSet fillEllipse(int x, int y, int width, int height) {
			drawings.add(new Drawing(DrawingType.FILL_ELLIPSE, x, y, width, height));
			return this;
		}

		public DrawingSet setColor(Color c) {
			return setColor(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
		}

		public DrawingSet setColor(int r, int g, int b) {
			return setColor(r, g, b, 255);
		}

		public DrawingSet setColor(int r, int g, int b, int a) {
			drawings.add(new Drawing(DrawingType.SET_COLOR, r, g, b, a));
			return this;
		}

		public DrawingSet setThickness(int thickness) {
			drawings.add(new Drawing(DrawingType.SET_THICKNESS, thickness));
			return this;
		}
	}

}
