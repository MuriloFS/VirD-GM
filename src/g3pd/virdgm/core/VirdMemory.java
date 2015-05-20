package g3pd.virdgm.core;

import org.jscience.mathematics.number.Complex;

import g3pd.virdgm.types.VTObject;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import de.upb.swt.mcie.robdds.*; // Pacote para criação de ROBDD em java

/**Classe que representa a memoria utilizada durante a execucao de 
 * determinada avaliacao*/
public class VirdMemory implements Serializable{

	private static final long serialVersionUID = 88272296244932942L;
	public ConcurrentHashMap<Integer, Object> data = new ConcurrentHashMap<Integer, Object>();
	public ConcurrentHashMap<Integer, String> type = new ConcurrentHashMap<Integer, String>();
	public int shift;
	public boolean GPU = false;
	public boolean writing = false;
	private ObjectOutputStream oos;
	
	/**Indica o objeto stream para escrita dos dados na memoria*/
	public void setOutputStream(ObjectOutputStream oos)
	{
		this.oos = oos;
	}
	
	public VirdMemory(){}
	/**Construtor do objeto VirdMemory
	 * @param memorytype	Lista com os tipos de cada posicao de memoria
	 * @param memorydata	Lista com os dados de cada posicao de memoria */
	@SuppressWarnings("unchecked")
	public VirdMemory(ConcurrentHashMap<Integer, String> memoryType, ConcurrentHashMap<Integer, Object> memoryData){
			this.data = memoryData;
			this.type = memoryType;
			ConcurrentHashMap<Integer, Object> temp;
	}
	
	public VirdMemory intervalMemory(String input){
		int begin, q, num_partes;
		String init_pos;
		//System.out.println(entrada);
		
		//retira o '[' inicial e o ']' final
		input = input.substring(1 , input.length() - 1);
		
		//separa emdim_soma substrings
		String [] partes = input.split("]");
		
		String[][] saida = new String[partes.length][];
		
		q = 0;
		
		init_pos = "";
		num_partes = 1;
		//separa a entrada em vetores de listas de strings
		for(int i=0; i<partes.length; i++){
			begin = 2;
			if (i==0) {begin = 1;}
			
			//retira os '[' , ']' das substrings; 
			partes[i] = partes[i].substring(begin);
			
			//separa as substrings em uma lista de strings
			saida[i] = (partes[i].split(","));
			
			num_partes = num_partes * (int)Math.pow(2, saida[i][0].length()) / saida[i].length;
			init_pos = init_pos + saida[i][0];
			q += saida[i][0].length();
			
		}
		
		//System.out.println(q);
		//System.out.println("Pos: " + init_pos + "\nPartes: " + num_partes);
		
		VirdMemory temp = new VirdMemory();
		
		ConcurrentHashMap<Integer, float[]> m = (ConcurrentHashMap<Integer, float[]>) this.readMemory(0);
		float men[] = m.get(0);
		float data[] = new float[(int)Math.pow(2, q) * 2 / num_partes];
		float d[] = new float[1];
		
		temp.shift = Integer.parseInt(init_pos, 2);
		
		System.arraycopy(men, temp.shift, data, 0, data.length);
		
		
		
		ConcurrentHashMap<Integer, float[]> t = new ConcurrentHashMap<Integer, float[]>();
		t.put(0, data);
		t.put(1, d);
		
		temp.data.put(0, t);
		
		temp.GPU = this.GPU;
		
		return temp;
	}
	
	public void print (int i){
		if (GPU){
			ConcurrentHashMap<Integer, float[]> memory = (ConcurrentHashMap<Integer, float[]>)readMemory(0);
			int x;
			float[] m = memory.get(1);
			x = m.length/2;
			if (m.length > 100) x = 50;
			
			for (i = 0; i < x; i++){
		    	System.out.println("pos: " + i/2 + "  " + m[i] + " " + m[i+1]);
		    	i++;
			}

			
			for (i = m.length - x; i < m.length; i++){
		    	System.out.println("pos: " + i/2 + "  " + m[i] + " " + m[i+1]);
		    	i++;
			}
		}
	}

