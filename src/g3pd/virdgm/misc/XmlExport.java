package g3pd.virdgm.misc;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jscience.mathematics.number.Complex;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import g3pd.virdgm.core.VirdLauncher;
import g3pd.virdgm.core.VirdMemory;

public class XmlExport {
	public void XmlExport(){
		
	}
	public void exportXML(VirdMemory memory){
		String types = new String("Complex");
		String values = new String("");
		int size = memory.getMemorySize()/2;
		ConcurrentHashMap<Integer, Complex> mem;
		if (size == 0) size = 1;
		for(int i=0;i<size;i++){
			mem = (ConcurrentHashMap<Integer, Complex>)memory.readMemory(i);
			
			for (int j=0; j<mem.size();j++){
				values += String.valueOf(mem.get(j)) + ", ";
			}
		}
		
		values = values.substring(0,values.length()-2);
		exportResult(types,values,-1);
	}
	
	public static void exportResult(String types, String values, Integer priority){
		if(priority <= VirdLauncher.priority){			
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder docBuilder = null;
			try {
				docBuilder = factory.newDocumentBuilder();
			} catch (ParserConfigurationException e){
				e.printStackTrace();}
			
		    //Create blank DOM Document
		    Document doc = docBuilder.newDocument();
		    
		    //Create the elements of the XML
		    Element memory = doc.createElement("memoria");
		    Element position = doc.createElement("posicao");
		    Element type = doc.createElement("valores");
			Element data = doc.createElement("dados");
			
			//Add the 'memoria' element as root
		    doc.appendChild(memory);
		    
		    //Set the attributes of element 'posicao' and add as child of 'memoria' element
		    position.setAttribute("dimensao",String.valueOf(1));
		    position.setAttribute("tamanho",String.valueOf(types.split(",").length));
		    memory.appendChild(position);

		    //Set the text nodes of 'valores' e 'dados' and add as child of 'memoria' element
			Node nodo = doc.createTextNode(types);
			Node nodo2 = doc.createTextNode(values);
			type.appendChild(nodo);
			data.appendChild(nodo2);
			memory.appendChild(type);
			memory.appendChild(data);
		    
			//Save the document with the informations 
		    TransformerFactory tranFactory = TransformerFactory.newInstance(); 
		    Transformer aTransformer = null;
			try {
				aTransformer = tranFactory.newTransformer();
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();}			 

		    Source src = new DOMSource(doc);
		    File f = new File(System.getProperty("user.dir"));
		    Result dest = new StreamResult(f.getParent()+"/tempFiles/tempMemory.xml");
		    try {
				aTransformer.transform(src, dest);
			} catch (TransformerException e) {
				e.printStackTrace();}
		}
	}
}
