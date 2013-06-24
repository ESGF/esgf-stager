package practice;

public class HorseRaceMain {

	public static void main(String [] args) throws InterruptedException {
		Race race = new Race();
		WaterTrough trough = new WaterTrough();
		
		new Thread(new Horse("Mine That Bird",race,trough)).start();
		new Thread(new Horse("Big Brown",race, trough)).start();
		//new Thread(new Horse("Street Sense",race,trough)).start();
		//new Thread(new Horse("Barbaro",race,trough)).start();
		
		System.out.println("Get ready");
		Thread.sleep(2000);
		System.out.println("Get set");
		Thread.sleep(2000);
		System.out.println("Go!");
		race.startRace();
	}
	
}
