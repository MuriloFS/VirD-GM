package g3pd.virdgm.types;

import java.io.Serializable;
import java.util.Vector;

public class VTObject implements Serializable {

	private static final long serialVersionUID = -7583389449534378070L;
	private Vector<Object> myo;


	public VTObject() {
		myo = new Vector<Object>();
	}

	public void addObject(Object obj)
	{
		myo.add(obj);
	}

	public Object getObject(Integer pos)
	{
		return myo.get(pos);
	}

}
