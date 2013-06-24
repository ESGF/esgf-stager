package practice;

public class Race {

	
	private int rank = 0;

	public static boolean firstThread = true;
	
	public synchronized void getReadyToRace() throws InterruptedException {
		this.wait();
		System.out.println("ready");
	}
	
	public synchronized void startRace() {
		System.out.println("opening the gate");
		this.notifyAll();
	}
	
	public synchronized int crossFinishLine(String name) throws InterruptedException {
		System.out.println(name + " entering finishline");
		if(firstThread) {
			firstThread = false;
			
			Thread.currentThread();
			try {
				Thread.sleep(8000);
			} catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println(name + " exiting finishline");
		return ++rank;
	}
	
	
}
