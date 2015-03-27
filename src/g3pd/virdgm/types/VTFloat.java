package g3pd.virdgm.types;

public class VTFloat extends VTObject {

	private static final long serialVersionUID = -3087983793926829843L;
	public Float value;

	public VTFloat() {
		value = new Float(0);
	}


	public VTFloat(String val) {
		parseValue(val);
	}

	public void add(VTFloat vf) {
		value += vf.getValue();
	}

	public void sub(VTFloat vf) {
		value -= vf.getValue();
	}

	public void mul(VTFloat vf) {
		value *= vf.getValue();
	}

	public void div(VTFloat vf) {
		if(vf.getValue() != 0) {
			value /= vf.getValue();
		}
	}

	private void parseValue(String val) {

		value = Float.parseFloat(val);
	}

	public Float getValue() {

		return value;
	}

	public void setValue(Float val) {

		value = val;
	}

	public String toString() {

		return "Float: " + value;
	}

}