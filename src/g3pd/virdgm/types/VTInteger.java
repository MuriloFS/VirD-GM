package g3pd.virdgm.types;


public class VTInteger extends VTObject {

	private static final long serialVersionUID = 4277675977762518386L;
	public Integer value;

	public VTInteger() {
		value = new Integer(0);
	}
	
	public VTInteger(Integer value) {
		this.value = value;
	}
	
	public VTInteger(String val) {
		parseValue(val);
	}

	public void add(VTInteger vi) {
		value += vi.getValue();
	}
	
	public void sub(VTInteger vi) {
		value -= vi.getValue();
	}
	
	public void mul(VTInteger vi) {
		this.value *= vi.getValue();
	}
	
	public void div(VTInteger vi) {
		if(vi.getValue() != 0) {
			value /= vi.getValue();
		}
	}

	private void parseValue(String val) {
		value = Integer.parseInt(val);
		
	}
	
	public Integer getValue() {
		return value;
	}

	public void setValue(Integer val) {

		value = val;			
	}

	public String toString() {

		return "Integer: " + value;
	}
}
