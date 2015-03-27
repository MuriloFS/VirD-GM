package g3pd.virdgm.core;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.jscience.mathematics.number.Complex;

/**Classe que representa a memoria utilizada durante a execucao de 
 * determinada avaliacao*/
public class VirdBackMemory {
	private ConcurrentHashMap<Integer, Complex> dataBackup = new ConcurrentHashMap<Integer, Complex>();
	private ConcurrentHashMap<Integer, String> typeBackup = new ConcurrentHashMap<Integer, String>();
	
	public VirdBackMemory(){
		
	}
	
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
	
	/**Metodo que retorna o tipo de determinada posicao de memoria
	 * @param position	Posicao a qual se verificar o tipo*/
	public String getType(Integer position)
	{
		return (String) this.typeBackup.get(position);
	}

	public int getMemorySize(){
		return this.dataBackup.size();
	}
}