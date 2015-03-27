package g3pd.virdgm.types;
import org.jscience.mathematics.number.Complex;

public class VTComplex extends VTObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6442397426880213385L;
	Complex value;
	
	public VTComplex(){
		value = Complex.ZERO;
	}
	
	public VTComplex(Complex val){
		value = val;
	}
	
	public VTComplex(String val){
		
		val = val.replace("(", "");
		val = val.replace(")", "");
		val = val.replace("j", "");
		String [] parts = val.split("\\+");
		double real = Double.parseDouble(parts[0]);
		double imaginary = Double.parseDouble(parts[1]);
		value = Complex.valueOf(real, imaginary);
	}
	
	public Complex GetValue(){
		return value;
	}
	public Double getReal (){
		return value.getReal();
	}
	public void Add(VTComplex value2){
		value = value.plus(value2.GetValue());
	}
	
	public void Mul(VTComplex value2){
		value = value.times(value2.GetValue());
	}
	
	public void Div(VTComplex value2){
		value = value.divide(value2.GetValue());
	}
	
	public void Div(float value2){
		value = value.divide(value2);
	}
	
	public void Pow(Integer value2){
		value = value.pow(value2);
	}
}
	