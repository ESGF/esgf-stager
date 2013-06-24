package practice;

import java.util.Random;

public class WaterTrough {

	private Random rand = new Random();
	
	public synchronized long getDrink() throws InterruptedException {
		long duration = Math.abs((this.rand.nextLong()) % 2000) + 1000;
		Thread.sleep(duration);
		return duration;
	}
	
	
}
