package server;

public class Proces {
	private String cod;
	private Semafor semafor;

	public Proces(String cod) {
		super();
		this.cod = cod;
		this.semafor = new Semafor();
	}
	
	public int setThreadSleep(int time,ClientState client) {
		try {
			if(this.semafor.isAccesPermis()) { //daca semaforul cu procesul e liber
				this.semafor.setNewCurrentClient(client);
				this.semafor.setAcces();
				Thread.sleep(time);
				return 0;
			}else return 1;
		}catch(Exception ex) {
						
		}
		return 1;
	}

	public String getCod() {
		return cod;
	}
	
	public Semafor getSemafor() {
		return semafor;
	}
	
}