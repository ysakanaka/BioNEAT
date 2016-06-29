package erne.util;

import java.util.Random;

public class Randomizer {

	private static Random rand = new Random();

	public static double getRandomLogScale(double lowerBound, double upperBound) {
		double lower = Math.log(lowerBound);
		double upper = Math.log(upperBound);
		return Math.exp(lower + rand.nextDouble() * (upper - lower));
	}

	public static void main(String[] args) {
		for (int i = 0; i < 1000; i++) {
			System.out.println(getRandomLogScale(1, 60));
		}
	}
}
