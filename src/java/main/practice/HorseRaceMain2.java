package practice;

public class HorseRaceMain2 {

	public static void main(String [] args) throws InterruptedException {
		Race2 race = new Race2();
		
		new Thread(new Horse2("Mine That Bird",race)).start();
		new Thread(new Horse2("Big Brown",race)).start();
		//new Thread(new Horse("Street Sense",race,trough)).start();
		//new Thread(new Horse("Barbaro",race,trough)).start();
		
		//System.out.println("Get ready");
		//Thread.sleep(2000);
		//System.out.println("Get set");
		//Thread.sleep(2000);
		//System.out.println("Go!");
		//race.startRace();
	}
	
}
