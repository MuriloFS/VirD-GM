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

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import org.jscience.mathematics.number.Complex;
import org.w3c.dom.*;


import javax.xml.parsers.*;

/**Classe responsavel pela interpretacao do arquivo descritor da memoria e mapeamento
 * dos estados de memoria na VirD-GM*/
public class VirdMemLoader {
	ConcurrentHashMap <Integer,Object>	dataAttr 		= new ConcurrentHashMap <Integer,Object>();
	ConcurrentHashMap <Integer,String>	typeAttr 		= new ConcurrentHashMap <Integer,String>();
	boolean								GPU;
    int									dimensionsAttr;
    String								inputFile;
    int									sizeAttr;
    
    /**Metodo que recebe o arquivo descritor da memoria e verifica sua integridade
     * @param inputFile		Nome do arquivo descritor da memoria*/
    public VirdMemLoader(String inputFile) {
    	//VirdLogger.timeLogger("Inicializando interpretador de memoria",1);
    	
        this.inputFile = inputFile;

        try {
        	//VirdLogger.timeLogger("VirdMemLoader: abrindo arquivo de memoria ->  " + inputFile,1);
            OpenFile();
            //validateArgs();
            
          //  VirdLogger.timeLogger("VirdMemLoader: arquivo de memoria lido... saindo ->  " + inputFile,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public VirdMemLoader(double num) {
    	//VirdLogger.timeLogger("Inicializando interpretador de memoria",1);
    	//System.out.println("Aqui");
    	num = Math.pow(2, num);
    	ConcurrentHashMap<Integer, Complex>temp = new ConcurrentHashMap<Integer, Complex>();
    	
    	for (int i = 0; i < num; i++) {
    		temp.put(i, Complex.ZERO);
    		typeAttr.put(i, "Complex");
        }
    	dataAttr.put(0, temp);
    }
    
    public VirdMemLoader(String inputFile, String library, boolean gpu) {
    	//VirdLogger.timeLogger("Inicializando interpretador de memoria",1);
    	
        this.inputFile = inputFile;
        GPU = gpu;

        try {
        	//VirdLogger.timeLogger("VirdMemLoader: abrindo arquivo de memoria ->  " + inputFile,1);
            OpenFile(library);
            //validateArgs();
            
          //  VirdLogger.timeLogger("VirdMemLoader: arquivo de memoria lido... saindo ->  " + inputFile,1);
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

      //  VirdLogger.timeLogger("VirdMemLoader: inicializando interpretador XML ",1);
        parseFile(nodelist);
      //  VirdLogger.timeLogger("VirdMemLoader: finalizando interpretador XML ",1);
    }
    
    public void OpenFile(String library) throws Exception {
        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db       = dbf.newDocumentBuilder();
        Document               doc      = db.parse(this.inputFile);
        Element                elem     = doc.getDocumentElement();
        NodeList               nodelist = elem.getChildNodes();

      //  VirdLogger.timeLogger("VirdMemLoader: inicializando interpretador XML ",1);
        parseFileAnalyzer(nodelist);
      //  VirdLogger.timeLogger("VirdMemLoader: finalizando interpretador XML ",1);
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
    	/*for (int i = 0; typeList[i].trim().equals("-") == false ; i++)
    	System.out.println("+++++++++++++TYPE LIST: "+typeList[i]);
        for (int i = 0; typeList[i].trim().equals("-") == false ; i++)
        	typeValues.add(typeList[i].trim());*/

		//ADRIANO
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
    			
    			//System.out.println(str);
    			
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
            		//VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
            	}
    			i--;
    		}
    		/*
    		for (int i = 0; i < this.getSizeAttr(); i++) {
    			
    			
            	Object memoryValue = applyValue(dataList[i].trim(), 0);
            	
            	if (memoryValue != null) {
            		c = (Complex) memoryValue;
            		data[i*2] = (float)c.getReal();
            		data2[i*2] = 0;
            		data[i*2 +1] = (float)c.getImaginary();
            		data2[i*2 +1] = 0;
            	}
            	else {
            		//VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
            	}       	
            }*/
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
	        		//VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
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
		/*for (int i = 0; dataList[i].trim().equals("-") == false ; i++)
    	System.out.println("+++++++++++++DATA LIST: "+dataList[i]);
        for (int i = 0; dataList[i].trim().equals("-") == false ; i++) {
        	Object memoryValue = applyValue(dataList[i].trim(), i);        	
        	if (memoryValue != null) {
        	dataValues.add(memoryValue);
        	}        	
        	else {
        		VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
        	}       	
        }*/
		//ADRIANO
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
        		//VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
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
    	//VirdLogger.timeLogger("VirdMemLoader: erro ao criar objeto do tipo " + 
    		//	this.typeAttr.get(pos) + " na posicao: " + pos,1);
    	
    	return null;
    }
    /**Interpretador do arquivo descritor da memoria, responsavel por identificar
     * a dimensao, tamanho, tipos e dados da memoria*/
    private void parseFile(NodeList nl) throws Exception {
    	//VirdLogger.timeLogger("VirdMemLoader: interpretador XML iniciado",1);
        for (int i = 0; i < nl.getLength(); i++) {
            Node nodes = nl.item(i);

            if (nodes.getNodeType() == Node.ELEMENT_NODE) {

                if (nodes.getNodeName().equals("posicao")) {
                    String pos1 = ((Element) nodes).getAttribute("dimensao");
                    String pos2 = ((Element) nodes).getAttribute("tamanho");
                   // VirdLogger.timeLogger("VirdMemLoader: configuracao de memoria: dimensao -> " + 
                    		//pos1 + " tamanho " + pos2,1);

                    setDimensionsAttr(Integer.parseInt(pos1));
                    setSizeAttr(Integer.parseInt(pos2));
                }

                else if (nodes.getNodeName().equals("valores")) {
                	//VirdLogger.timeLogger("VirdMemLoader: obtendo tipo de dado da memoria ",1);
                    NodeList nodo = nodes.getChildNodes();
                    String   val  = nodo.item(0).getNodeValue();
                    
                   // VirdLogger.timeLogger("VirdMemLoader: dados da memoria sao do tipo: " + val,1);

                    setTypeAttr(parseTypeAttr(val));
                }

                else if (nodes.getNodeName().equals("dados")) {
                //	VirdLogger.timeLogger("VirdMemLoader: Obtendo valores de memoria do XML ",1);
                    Node   nodo = nodes.getChildNodes().item(0);
                    String vals = nodo.getNodeValue();
                    
                  //  VirdLogger.timeLogger("VirdMemLoader: valores de memoria: " + vals,1);
                    
                    setDateAttr(parseDateAttr(vals));
                    
                }
                else {
                //	VirdLogger.timeLogger("VirdMemLoader: Erro no arquivo de memoria: XML " +
                		//	"tag desconhecida " + nodes.getNodeName(),1);
                	
                }
            }
        }
    }
    
