package com.evanseverson.testdrawing;

import com.evanseverson.utils.DrawingFrame;

import javax.swing.*;
import java.util.*;

public class ContinuousDFS {
	
	private static final double START_X = 500, START_Y = 500;
	private static final int WIDTH = 1000, HEIGHT = 1000;
	private static final double STEP_SIZE = 20;

	public static void main(String[] args) throws InterruptedException {
		DFSFrame dfsFrame = new DFSFrame(WIDTH, HEIGHT, 3);

//		dfsFrame.debug();

		Thread.sleep(100);

		for (int i = 0; i < 1000; i++) {
			dfsFrame.step();
			Thread.sleep(200);
		}
		System.out.println("done!");
	}
	
	private static class DFSFrame extends DrawingFrame {

		List<List<Step>> paths = new ArrayList<>();
		Map<StepBucket, Set<Step>> stepBucketsToSteps = new HashMap<>();
		
		private class Step {
			double x1, y1, x2, y2;
			
			private Step(double x1, double y1, double x2, double y2) {
				this.x1 = x1;
				this.y1 = y1;
				this.x2 = x2;
				this.y2 = y2;
			}

			private StepBucket getStepBucket() {
				int x = (int) ((x1 + x2) / 2 / STEP_SIZE);
				int y = (int) ((y1 + y2) / 2 / STEP_SIZE);
				return new StepBucket(x, y);
			}

			private List<StepBucket> getNeighboringStepBuckets() {
				int x = (int) ((x1 + x2) / 2 / STEP_SIZE);
				int y = (int) ((y1 + y2) / 2 / STEP_SIZE);

				List<StepBucket> returnVal = new ArrayList<>(9);
				for (int i = -1; i <= 1; i++) {
					for (int j = -1; j <= 1; j++) {
						returnVal.add(new StepBucket(x + i, y + j));
					}
				}
				return returnVal;
			}
		}

		private class StepBucket implements Comparable<StepBucket> {
			private int x, y;

			private StepBucket(int x, int y) {
				this.x = x;
				this.y = y;
			}

			@Override
			public int compareTo(StepBucket o) {
				int c = Integer.compare(y, o.y);
				return c != 0 ? c : Integer.compare(x, o.x);
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (!(o instanceof StepBucket)) return false;
				StepBucket that = (StepBucket) o;
				return x == that.x && y == that.y;
			}

			@Override
			public int hashCode() {
				return Objects.hash(x, y);
			}
		}

		public DFSFrame(int width, int height, int numPaths) {
			super(width, height);
			for (int i = 0; i < numPaths; i++) {
				paths.add(new ArrayList<>());
			}
		}

		private void step() {
			List<Step> newSteps = new ArrayList<>();
			synchronized (stepBucketsToSteps) {
				for (List<Step> path : paths) {
					double headX = START_X, headY = START_Y;
					if (path.size() != 0) {
						Step step = path.get(path.size() - 1);
						headX = step.x2;
						headY = step.y2;
					}

					// Find the next step
					while (true) {
						double angle = Math.random() * 2 * Math.PI;
						Step nextStep = new Step(headX, headY,
								headX + Math.cos(angle) * STEP_SIZE, headY + Math.sin(angle) * STEP_SIZE);
						if (stepCrossesExisting(nextStep)) {
							continue;
						}

						path.add(nextStep);

						StepBucket stepBucket = nextStep.getStepBucket();
						Set<Step> steps = stepBucketsToSteps.get(stepBucket);
						if (steps == null) {
							steps = new HashSet<>();
							stepBucketsToSteps.put(stepBucket, steps);
						}
						steps.add(nextStep);
						newSteps.add(nextStep);
						break;
					}
				}
			}
			drawNewSteps(newSteps);
		}

		private void drawNewSteps(List<Step> steps) {
			SwingUtilities.invokeLater(() -> {
				DrawingSet drawingSet = createDrawingSet().setThickness(2);
				for (Step step : steps) {
					drawingSet.drawLine((int) step.x1, (int) step.y1, (int) step.x2, (int) step.y2);
				}
				drawingSet.apply();
			});
		}

		private void drawAllSteps() {
			SwingUtilities.invokeLater(() -> {
				synchronized (stepBucketsToSteps) {
					DrawingSet drawingSet = createDrawingSet().clear().setThickness(2);
					for (Set<Step> steps : stepBucketsToSteps.values()) {
						for (Step step : steps) {
							System.out.println("drawing a line");
							drawingSet.drawLine((int) step.x1, (int) step.y1, (int) step.x2, (int) step.y2);
						}
					}
					drawingSet.apply();
				}
			});
		}

		private boolean stepCrossesExisting(Step step) {
			List<StepBucket> stepBuckets = step.getNeighboringStepBuckets();

			double x1 = step.x1, y1 = step.y1, x2 = step.x2, y2 = step.y2;
			double dx = x1 - x2, dy = y1 - y2;

			for (StepBucket bucket : stepBuckets) {
				Set<Step> steps = stepBucketsToSteps.get(bucket);
				if (steps == null) {
					continue;
				}

				for (Step step2 : steps) {
					double a1 = step2.x1, b1 = step2.y1, a2 = step2.x2, b2 = step2.y2;
					double da = a1 - a2, db = b1 - b2;

					// Solve linear relation
					// [dy -dx] [x] == [x2y1-x1y2]
					// [db -da].[y]    [a2b1-a1b2]
					double det = dx * db - dy * da;
					if (det == 0) {
						// Lines don't intersect
						continue;
					}
					double c1 = x1 * y2 - x2 * y1; // Negated from original eq
					double c2 = a2 * b1 - a1 * b2;
					double x = (da * c1 + dx * c2) / det;
					double y = (db * c1 + dy * c2) / det; // Needed for the case where one is a vertical line
					if (isBetween(x, x1, x2) && isBetween(x, a1, a2) && isBetween(y, y1, y2) && isBetween(y, b1, b2)) {
						return true;
					}
				}
			}
			return false;
		}

		private boolean isBetween(double val, double out1, double out2) {
			if (out2 < out1) {
				double tmp = out2;
				out2 = out1;
				out1 = tmp;
			}
			return out1 <= val && val <= out2;
		}

		public void debug() {
			Step s = new Step(500, 500, 500+14.1421, 500+14.1421);
			Set<Step> set = new HashSet<>();
			set.add(s);
			stepBucketsToSteps.put(s.getStepBucket(), set);

			stepCrossesExisting(new Step(510, 500, 510, 520));
		}
	}
}
