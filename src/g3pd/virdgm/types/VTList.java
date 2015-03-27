package g3pd.virdgm.types;

//import g3pd.virdgm.misc.VirdLogger;

import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VTList extends VTObject {

	private static final long serialVersionUID = 6649732450722049927L;
	
	Vector  dataAttr;
    int    dimensionsAttr;
    String inputFile;
    int    sizeAttr;
    Vector typeAttr;
    
    VTObject []   array1;
    Object [][] array2;
	Object [][][] array3;

    public VTList(String inputFile) {
    	//VirdLogger.timeLogger("Inicializando interpretador de memoria",1);
    	
        this.inputFile = inputFile;

        try {
        	//VirdLogger.timeLogger("VirdMemLoader: abrindo arquivo de memoria ->  " + inputFile,1);
            OpenFile();
            
           // VirdLogger.timeLogger("VirdMemLoader: arquivo de memoria lido... saindo ->  " + inputFile,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void parseValue(String val) {
    	if(dimensionsAttr == 1) {
    		array1 = new VTObject[sizeAttr];
    		
    		//VirdLogger.timeLogger("VirdMemLoader: array ::::: " + array1.length,1);
    		
        	String[] dataList   = val.split(",");

            for (int i = 0; dataList[i].trim().equals("-") == false ; i++) {
        		//VirdLogger.timeLogger("VirdMemLoader: array ::::: " + dataList[0],1);
        		//VirdLogger.timeLogger("VirdMemLoader: array ::::: " + dataList[1],1);
        		//VirdLogger.timeLogger("VirdMemLoader: array ::::: " + dataList[2],1);
        		//VirdLogger.timeLogger("VirdMemLoader: array ::::: " + dataList[3],1);
        		VTObject memoryValue = applyValue(dataList[i].trim(), i);
            	
            	if (memoryValue != null) {
            		array1[i] = memoryValue;
            	
            	}
            	
            	else {
            		//VirdLogger.timeLogger("VirdMemLoader: FALHA ao acrescentar valor na memoria",1);
            	}       	
            }
    		
    	}
    	
    	else if (dimensionsAttr == 2) {
    		array2 = new Object[sizeAttr][sizeAttr];
    		String[] dataLines   = val.split("-");
    		
    		
            for (int i = 0; i < sizeAttr; i++) {
            	String[] dataList   = dataLines[i].split(",");

                for (int j = 0; j < sizeAttr ; j++) {
                	Object memoryValue = applyValue(dataList[j].trim(), j);

                	array2[i][j] = memoryValue;   	
                }
            	
            } 		
    		
    	}
    	
    	else if (dimensionsAttr == 3) {
    		String separator = "";
    		
    		for(int i = 0; i < sizeAttr; i++) {
    			separator += "-, ";
    		}
    		
    		separator += "-";
    		    		
    		array3 = new Object[sizeAttr][sizeAttr][sizeAttr];
    		
    		String[] dataDimen   = val.split(separator);
    		
    		for(int k = 0; k < sizeAttr; k++) {
    				    		
	    		String [] dataLines = dataDimen[k].split("-");
	    		
	            for (int i = 0; i < sizeAttr; i++) {
	            	String[] dataList   = dataLines[i].split(",");
	
	                for (int j = 0; j < sizeAttr ; j++) {
	                	Object memoryValue = applyValue(dataList[j].trim(), j);
	
	                	array3[k][i][j] = memoryValue;   	
	                }
	            }
    		}
    	}
    }
    
    public void OpenFile() throws Exception {
        DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
        DocumentBuilder        db       = dbf.newDocumentBuilder();
        Document               doc      = db.parse(inputFile);
        Element                elem     = doc.getDocumentElement();
        NodeList               nodelist = elem.getChildNodes();

       // VirdLogger.timeLogger("VirdMemLoader: inicializando interpretador XML ",1);
        parseFile(nodelist);
       // VirdLogger.timeLogger("VirdMemLoader: finalizando interpretador XML ",1);
    }

    public int getDimensionsAttr() {
        return dimensionsAttr;
    }

    public void setDimensionsAttr(int dimensionsAttr) {
        this.dimensionsAttr = dimensionsAttr;
    }

    public int getSizeAttr() {
        return sizeAttr;
    }

    public void setSizeAttr(int sizeAttr) {
        this.sizeAttr = sizeAttr;
    }

    public Vector getTypeAttr() {
        return typeAttr;
    }

    public void setTypeAttr(Vector typeAttr) {
        this.typeAttr = typeAttr;
    }
    
    

    private Vector parseTypeAttr(String typeAttr) throws Exception {
    	Vector typeValues = new Vector();
    	String[] typeList   = typeAttr.split(",");

        for (int i = 0; typeList[i].trim().equals("-") == false ; i++)
        	typeValues.add(typeList[i].trim());         

        return typeValues;
    }
    
    
    
    public Vector getDateAttr() {
        return dataAttr;
    }

    public void setDateAttr(Vector dateAttr) {
        this.dataAttr = dateAttr;
        
    }


    private VTObject applyValue(String val, int pos) {
    	if (this.typeAttr.get(pos).equals("Integer")) {
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
    		return new VTArray(val);
    	}
    	
    	else if (this.typeAttr.get(pos).equals("List")) {
    		return new VTList(val);
    	}
    	
    	//VirdLogger.timeLogger("VirdMemLoader: erro ao criar objeto do tipo " + this.typeAttr.get(pos) + " na posicao: " + pos,1);
    	
    	return null;
    }
    

    
    private void parseFile(NodeList nl) throws Exception {
    	//VirdLogger.timeLogger("VirdMemLoader: interpretador XML iniciado",1);
    	String vals = "";
        for (int i = 0; i < nl.getLength(); i++) {
            Node nodes = nl.item(i);

            if (nodes.getNodeType() == Node.ELEMENT_NODE) {

                if (nodes.getNodeName().equals("posicao")) {
                    String pos1 = ((Element) nodes).getAttribute("dimensao");
                    String pos2 = ((Element) nodes).getAttribute("tamanho");
                    //VirdLogger.timeLogger("VirdMemLoader: configuracao de memoria: dimensao -> " + pos1 + " tamanho " + pos2,1);

                    setDimensionsAttr(Integer.parseInt(pos1));
                    setSizeAttr(Integer.parseInt(pos2));
                }

                else if (nodes.getNodeName().equals("valores")) {
                	//VirdLogger.timeLogger("VirdMemLoader: obtendo tipo de dado da memoria ",1);
                    NodeList nodo = nodes.getChildNodes();
                    String   val  = nodo.item(0).getNodeValue();
                    
                   /// VirdLogger.timeLogger("VirdMemLoader: dados da memoria sao do tipo: " + val,1);

                    setTypeAttr(parseTypeAttr(val));
                }

                else if (nodes.getNodeName().equals("dados")) {
                	//VirdLogger.timeLogger("VirdMemLoader: Obtendo valores de memoria do XML ",1);
                    Node   nodo = nodes.getChildNodes().item(0);
                    vals += nodo.getNodeValue() + " ";
                    
                   // VirdLogger.timeLogger("VirdMemLoader: valores de memoria: " + vals,1);
                    

                                        	                    
                }
                else {
                	//VirdLogger.timeLogger("VirdMemLoader: Erro no arquivo de memoria: XML tag desconhecida " + nodes.getNodeName(),1);
                	
                }
            }
        }
        parseValue(vals);
        
    }
	
	public String toString() {
		String values = "\n";

		if(dimensionsAttr == 1) {
			for(int i = 0; i < sizeAttr; i++) {
				values += array1[i] + ", " ;
			}
		}
		
		else if (dimensionsAttr == 2) {
			for(int i = 0; i < sizeAttr; i++ ) {
				for(int j = 0; j < sizeAttr; j++) {
					values += ", " + array2[i][j];
				}
			}
		}
		
		else if (dimensionsAttr == 3) {
			for(int k = 0; k < sizeAttr; k++) {
				for(int i = 0; i < sizeAttr; i++ ) {
					for(int j = 0; j < sizeAttr; j++) {
						values += array3[k][i][j] + ", ";
					}
					values += "\n";
				}
				values += "\n";
			}
		}
		
		
		return "File: " + inputFile + values ;
	}
	
	
	
	
	
	
	

}
