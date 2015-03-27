package g3pd.virdgm.core;

import g3pd.virdgm.types.VTArray;
import g3pd.virdgm.types.VTBoolean;
import g3pd.virdgm.types.VTDouble;
import g3pd.virdgm.types.VTFloat;
import g3pd.virdgm.types.VTInteger;
import g3pd.virdgm.types.VTList;
import g3pd.virdgm.types.VTLong;
import g3pd.virdgm.types.VTPoint;
import g3pd.virdgm.types.VTString;
import org.jscience.mathematics.number.Complex;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.*;


import javax.xml.parsers.*;
/**Classe responsavel pela interpretacao do arquivo descritor da memoria e mapeamento
 * dos estados de memoria na VirD-GM*/
public class VirdMemLoader {
	ConcurrentHashMap<Integer, Object>dataAttr = new ConcurrentHashMap<Integer, Object>();
	ConcurrentHashMap<Integer, String>typeAttr = new ConcurrentHashMap<Integer, String>();
	boolean GPU;
    int    dimensionsAttr;
    String inputFile;
    int    sizeAttr;

    /**Metodo que recebe o arquivo descritor da memoria e verifica sua integridade
     * @param inputFile		Nome do arquivo descritor da memoria*/
    public VirdMemLoader(String inputFile) {
    	
        this.inputFile = inputFile;

        try {
            OpenFile();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public VirdMemLoader(double num) {
    	System.out.println("Aqui");
    	num = Math.pow(2, num);
    	ConcurrentHashMap<Integer, Complex>temp = new ConcurrentHashMap<Integer, Complex>();
    	
    	for (int i = 0; i < num; i++) {
    		temp.put(i, Complex.ZERO);
    		typeAttr.put(i, "Complex");
        }
    	dataAttr.put(0, temp);
    }
    
    public VirdMemLoader(String inputFile, String library, boolean gpu) {
    	
        this.inputFile = inputFile;
        GPU = gpu;

        try {
            OpenFile("QGMAnalyzer");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**Metodo responsavel por abrir o arquivo descritor da memoria e inicializar o
     * parser interpretador
     * */
    public void OpenFile() throws Exception {
        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db       = dbf.newDocumentBuilder();
        Document               doc      = db.parse(inputFile);
        Element                elem     = doc.getDocumentElement();
        NodeList               nodelist = elem.getChildNodes();

        parseFile(nodelist);
    }
    
    public void OpenFile(String library) throws Exception {
        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db       = dbf.newDocumentBuilder();
        Document               doc      = db.parse(this.inputFile);
        Element                elem     = doc.getDocumentElement();
        NodeList               nodelist = elem.getChildNodes();

        parseFileAnalyzer(nodelist);
    }
    
    /**Metodo que retorna a dimensao da memoria*/
    public int getDimensionsAttr() {
        return dimensionsAttr;
    }
    /**Metodo que altera a dimensao da memoria
     * @param dimensionsAttr 	Dimensao da memoria */
    public void setDimensionsAttr(int dimensionsAttr) {
        this.dimensionsAttr = dimensionsAttr;
    }
    /**Metodo que retorna o tamanho da memoria*/
    public int getSizeAttr() {
        return sizeAttr;
    }
    /**Metodo que altera a dimensao da memoria
     * @param sizeAttr 	Tamanho da memoria*/
    public void setSizeAttr(int sizeAttr) {
        this.sizeAttr = sizeAttr;
    }
    /**Metodo que retorna o vetor de tipos da memoria*/
    public ConcurrentHashMap<Integer, String> getTypeAttr() {
        return typeAttr;
    }
    /**Metodo para alterar os tipos de memoria
     * @param typeAttr		Vetor com os novos tipos da memoria*/
    public void setTypeAttr(ConcurrentHashMap<Integer, String> typeAttr) {
        this.typeAttr = typeAttr;
    }
    /**Metodo que retorna o vetor de dados contidos na memoria*/
    public ConcurrentHashMap<Integer, Object> getDateAttr() {
        return dataAttr;
    }
    
    /**Metodo que altera o vetor de dados contidos na memoria
     * @param dateAttr		Novo vetor contendo os dados para a memoria*/
    public void setDateAttr(ConcurrentHashMap<Integer, Object> dateAttr) {
        this.dataAttr = dateAttr;
        
    }

    /**Metodo responsavel pela interpretacao dos tipos contidos na memoria, 
     * representados na tag < valores > do arquivo descritor da memoria
     * @param typeAttr	String contendo os tipos de cada posicao de memoria*/
    private ConcurrentHashMap<Integer, String> parseTypeAttr(String typeAttr) throws Exception {
    	ConcurrentHashMap<Integer, String>typeValues = new ConcurrentHashMap<Integer, String>();
    	String[] typeList   = typeAttr.split(",");
        for (int i = 0; i < typeList.length; i++)
        	typeValues.put(i, typeList[i].trim());
    	
        return typeValues;
    }

    /**Metodo responsavel pela interpretacao dos dados contidos na memoria,
     * representados na tag <dados> do arquivo descritor da memoria
     * @param dateAttr	String contendo os dados de cada posicao de memoria*/
    private ConcurrentHashMap<Integer, Object> parseDateAttr(String dateAttr) throws Exception {
    	ConcurrentHashMap<Integer, Object> dataValues = new ConcurrentHashMap<Integer, Object>();
    	StringTokenizer st = new StringTokenizer(dateAttr, ",");
    	String str;
    	
    	Complex c;
    	float data[] = new float[this.sizeAttr*2];
    	//
    	float data2[] = new float[this.sizeAttr*2];
    	
    	int lastIndex, fromIndex;
    	
    	fromIndex = dateAttr.length();
    	
    	if (GPU){
    		
    		int i = this.sizeAttr - 1;
    		while (i>=0){
    			lastIndex = dateAttr.lastIndexOf(',', fromIndex - 1);
    			str = dateAttr.substring(lastIndex+1, fromIndex);
    			
    			
    			fromIndex = lastIndex;
    			
    			Object memoryValue = applyValue(str, 0);
    			if (memoryValue != null) {
            		c = (Complex) memoryValue;
            		data[i*2] = (float)c.getReal();
            		data2[i*2] = 0;
            		data[i*2 +1] = (float)c.getImaginary();
            		data2[i*2 +1] = 0;
            	}
            	else {
            		
            	}
    			i--;
    		}
    		dataValues.put(0, data);
    		dataValues.put(1, data2);
    	}
    	else{
    		int i = 0;
    		while (st.hasMoreTokens()){
	        	Object memoryValue = applyValue(st.nextToken(), 0);
	        	
	        	if (memoryValue != null) {
        			dataValues.put(i, memoryValue);
	        	}
	        	else {
	        	}
	        	i++;
	        }
    	}
                
        return dataValues;
    }
    
    /**Metodo responsavel pela interpretacao dos dados contidos na memoria,
     * representados na tag <dados> do arquivo descritor da memoria
     * @param dateAttr	String contendo os dados de cada posicao de memoria*/
    private ConcurrentHashMap<Integer, Object> parseDateAttrAnalyzer(String dateAttr, int index, int procs) throws Exception {
    	ConcurrentHashMap<Integer, Object> dataValues = new ConcurrentHashMap<Integer, Object>();
    	String[] dataList   = dateAttr.split(",");
    	int range = dataList.length/procs;
    	int resto = dataList.length%procs;
    	int add = 0;
    	
		if(resto>index){
			add = 1;
		}
		
        for (int i = 0; i < range+add; i++) {
        	Object memoryValue = applyValue(dataList[(range*index)+i].trim(), i);
        	
        	if (memoryValue != null) {
        	dataValues.put(i, memoryValue);
        	
        	}
        	
        	else {
        	}       	
        }	 
        return dataValues;
    }
    
    /**Metodo responsavel por inicializar cada tipo identificado no arquivo descritor
     * da memoria, com o tipos da VirD-GM, associando seu valor.
     * @param val	Valor encontrado na posicao
     * @param pos 	Posicao onde se encontra o valor*/
    private Object applyValue(String val, int pos){
    	if (this.typeAttr.get(pos).equals("Integer")){
    		return new VTInteger(val);
    	}
    	
    	else if (this.typeAttr.get(pos).equals("Long")) {
    		return new VTLong(val);
    	}

    	else if (this.typeAttr.get(pos).equals("Float")) {
    		return new VTFloat(val);
    	}

    	else if (this.typeAttr.get(pos).equals("Double")) {
    		return new VTDouble(val);
    	}
    	
    	else if (this.typeAttr.get(pos).equals("String")) {
    		return new VTString(val);
    	}

    	else if (this.typeAttr.get(pos).equals("Boolean")) {
    		return new VTBoolean(val);
    	}
    	
    	else if (this.typeAttr.get(pos).equals("Point")) {
    		return new VTPoint(val);
    	}
    	
    	else if (this.typeAttr.get(pos).equals("Array")) {
    		String [] vl = val.split(";");
    		try{
    			int dim = Integer.parseInt(vl[0]);
        		int tam = Integer.parseInt(vl[1]);
        		return new VTArray(dim,tam);
        		
    		}catch (Exception e) {
				// TODO: handle exception    
    			return new VTArray(val);
    		} 
    	}
    	
    	else if (this.typeAttr.get(pos).equals("List")) {
    		return new VTList(val);
    	}
    	else if (this.typeAttr.get(pos).equals("Complex")) {
    		val = val.replace("(", "");
    		val = val.replace(")", "");
    		val = val.replace("j", "");
    		String [] parts = val.split("\\+");
    		Complex value;
    		double real = Double.parseDouble(parts[0]);
    		if(parts.length == 2){
    			double imaginary = Double.parseDouble(parts[1]);
    			value = Complex.valueOf(real, imaginary);
    		}else{
    			value = Complex.valueOf(real, 0);
    		}

    		return  value;
    	}else if (this.typeAttr.get(pos).equals("Vector")) {
    		
    		val = val.replace("[", "");
    		val = val.replace("]", "");
    		
    		String [] parts = val.split(";");
    		
    		List value = new ArrayList();
    		for (String s : parts){
    			value.add(s);
    		}
    		return  value;
    	}
    	
    	return null;
    }
    /**Interpretador do arquivo descritor da memoria, responsavel por identificar
     * a dimensao, tamanho, tipos e dados da memoria*/
    private void parseFile(NodeList nl) throws Exception {
        for (int i = 0; i < nl.getLength(); i++) {
            Node nodes = nl.item(i);

            if (nodes.getNodeType() == Node.ELEMENT_NODE) {

                if (nodes.getNodeName().equals("posicao")) {
                    String pos1 = ((Element) nodes).getAttribute("dimensao");
                    String pos2 = ((Element) nodes).getAttribute("tamanho");
                    setDimensionsAttr(Integer.parseInt(pos1));
                    setSizeAttr(Integer.parseInt(pos2));
                }

                else if (nodes.getNodeName().equals("valores")) {
                    NodeList nodo = nodes.getChildNodes();
                    String   val  = nodo.item(0).getNodeValue();
                    setTypeAttr(parseTypeAttr(val));
                }

                else if (nodes.getNodeName().equals("dados")) {
                    Node   nodo = nodes.getChildNodes().item(0);
                    String vals = nodo.getNodeValue();
                    setDateAttr(parseDateAttr(vals));
                    
                }
                else {
                }
            }
        }
    }
    
    private void parseFileAnalyzer(NodeList nl) throws Exception {
    	ConcurrentHashMap<Integer, Object> memory = new ConcurrentHashMap<Integer, Object>();
        for (int i = 0; i < nl.getLength(); i++) {
            Node nodes = nl.item(i);

            if (nodes.getNodeType() == Node.ELEMENT_NODE) {

                if (nodes.getNodeName().equals("posicao")) {
                    String pos1 = ((Element) nodes).getAttribute("dimensao");
                    String pos2 = ((Element) nodes).getAttribute("tamanho");
                    setDimensionsAttr(Integer.parseInt(pos1));
                    setSizeAttr(Integer.parseInt(pos2));
                }
               
                else if (nodes.getNodeName().equals("valores")) {
                    NodeList nodo = nodes.getChildNodes();
                    ConcurrentHashMap <Integer, String> val = new ConcurrentHashMap <Integer, String>();
                    val.put(0, "Complex");
                    setTypeAttr(val);
                }
                
                else if (nodes.getNodeName().equals("dados")) {
                    Node   nodo = nodes.getChildNodes().item(0);
                    String vals = nodo.getNodeValue();
                    
                    ConcurrentHashMap<Integer, Object> temp;
                    temp = parseDateAttr(vals);
                    
                    memory.put(0, temp);
                    
                    setDateAttr(memory);
                }
                else {
                }
            }
        }
    }
    
    public void eraseMemory(ConcurrentHashMap<Integer, Object> temp, int size){
    	if (temp.isEmpty()){
    		for (int i = 0; i < size; i++){
    			temp.put(i, Complex.ZERO);
    		}
    	}
    	else{
    		for (int i = 0; i < size; i++){
    			temp.replace(i, Complex.ZERO);
    		}	
    	}
    }
    
}
