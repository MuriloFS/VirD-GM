package g3pd.virdgm.types;

public class VTPoint extends VTObject {

	private static final long serialVersionUID = -6650625346656100654L;
	Double x;
	Double y;
	Double z;
	
	public VTPoint() {
		x = new Double("0");
		y = new Double("0");
		z = new Double("0");
	}
	
	public VTPoint(String val) {
		parseValue(val);
	}

	public VTDouble distance(VTPoint vp) {
		
		Double dx = vp.getX() - this.x;
		Double dy = vp.getY() - this.y;
		Double dz = vp.getZ() - this.z;
		
		VTDouble distance = new VTDouble();
		
		distance.setValue(Math.sqrt(dx*dx + dy*dy + dz*dz));
		
		return distance;
	}
	
	public Double getX() {
		return this.x;
	}

	public Double getY() {
		return this.y;
	}

	public Double getZ() {
		return this.z;
	}

	
	private void parseValue(String val) {
		String [] values = val.split(":");
		if (values.length == 3) {
			x = new Double(values[0]);
			y = new Double(values[1]);
			z = new Double(values[2]);
		}
		
	}
	


	public void setX(Double x) {
		this.x = x;
	}
	
	public void setY(Double y) {
		this.y = y;
	}
	
	public void setZ(Double z) {
		this.z = z;
	}


	public String toString() {

		return "VirdPoint: x = " + this.x + " y = " + this.y + " z = " + this.z;
	}

}