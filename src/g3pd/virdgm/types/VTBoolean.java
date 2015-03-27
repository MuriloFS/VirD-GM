package g3pd.virdgm.types;

public class VTBoolean extends VTObject {

	private static final long serialVersionUID = -6865792527857130867L;
	Boolean value;

	public VTBoolean(String val) {
		parseValue(val);
	}
	
	private void parseValue(String val) {
		value = Boolean.parseBoolean(val);
	}

	public Boolean getValue() {

		return value;
	}

	public void setValue(Boolean val) {

		value = val;			
	}

	public String toString() {

		return "Boolean: " + value;
	}

}