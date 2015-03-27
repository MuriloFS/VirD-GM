package g3pd.virdgm.types;

public class VTLong extends VTObject {

	private static final long serialVersionUID = -2774488159465524529L;
	public Long value;

	public VTLong() {
		value = new Long(0);
	}
	
	public VTLong(String val) {
		parseValue(val);
	}

	public void add(VTLong vl) {
		value += vl.getValue();
	}
	
	public void sub(VTLong vl) {
		value -= vl.getValue();
	}
	
	public void mul(VTLong vl) {
		value *= vl.getValue();
	}
	
	public void div(VTLong vl) {
		if(vl.getValue() != 0) {
			value /= vl.getValue();
		}
	}
	
	private void parseValue(String val) {
		
		value = Long.parseLong(val);
	}
	
	public Long getValue() {

		return value;
	}

	public void setValue(Long val) {

		value = val;			
	}

	public String toString() {

		return "Long: " + value;
	}

}