    private void parseFileAnalyzer(NodeList nl) throws Exception {
    	ConcurrentHashMap<Integer, Object> memory = new ConcurrentHashMap<Integer, Object>();
    	//VirdLogger.timeLogger("VirdMemLoader: interpretador XML iniciado",1);
        for (int i = 0; i < nl.getLength(); i++) {
            Node nodes = nl.item(i);

            if (nodes.getNodeType() == Node.ELEMENT_NODE) {

                if (nodes.getNodeName().equals("posicao")) {
                    String pos1 = ((Element) nodes).getAttribute("dimensao");
                    String pos2 = ((Element) nodes).getAttribute("tamanho");
                   // VirdLogger.timeLogger("VirdMemLoader: configuracao de memoria: dimensao -> " + 
                    		//pos1 + " tamanho " + pos2,1);

                    setDimensionsAttr(Integer.parseInt(pos1));
                    setSizeAttr(Integer.parseInt(pos2));
                }
               
                else if (nodes.getNodeName().equals("valores")) {
                	//VirdLogger.timeLogger("VirdMemLoader: obtendo tipo de dado da memoria ",1);
                    NodeList nodo = nodes.getChildNodes();
                    //String   val  = nodo.item(0).getNodeValue();
                    
                   // VirdLogger.timeLogger("VirdMemLoader: dados da memoria sao do tipo: " + val,1);
                    
                    ConcurrentHashMap <Integer, String> val = new ConcurrentHashMap <Integer, String>();
                    val.put(0, "Complex");
                    
                    setTypeAttr(val);
                }
                
                else if (nodes.getNodeName().equals("dados")) {
                //	VirdLogger.timeLogger("VirdMemLoader: Obtendo valores de memoria do XML ",1);
                    Node   nodo = nodes.getChildNodes().item(0);
                    String vals = nodo.getNodeValue();
                    
                    ConcurrentHashMap<Integer, Object> temp;
                    temp = parseDateAttr(vals);
                    
                    memory.put(0, temp);
                    
                    setDateAttr(memory);
                    
                  //  VirdLogger.timeLogger("VirdMemLoader: valores de memoria: " + vals,1);
                    //comentar esta parte e realizar o processo de moria normal 0 com a normal 1 com tudo zero para os resultados;
                    /*
                    int procs = 0;
                    try {  
                        //FileReader para o arquivo:  
                    	File f = new File(System.getProperty("user.dir"));
                    	FileReader fr = new FileReader(f.getParent()+"/tempFiles/hosts.txt");  
                        //BufferedReader para o FileReader:  
                        BufferedReader br = new BufferedReader(fr);  
                        String temp;
                        //A cada iteração, l~e uma linha do arquivo e atribui-a a temp:  
                        while ((temp = br.readLine()) != null) {
                            temp = temp.split(":")[1];
                            procs += Integer.parseInt(temp);
                        }  
                    }  
                    catch (FileNotFoundException e1) {  
                        System.out.println("File not found!");  
                    }
                    
                    ConcurrentHashMap<Integer, Object> temp;
                    for(int k=0; k<procs; k++){
		                temp = parseDateAttrAnalyzer(vals,k,procs);
		                memory.put(k, temp);
                    }
                    
                    for (int j = procs; j < 2*procs; j++)
                    	memory.put(j, new ConcurrentHashMap<Integer, Object>());
                    setDateAttr(memory);
                    */
                }
                else {
                //	VirdLogger.timeLogger("VirdMemLoader: Erro no arquivo de memoria: XML " +
                		//	"tag desconhecida " + nodes.getNodeName(),1);
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
