package g3pd.virdgm.core;

import g3pd.virdgm.types.VTObject;

import java.io.*;
import java.net.*;

/**Classe responsavel pela conexao entre a base e os nodos clientes*/
public class VirdClient extends Thread {

   private ServerSocket dateServer;


   public static void main(String argv[]) throws Exception {
     new VirdClient(Integer.parseInt(argv[0]));
   }
   
   /**Construtor responsavel por inicializar o servico
    * @param port 	Porta onde o cliente se conectou*/
   public VirdClient(Integer port) throws Exception {
	   dateServer = new ServerSocket(3000 + port);
	   System.out.println("Inicializando VirD-Client");
	   this.start();
   }
   
   public void run(){
	   while(true){
		   try {
			   System.out.println("Aguardando Processos: ");
			   Socket client = dateServer.accept();
			   System.out.println("Recebido processo de: " + client.getInetAddress());
		   } catch(Exception e){
			   
		   }
	   }
   	}
}

class Connect extends Thread {
   private Socket client = null;
   private ObjectInputStream ois = null;
   private ObjectOutputStream oos = null;

   public Connect() {}

   public Connect(Socket clientSocket){
     client = clientSocket;
     try {
      ois = new ObjectInputStream(client.getInputStream());
      oos = new ObjectOutputStream(client.getOutputStream());
     } catch(Exception e1) {
         try {
        	System.out.println("Erro: no connect");
            client.close();
         }catch(Exception e) {
           System.out.println("Erro:" + e.getMessage());
         }
         return;
     }
     this.start();
   }


   public void run() {
      try {
    	  VTObject myo = (VTObject) ois.readObject();
    	  
    	  VirdExecImpl ve = new VirdExecImpl();
    	  
    	  ve.exec((String) myo.getObject(0), (String) myo.getObject(1), (String) myo.getObject(2), (String) myo.getObject(3), (Integer) myo.getObject(4), (VirdMemory) myo.getObject(5), oos);
    	  myo = null;
    	 
    	  ois.close();
    	  oos.close();
    	  client.close();
      }catch(Exception e) {
    	  System.out.println(e.getMessage());
      }
   }
}
