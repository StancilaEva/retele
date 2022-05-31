package server;

import java.util.ArrayList;
import java.util.List;

public class Semafor {
	private boolean accesPermis;
	private ClientState clientCurent=null;
	private List<ClientState> listaClientiSubscrisi;
	
	public Semafor() {
		super();
		this.accesPermis = true;
		this.listaClientiSubscrisi=new ArrayList();
	}
	
	public void setAcces() {
		if(this.accesPermis==true) {
			this.accesPermis=false;
		}else {
			this.accesPermis=true;
		}
		
	}	
	
	public boolean isAccesPermis() {
		return accesPermis;
	}

	public int addClient(ClientState clientNou) {
		for(ClientState c: listaClientiSubscrisi) { 
			if(clientNou.code==c.code) {
				return 0;
			}
		}
		listaClientiSubscrisi.add(clientNou);
		return 1;
	}
	
	public void setNewCurrentClient(ClientState clientNou) {
		this.clientCurent=clientNou;
	}

	public ClientState getClientCurent() {
		return clientCurent;
	}

	public List<ClientState> getListaClientiSubscrisi() {
		return listaClientiSubscrisi;
	}
	
}