	/**Metodo para atualizacao da memoria 
	 * @param value 	Valor a ser escrito na memoria
	 * @param position 	Posicao a ser atualizada*/
	public void updateMemory(Object value, Integer position) throws IOException
	{
		VTObject vto = new VTObject();
    	vto.addObject(value);
    	vto.addObject(position);
    	oos.writeObject(vto);		  	
	}
	
	public void updateMemory(ConcurrentHashMap <Integer, Complex> men) throws IOException
	{
		VTObject vto = new VTObject();
    	vto.addObject(men);
    	oos.writeObject(vto);
	}
	
	public void updateMemory(float[] men, int deslocamento) throws IOException{
		ConcurrentHashMap <Integer, Object> out = new ConcurrentHashMap <Integer, Object>();
		out.put(0, men);
		out.put(1, deslocamento);
		
		VTObject vto = new VTObject();
    	vto.addObject(out);
    	oos.writeObject(vto);
	}

	public void writeMemory(Object value, Integer position){
		this.data.put(position, value);
	}
	
	@SuppressWarnings("unchecked")
	synchronized public void writeMemory(Object map){
		writing = true;
		if (!GPU){
			ConcurrentHashMap<Integer, Complex> temp = (ConcurrentHashMap<Integer, Complex>)(map);
			ConcurrentHashMap<Integer, Complex> memory = (ConcurrentHashMap<Integer, Complex>)(this.data.get(0));
			Enumeration <Integer> p;
			Integer position;
			for (p = temp.keys(); p.hasMoreElements(); ){
				position = p.nextElement();
				memory.replace(position, temp.get(position));
			}
		}
		else{
			
			float[] temp = (float[])((ConcurrentHashMap<Integer, Object>) map).get(0);
			int deslocamento = (Integer)((ConcurrentHashMap<Integer, Object>) map).get(1);
			
			ConcurrentHashMap<Integer, float[]> memory = (ConcurrentHashMap<Integer, float[]>)readMemory(0);
			
			float[] m = memory.get(1);
			
			deslocamento = deslocamento * 2;
			
			for (int j = 0; j < temp.length; j++){
				m[j+deslocamento] += temp[j];
			}
			
			memory.put(1, m);
			this.data.put(0, memory);
			
		}
		writing = false;
	}

	/**Metodo para leitura de determinada posicao de memoria
	 * @param position 	Posicao da memoria a ser lida*/
	public Object readMemory(Integer position)
	{
		return data.get(position);
	}

	/**Metodo que retorna o tipo de determinada posicao de memoria
	 * @param position	Posicao a qual se verificar o tipo*/
	public String getType(Integer position)
	{
		return (String) this.type.get(position);
	}

	public int getMemorySize(){
		return this.data.size();
	}
	
	@SuppressWarnings("unchecked")
	public void adjustMemory(Set <Integer> newValues){
		Object [] positions = newValues.toArray();
		ConcurrentHashMap<Integer, Complex> writeMem = (ConcurrentHashMap<Integer, Complex>) this.data.get(1);
		ConcurrentHashMap<Integer, Complex> readMem = (ConcurrentHashMap<Integer, Complex>) this.data.get(0);
		for (int i=0; i<newValues.size(); i++)
			readMem.put((Integer)positions[i], writeMem.get(positions[i]));
		
//		ConcurrentHashMap<Integer, Object> temp;
//		temp = (ConcurrentHashMap<Integer, Object>) this.data.get(0);
//		this.data.replace(0, this.data.get(1));
//		this.data.replace(1, temp);
		eraseMemory(1);
	}
	
    @SuppressWarnings("unchecked")
	public void eraseMemory(int mem){
    	ConcurrentHashMap <Integer, Object> temp = (ConcurrentHashMap <Integer, Object>) this.data.get(mem); 
		for (int i = 0; i < temp.size(); i++)
			temp.replace(i, Complex.ZERO);
    }

}
