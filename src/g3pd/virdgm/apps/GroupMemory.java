package g3pd.virdgm.apps;

import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.io.ObjectOutputStream;
import g3pd.virdgm.core.VirdLauncher;
import g3pd.virdgm.core.VirdApp;
import g3pd.virdgm.core.VirdMemory;

public class GroupMemory implements VirdApp {

	public void app(Integer value, Integer[] input, String outputPosAttr, Integer iterator, VirdMemory memory, ObjectOutputStream oos) throws IOException {
		Integer output = 0;
		if (iterator == -1){
			System.out.println("GroupMemory Processo Quântico");
		}
		//else{
			//output = Integer.parseInt(outputPosAttr);
		
			int numProcs = memory.getMemorySize()/2;
			ConcurrentHashMap<Integer, Object> hash = (ConcurrentHashMap<Integer, Object>)memory.readMemory(output+numProcs);
			ConcurrentHashMap<Integer, Object> result = new ConcurrentHashMap<Integer, Object>();
			for(int j=0; j<hash.size(); j++)
				result.put(j, hash.get(j));
			memory.updateMemory(result, output);
		//}
	}
	
	public void app(String valueAttr, String input, String outputPosAttr,
			Integer iterator, VirdMemory memory, ObjectOutputStream oos) throws IOException {		
	}
}