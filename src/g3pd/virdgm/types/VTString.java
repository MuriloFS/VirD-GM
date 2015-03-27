package g3pd.virdgm.types;

public class VTString extends VTObject {

	private static final long serialVersionUID = -6650625346656100654L;
	String value;
	
	public VTString() {
		value = new String("");
	}
	
	public VTString(String val) {
		parseValue(val);
	}

	public void add(VTString vs) {
		value += vs.getValue();
	}

	private void parseValue(String val) {
		value = val;
	}
	
	public String getValue() {

		return value;
	}

	public void setValue(String val) {

		value = val;			
	}

	public String toString() {

		return "String: " + value;
	}

}