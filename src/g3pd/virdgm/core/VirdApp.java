package g3pd.virdgm.core;
/**
** Interface para definicao de uma biblioteca
**/
import java.io.IOException;
import java.io.ObjectOutputStream;

public interface VirdApp {
	/**O metodo app contem os parametros da aplicacao*/
	public void app(String valueAttr, String input, String outputPosAttr, String controlListAttr, Integer iterator, VirdMemory memory, ObjectOutputStream oos) throws IOException;
	public void app(Integer value, Integer [] input, String outputPosAttr, Integer iterator, VirdMemory memory, ObjectOutputStream oos) throws IOException;
	
}
