package g3pd.virdgm.apps;

import java.util.Enumeration;
import java.util.Vector;

import org.jscience.mathematics.number.Complex;

public class Page {
	public int dim;
	public Vector <Bloco> blocos;
	
	public void print(){
		Bloco bl;
		Integer pos;
		Complex complex;		
		
		Enumeration <Bloco> b;
		Enumeration <Integer> p;
		Enumeration <Complex> c;
		
		System.out.print("PAGE:\n[");
		for (b = blocos.elements() ; b.hasMoreElements(); ){
			System.out.print("[");
			bl = b.nextElement();

			System.out.print("[" + bl.VPPid + ", ");
			c = bl.complex.elements();
			for (p = bl.pos.elements(); p.hasMoreElements();){
				pos = p.nextElement();
				complex = c.nextElement();
				
				System.out.print("[" + "(" + complex + ")" + ", " + pos + "]");
				
				if (p.hasMoreElements()) System.out.print(", ");
			}
			
			System.out.print("]");
			if (b.hasMoreElements()) System.out.print(", ");
		}
		System.out.println("]");
	}
}
