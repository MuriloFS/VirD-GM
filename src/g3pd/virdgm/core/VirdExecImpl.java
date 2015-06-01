package g3pd.virdgm.core;

//import g3pd.virdgm.misc.VirdLogger;
import g3pd.virdgm.types.VTObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**Classe que implementa a VirdExec, sendo responsavel pela execucao das funcoes 
 * que cada processo representa*/
public class VirdExecImpl implements VirdExec {
    /**
     * Metodo responsavel por identificar os parametros de entrada dos processos
     * @param inputAttr String contendo os parametros de entrada para o processo
     * */
	public Integer [] inputAttrParser(String inputAttr) {
        String[] attrs      = inputAttr.split(",");
        Integer []    attributes = new Integer [attrs.length];

        for (int i = 0; i < attrs.length; i++) {
            attributes[i] = Integer.parseInt(attrs[i]);
        }
        
        return attributes;
    }
	/**
	 * Metodo da Interface VirdExec responsavel por criar uma conexao com o cliente para 
	 * enviar os resultados dos processos
	 * 
	 * /**
	 * @param actionAttr	Acao a ser avaliada
	 * @param valueAttr		Valor para testes
	 * @param inputPosAttr	Posicoes da memoria com os paramentros de entrada
	 * @param outputAttr	Posicao da memoria para armazenar o resultado da avaliacao
	 * @param iteradorAttr	Iterador 
	 * @param memory		Espaco de memoria utilizado, com os parametros
	 * @param host			Host que recebera o processo para avaliacao
	 * @param port 			Porta onde ocorre a comunicacao
	 * */
	public boolean send(String actionAttr,  String valueAttr, String inputPosAttr, String outputPosAttr, String controlListAttr, Integer iterator, VirdMemory memory,  String host, Integer port)
	{
	      ObjectOutputStream oos = null;
	      ObjectInputStream ois = null;
	      Socket socket = null;

	      try {
	    	// open a socket connection
	        socket = new Socket(host, port);
	        // open I/O streams for objects
	        oos = new ObjectOutputStream(socket.getOutputStream());
	        ois = new ObjectInputStream(socket.getInputStream());
	        // read an object from the server

	        VTObject myo = new VTObject();
	        VTObject myi = new VTObject();
	        
	        myo.addObject(actionAttr); //0
	        myo.addObject(valueAttr); //1
	        myo.addObject(inputPosAttr); //2
	        myo.addObject(outputPosAttr); //3
	        myo.addObject(controlListAttr);
	        myo.addObject(iterator); //4
	        
	        /*
	        System.out.println("Dados");
	        System.out.println("Value:" + valueAttr);
	        System.out.println("In: "+ inputPosAttr);
	        System.out.println("Ou:" + outputPosAttr);
	        */
	        
	        if (memory.GPU){
	        	VirdMemory temp = memory.intervalMemory(outputPosAttr);
	        	myo.addObject(temp);
	        }
	        else{
	        	myo.addObject(memory); //5
	        }
	        
	        oos.writeObject(myo);
	        
	        myi = (VTObject) ois.readObject();
	        
	        if (iterator == -1){ 	//Processo Quântico
	        	memory.writeMemory(myi.getObject(0));
	        }
	        else{ 					//Processo Elementar
	        	memory.writeMemory(myi.getObject(0), (Integer) myi.getObject(1));
	        }
	        
	        oos.close();
	        ois.close();
	      } catch(Exception e) {
	    	  System.out.println("Exeption");
	      }
	      
	        return true;
	}
	/**
	 * Metodo da Interface VirdExec responsavel pela execucao dos processos, buscando a 
	 * biblioteca associada a acao de cada processo
	 * /**
	 * @param actionAttr	Acao a ser avaliada
	 * @param valueAttr		Valor para testes
	 * @param inputPosAttr	Posicoes da memoria com os paramentros de entrada
	 * @param outputAttr	Posicao da memoria para armazenar o resultado da avaliacao
	 * @param iteradorAttr	Iterador 
	 * @param memory		Espaco de memoria utilizado, com os parametros
	 * @param oos			Objeto para escrita na memoria
	 * */
    public boolean exec(String actionAttr, String valueAttr, String inputPosAttr, String outputPosAttr, String controlListAttr, Integer iterator, VirdMemory memory, ObjectOutputStream oos) {
        memory.setOutputStream(oos);
        if(iterator == null) iterator = 0;

		Integer [] input = null;
		Integer output = 0;
		
		if (!actionAttr.equals("QGMAnalyzer")){
			input = inputAttrParser(inputPosAttr);
		}
		
		if (!outputPosAttr.contains("[")){
			output = Integer.parseInt(outputPosAttr);
		}
		
        Integer valueput = 0;
        if (actionAttr.equals("Teste")){
        	System.out.println("HAHA");
        	valueput = Integer.parseInt(valueAttr);
        }

        Class classe = null;
        
       	Class[] types = null;
       	Object[] args = null;
       	
		if (actionAttr.equals("QGMAnalyzer")){//AQUI
        	types = new Class[] {String.class, String.class , String.class, String.class, Integer.class, VirdMemory.class, ObjectOutputStream.class};
           	args = new Object[] {valueAttr, inputPosAttr, outputPosAttr, controlListAttr, iterator, memory, oos };
        }else{
	       	types = new Class[] { Integer.class, Integer[].class , String.class, Integer.class, VirdMemory.class, ObjectOutputStream.class};
	       	args = new Object[] {valueput, input, outputPosAttr, iterator, memory, oos };
        }

        try {
        	classe = Class.forName("g3pd.virdgm.apps." + actionAttr);
        	Method app = classe.getDeclaredMethod("app", types);
        	app.invoke(classe.newInstance(), args);
        } catch (ClassNotFoundException e) {
        	e.printStackTrace();
        } catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
        return true;
    }
}

