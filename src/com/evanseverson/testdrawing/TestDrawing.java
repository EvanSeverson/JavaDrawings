package com.evanseverson.testdrawing;

import com.evanseverson.utils.DrawingFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class TestDrawing {

	public static final int WIDTH = 1000;
	public static final int HEIGHT = 1000;
	public static final int MAX_ITERS = 2000;
	public static final int MAX_COLOR_ITERS = 60;
	public static final int ZOOM_FACTOR = 8;

	public static void main(String[] args) {
		sleep(100);

		MandelbrotDrawingFrame mandelbrotDrawingFrame = new MandelbrotDrawingFrame(WIDTH, HEIGHT);
	}

	private static void drawMandelbrot(DrawingFrame drawingFrame, double xMin, double yMin, double xMax, double yMax) {
		int minIter = Integer.MAX_VALUE;
		int maxIter = 0;

		int[][] iters = new int[HEIGHT][WIDTH];

		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {
				double x0 = ((double) i) / WIDTH * (xMax - xMin) + xMin;
				double y0 = ((double) j) / HEIGHT * (yMax - yMin) + yMin;

				double x2 = 0;
				double y2 = 0;
				double w = 0;
				int iter = 0;
				for (; iter < MAX_ITERS; iter++) {
					if (x2 + y2 > 4) {
						break;
					}
					double x = x2 - y2 + x0;
					double y = w - x2 - y2 + y0;
					x2 = x * x;
					y2 = y * y;
					w = (x + y) * (x + y);
				}

				minIter = Math.min(minIter, iter);
				maxIter = Math.max(maxIter, iter);

				iters[j][i] = iter;
			}
		}
		DrawingFrame.DrawingSet ds = drawingFrame.createDrawingSet();

		int iterRange = Math.max(maxIter - minIter, MAX_COLOR_ITERS);


		for (int j = 0; j < HEIGHT; j++) {
			for (int i = 0; i < WIDTH; i++) {
				int iter = iters[j][i];

				if (iter == MAX_ITERS) {
					ds.setColor(Color.BLACK);
				} else {
					ds.setColor(Color.getHSBColor((float) (Math.cbrt(((double) Math.min(iter - minIter, iterRange)) / iterRange) * 0.7f), 1, 1));
				}
				ds.drawPoint(i, j);
			}
		}

		ds.apply();
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static class MandelbrotDrawingFrame extends DrawingFrame {

		private double minX = -2, minY = -1.5, maxX = 1, maxY = 1.5;

		public MandelbrotDrawingFrame(int width, int height) {
			super(width, height);

			addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					int x = e.getPoint().x;
					int y = e.getPoint().y;
					double height = maxY - minY;
					double width = maxX - minX;
					double centerX = minX + width * (((double) x) / TestDrawing.WIDTH);
					double centerY = minY + height * (((double) y) / TestDrawing.HEIGHT);
					System.out.println("centerX=" + centerX + " centerY=" + centerY);
					minX = centerX - (width / 2 / ZOOM_FACTOR);
					minY = centerY - (height / 2 / ZOOM_FACTOR);
					maxX = centerX + (width / 2 / ZOOM_FACTOR);
					maxY = centerY + (height / 2 / ZOOM_FACTOR);
					createDrawingSet()
							.setColor(255, 255, 255, 50)
							.fillRectangle(
									x - TestDrawing.WIDTH / 2 / ZOOM_FACTOR,
									y - TestDrawing.HEIGHT / 2 / ZOOM_FACTOR,
									x + TestDrawing.WIDTH / 2 / ZOOM_FACTOR,
									y + TestDrawing.HEIGHT / 2 / ZOOM_FACTOR)
							.setThickness(2)
							.setColor(Color.BLACK)
							.drawRectangle(
									x - TestDrawing.WIDTH / 2 / ZOOM_FACTOR,
									y - TestDrawing.HEIGHT / 2 / ZOOM_FACTOR,
									x + TestDrawing.WIDTH / 2 / ZOOM_FACTOR,
									y + TestDrawing.HEIGHT / 2 / ZOOM_FACTOR)
							.apply();

					SwingUtilities.invokeLater(() -> {
						drawMandelbrot(MandelbrotDrawingFrame.this, minX, minY, maxX, maxY);
					});
				}

				public void mousePressed(MouseEvent e) {}
				public void mouseReleased(MouseEvent e) {}
				public void mouseEntered(MouseEvent e) {}
				public void mouseExited(MouseEvent e) {}
			});

			SwingUtilities.invokeLater(() -> {
				sleep(100);
				drawMandelbrot(this, minX, minY, maxX, maxY);
			});
		}

//		@Override
//		protected void addComponents() {
//			BoxLayout
//		}
	}

}
