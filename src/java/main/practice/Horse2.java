package practice;

import java.util.Random;

public class Horse2 implements Runnable {

	private String name;
	private Race2 race;
	
	public Horse2(String name, Race2 race) {
		this.name = name;
		this.race = race;
	}
	
	
	@Override
	public void run() {
		try {
			//this.race.getReadyToRace();
			
			
			int place = this.race.crossFinishLine(this.name);
			
		} catch(InterruptedException ie) {
			System.out.println(name + " apparently broke a leg and wont be finishing");
		}
	}
	
}
