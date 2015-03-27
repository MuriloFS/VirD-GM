package g3pd.virdgm.types;

public class VTDouble extends VTObject {

	private static final long serialVersionUID = 919263325842138922L;
	public Double value;

	public VTDouble() {
		value = new Double(0);
	}
	
	public VTDouble(String val) {
		parseValue(val);
	}

	public void add(VTDouble vd) {
		value += vd.getValue();
	}
	
	public void sub(VTDouble vd) {
		value -= vd.getValue();
	}
	
	public void mul(VTDouble vd) {
		value *= vd.getValue();
	}
	
	public void div(VTDouble vd) {
		if(vd.getValue() != 0) {
			value /= vd.getValue();
		}
	}

	private void parseValue(String val) {
		
		value = Double.parseDouble(val);
	}
	
	public Double getValue() {

		return value;
	}

	public void setValue(Double val) {

		value = val;			
	}

	public String toString() {

		return "Double: " + value;
	}

}