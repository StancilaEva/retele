package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.Transport;

import java.util.List;
import java.util.Map;

public class Server implements AutoCloseable{

	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private Map<Integer,Socket> clients=Collections.synchronizedMap(new HashMap<>());
	private List<Proces> listaProcese = Collections.synchronizedList(new ArrayList<>());
	public Object       lock    = new Object();
	
	@Override
	public void close() throws Exception {
		if (serverSocket != null && !serverSocket.isClosed()) {
			serverSocket.close();
		}
		if (executorService != null) {
			executorService.shutdown();
		}
	}

	public void start(int port) throws IOException {
		serverSocket = new ServerSocket(port);
		executorService = Executors.newFixedThreadPool(10 * Runtime.getRuntime().availableProcessors());
		listaProcese.add(new Proces("test"));
		listaProcese.add(new Proces("test2"));
		executorService.execute(() -> {
			while (serverSocket != null && !serverSocket.isClosed()) {
				try {
					Socket client = serverSocket.accept();
					executorService.execute(() -> {
						try {
							ClientState state = new ClientState();
							while (client != null && !client.isClosed()) {
								String request = Transport.receive(client);
								String response = processCommand(request, state);
								if(response=="Client autentificat cu success") {
									clients.put(state.code,client);									
								}
								System.out.println(request + " -> " + response);
								Transport.send(response, client);
							}							
							
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							clients.remove(client);
						}
					});
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private String processCommand(String request, ClientState state) {
		String items[] = request.strip().split("\\s");
		if (state.isAuthenticated) {
			if (items[0].equals("acces")) {
				if (items.length == 2) {
					String numeProces=items[1];
					boolean found=false;
					Proces procesCurent=null;
					for(Proces proces: listaProcese) {			
						if(numeProces.equals(proces.getCod())) {
							int result = proces.setThreadSleep(10000,state);
							if(result==1) {
								if(proces.getSemafor().addClient(state) ==0){
									return "Sunteti deja in lista pentru procesul "+proces.getCod();
								}else {
									System.out.println("procesul este ocupat");
									
								    return "procesul este ocupat va rugam asteptati executarea lui de catre ceilalti participanti";
									
									
								}
								
							}else {
								System.out.println(proces.getCod()+" a fost ocupat de catre "+proces.getSemafor().getClientCurent().code);
								Semafor semafor = proces.getSemafor();
								semafor.getListaClientiSubscrisi().remove(state);
								if(semafor.getListaClientiSubscrisi().size()>0) {
									semafor.setNewCurrentClient(semafor.getListaClientiSubscrisi().get(0));
								}
								else semafor.setNewCurrentClient(null);
								procesCurent=proces;
								semafor.setAcces();
								return "Procesul "+proces.getCod()+" s-a finalizat ";
							}
													
						}else {
							return "Nu exista procesul cu acest nume";
						}
					}	
				} else {
					return "Adauga si numele procesului";
				}
			}
			else {
				StringBuilder sb = new StringBuilder();	 
				for(Proces proces:listaProcese) {
					if(proces.getSemafor().getListaClientiSubscrisi().contains(state)) {
						try {
							Thread.sleep(5000);
						while(proces.getSemafor().getClientCurent()!=state)
						Thread.sleep(5000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					if(proces.getSemafor().getClientCurent()==state) {
						int result = proces.setThreadSleep(10000,state);
						System.out.println(proces.getCod()+" a fost ocupat de catre "+proces.getSemafor().getClientCurent().code);
						Semafor semafor = proces.getSemafor();
						semafor.setAcces();
						semafor.getListaClientiSubscrisi().remove(state);
						if(semafor.getListaClientiSubscrisi().size()>0) {
							semafor.setNewCurrentClient(semafor.getListaClientiSubscrisi().get(0));
						}
						else semafor.setNewCurrentClient(null);
						sb.append("Procesul "+proces.getCod()+" s-a finalizat \n");
					}
					 
				}
				} 
				sb.append("Adauga 'acces' urmat de numele procesului. Procesele disponibile sunt:");
				for(Proces s: listaProcese) {
					sb.append(s.getCod()+", ");					
				}	
				return sb.toString();
			}
			
		} else {
			if (items[0].equals("auth")) {
				if (items.length == 2) {
					try {
						Integer code=Integer.parseInt(items[1]);
						for(Integer codeMap: clients.keySet()) {
							if(code==codeMap) {
								return "Acest cod exista deja";
							}
						}
						state.isAuthenticated = true;
						state.code=code;
						return "Client autentificat cu success";
					}catch(NumberFormatException ex) {
						return "Codul trebuie sa fie un numar";				
					}
				} else {
					return "Adauga 'auth' urmat de codul clientului";
				}
			} else {
				return "Adauga 'auth' urmat de codul clientului";
			}
		}
		return "Exit";
	}
}
