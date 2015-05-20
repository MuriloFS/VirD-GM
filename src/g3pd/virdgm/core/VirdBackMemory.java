package g3pd.virdgm.core;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jscience.mathematics.number.Complex;

//import g3pd.virdgm.misc.VirdLogger;
import g3pd.virdgm.types.VTObject;


/**Classe que representa a memoria utilizada durante a execucao de 
 * determinada avaliacao*/
public class VirdBackMemory {
	public ConcurrentHashMap<Integer, Complex> dataBackup = new ConcurrentHashMap<Integer, Complex>();
	public ConcurrentHashMap<Integer, String> typeBackup = new ConcurrentHashMap<Integer, String>();
//	public List dataBackup = Collections.synchronizedList(new LinkedList());
//	public List typeBackup = Collections.synchronizedList(new LinkedList());
	
	public VirdBackMemory(){}
	//public List dataBackup = Collections.synchronizedList(new LinkedList());
	/**Construtor do objeto VirdMemory
	 * @param memorytype	Lista com os tipos de cada posicao de memoria
	 * @param memorydata	Lista com os dados de cada posicao de memoria */
	public VirdBackMemory(List memoryType, List memoryData){
		for (int i = 0; i<memoryData.size();i++){
			this.dataBackup.put(i,(Complex)memoryData.get(i));
			try {
				updateBackMemory(memoryData.get(i),i);
			} catch (IOException e) {}
			this.typeBackup.put(i,(String)memoryType.get(i));
		}
	}

	/**Metodo para atualizacao da memoria 
	 * @param value 	Valor a ser escrito na memoria
	 * @param position 	Posicao a ser atualizada*/
	public void updateBackMemory(Object value, Integer position) throws IOException
	{
		dataBackup.put(position, (Complex)value);		  	
	}

	public void writeBackMemory(Object value, Integer position){    		
		this.dataBackup.put(position, (Complex)value);
	}

	/**Metodo para leitura de determinada posicao de memoria
	 * @param position 	Posicao da memoria a ser lida*/
	public Object readBackupMemory(Integer position)
	{
		return this.dataBackup.get(position);
	}
//	public Object readBackupMemory(Integer position)
//	{
//		return dataBackup.get(position);
//	}
	/**Metodo que retorna o tipo de determinada posicao de memoria
	 * @param position	Posicao a qual se verificar o tipo*/
	public String getType(Integer position)
	{
		return (String) this.typeBackup.get(position);
	}

	public int getMemorySize(){
		return this.dataBackup.size();
	}

//	public void updateFull(){
//		for (int i=0; i<this.data.size(); i++){
//			synchronized (this.data) {
//				this.dataBackup.set(i, data.get(i));
//			}
//		}
//	}

}