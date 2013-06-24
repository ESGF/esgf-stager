package practice;

import java.util.Random;

public class Horse implements Runnable {

	private String name;
	private Race race;
	private WaterTrough trough;
	private Random rand = new Random();
	
	public Horse(String name, Race race, WaterTrough trough) {
		this.name = name;
		this.race = race;
		this.trough = trough;
	}
	
	public long runLap() throws InterruptedException {
		long duration = Math.abs((this.rand.nextLong())) % 4000 + 2000;
		Thread.sleep(duration);
		return duration;
	}
	
	@Override
	public void run() {
		try {
			this.race.getReadyToRace();
			
			System.out.println(this.name + " is off and running");
			
			long totalTime = 0;
			
			for(int i=1;i<=3;i++) {
				long time = this.runLap();
				totalTime += time;
				System.out.println(name + " completes lap " + i + " in " + (double)time);
				/*
				time = this.trough.getDrink();
				System.out.println(name + " drinks for " + (double)time/(double)10000.0);
				*/
			}
			
			System.out.println(">>> " + name + "total time " + totalTime);
			
			int place = this.race.crossFinishLine(this.name);
			System.out.println(">>> " + name + " finishes in position " + place + "! with total " + totalTime);
			
		} catch(InterruptedException ie) {
			System.out.println(name + " apparently broke a leg and wont be finishing");
		}
	}
	
}
