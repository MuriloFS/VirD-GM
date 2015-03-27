package g3pd.virdgm.misc;

import g3pd.virdgm.core.VirdLauncher;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
//import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import g3pd.virdgm.core.VirdMemory;

public class VPEConnection extends Thread implements Serializable{
	private ServerSocket dataServer;
	private DataInputStream dis = null;
	//private ObjectInputStream ois = null;
	private DataOutputStream oos = null;
	private Socket client;
	private VirdLauncher launcher;
	

	/**Construtor responsavel por inicializar o servico
	 * @param port 	Porta onde o cliente se conectou*/
	public VPEConnection(VirdLauncher launcher, Integer port){
		try {
			dataServer = new ServerSocket(port);
			this.launcher = launcher;
			this.start();				
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run()
	{
		while(!dataServer.isClosed())
		{
			try {
				System.out.println("VirD-GM:   Aguardando Conexão do VPE-qGM: ");
				client = dataServer.accept();
				dis = new DataInputStream(client.getInputStream());
				oos = new DataOutputStream(client.getOutputStream());
				byte [] msg = new byte[3];
				dis.read(msg);			
				String msg1 = new String(msg);
				
				byte[] mem = "A".getBytes();
				byte[] mem2 = "C".getBytes();
				if (msg[0] == mem2[0]){
					dis.close();
					oos.close();
					dataServer.close();
					System.out.println("Fechou Conexoes");
				}else if (msg[0] == mem[0]){
					launcher.inicMem();
					System.out.println("Inicializou a memória");
				}else{
					String n = "";
					char c;
					for(int j=0;j<msg1.length();j++){
						c = msg1.charAt(j);
						if (Character.isDigit(c)){
							n += c;
						}
					}										
					int nArq = Integer.valueOf(n);
					launcher.memStop.acquire();
					VirdMemory memory = null;
					File f = new File(System.getProperty("user.dir"));
					for (int i=1; i <= nArq; i++){
						memory = (VirdMemory)launcher.inicLauncher(f.getParent()+"/tempFiles/tempProcess"+i+".xml");
					}
					launcher.memStop.release();
					XmlExport export = new XmlExport();
					export.exportXML(memory);
					oos.write(1);
					dis.close();
					oos.close();
				}
			} catch(Exception e) {e.printStackTrace();}
		}
	}
}
