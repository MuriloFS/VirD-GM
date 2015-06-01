package g3pd.virdgm.core;

//~--- non-JDK imports --------------------------------------------------------

import org.w3c.dom.*;

//~--- JDK imports ------------------------------------------------------------

//import g3pd.virdgm.misc.VirdLogger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import javax.xml.parsers.*;
/**Classe responsavel pela interpretacao do arquivo descritor de processos
 * e o mapeamento dos mesmos na VirD-GM*/
public class VirdProcLoader {
	int 		procID = 0;
	int[][] 	adjMatrix;
	String 		inputFile;
	VirdGraph 	virdGraph;
	VirdNode	virdNode;
	Vector 		<VirdProcElem>virdProcElem;
	Vector		<Node>envNodes;
	Vector		<Node>procelemNodes;
	/**Construtor que recebe um arquivo descritor de processo e verifica sua integridade
	 * @param inputFile 	Nome do arquivo descritor de processos
	 * @exception e 	Erro ocorrido durante o processo*/
	VirdProcLoader(String inputFile) throws IOException {
		//VirdLogger.timeLogger("Inicializando interpretador de processos",1);
		this.inputFile    = inputFile;
		this.virdProcElem = new Vector<VirdProcElem>();
		virdGraph         = new VirdGraph();
		envNodes		  = new Vector<Node>();
		procelemNodes	  = new Vector<Node>();

		try {
			//VirdLogger.timeLogger("VirdProcLoader: abrindo arquivo de processos ->  " + inputFile,1);
			openFile();
			//VirdLogger.timeLogger("VirdProcLoader: arquivo de processos lido... saindo",1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		//VirdLogger.timeLogger("VirdProcLoader: preparando-se para a criacao da matriz de adjacencias ",1);
		createAdjMatrix();
		//VirdLogger.timeLogger("VirdProcLoader: matriz de adjancencias criada ",1);

	}
	/**Metodo que retorna uma lista de processos elementares*/
	public Vector<VirdProcElem> getVirdProcElem() {
		return this.virdProcElem;
	}
	/**Metodo que retorna a matriz de adjacencia dos processos*/
	public int[][] getAdjMatrix() {
		return this.adjMatrix;
	}
	/**Metodo que retorna o grafo de dependencias gerado*/
	public VirdGraph getVirdGraph() {
		return this.virdGraph;
	}
	/**Metodo que retorna a lista de nodos com processos elementares*/
	public Vector<Node> getProcelemNodes()
	{
		return this.procelemNodes;
	}
	/**Metodo que retorna a lista de nodos com envelopes encontrados*/
	public Vector<Node> getEnvNodes()
	{
		return this.envNodes;
	}
	/**Metodo responsavel por abrir o arquivo descritor de processos
	 * e inicializar o parser interpretador*/
	public void openFile() throws Exception {
		DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
		DocumentBuilder        db       = dbf.newDocumentBuilder();
		Document               doc      = db.parse(inputFile);
		Element                elem     = doc.getDocumentElement();
		NodeList               nodelist = elem.getChildNodes();
		Node                   node     = nodelist.item(1);
		Vector <Object>                list     = new Vector<Object>();

		//VirdLogger.timeLogger("VirdProcLoader: inicializando interpretador XML ",1);
		parseFile(list, node);
		//VirdLogger.timeLogger("VirdProcLoader: finalizando interpretador XML ",1);
	}
	/**Interpretador do arquivo descritor de processos, responsavel por identificar
	 * o tipo de representacao, a acao, o valor para teste se houver,
	 * a posicao que armazenara o resultado, os parametros de entrada e o iterador
	 * dos processos e com tais informacoes inicializar os processos indicando os
	 * nodos onde cada um se encontra.
	 *
	 * @param previousProc 	Lista de processos anteriores
	 * @param node 		Nodo sendo analizado*/
	public Object parseFile(Object previousProc, Node node) throws IOException {
		//VirdLogger.timeLogger("VirdProcLoader: interpretador XML iniciado",1);
		
		if (getNodeAttribute(node, "repr").equals("conselem")) {
			return conselem(previousProc,node);
		}
		if (getNodeAttribute(node, "repr").equals("partquantum")) {
			return quantumprocess(previousProc,node);
		}
		if (getNodeAttribute(node, "repr").equals("probability")) {
			return probability(previousProc,node);
		}
		//Componente projeção utilizado pela qGM
		if (getNodeAttribute(node, "repr").equals("projection")) {
			return projection(previousProc,node);
		}	
		if (getNodeAttribute(node, "repr").equals("env")) {
			return envelope(previousProc,node);
		}
		if (getNodeAttribute(node, "repr").equals("somdet")) {
			return sumDet(previousProc,node);
		}
		//IMPLEMENTACAO DO OPERADOR DETPROJ - INTEGRANTE DO CONSTRUTOR DE PROJECAO
		//possui como parametro o tamanho da memoria que sera utilizado para varrer a memoria e realizar os calculos 
		//ANDERSON 26/02/10
		//TENTANDO IMPLEMENTAR O OPERADOR DETPROJ 
		//REsponsavel por determinar qual projecao sera executada baseado em calculos probabilisticos
		if (getNodeAttribute(node, "repr").equals("detproj")) {
			return detProj(previousProc,node);
		}
		if (getNodeAttribute(node, "repr").equals("iterativo")) {
			return iterativo(previousProc,node);
		}
		//        construindo macros 
		if (getNodeAttribute(node, "repr").equals("macro")) {
			return macro(previousProc,node);			
		}
		return -1;
	}
	
	private Object macro(Object previousProc, Node node) throws IOException {
		// TODO Auto-generated method stub
		if (getNodeAttribute(node, "tipo").equals("classic")){
			String  arqAttr  = getNodeAttribute(node, "arq");
			System.out.println("ARQUIVO: " + arqAttr);
			Vector <Object> procList = new Vector<Object>();
			try {
				DocumentBuilderFactory dbf      = DocumentBuilderFactory.newInstance();
				DocumentBuilder        db       = dbf.newDocumentBuilder();
				Document               doc      = db.parse(arqAttr);
				Element                elem     = doc.getDocumentElement();
				NodeList               nodelist = elem.getChildNodes();
				Node                   nodo     = nodelist.item(1);

				//VirdLogger.timeLogger("VirdProcLoader: inicializando interpretador XML ",1);
				procList.add(parseFile(previousProc, nodo));
				//VirdLogger.timeLogger("VirdProcLoader: finalizando interpretador XML ",1);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return procList;
		}	
		
		if (getNodeAttribute(node, "tipo").equals("quantum")){
			//VirdLogger.timeLogger("VirdProcLoader: Macro quantica encontrada:",1);
			Vector   <Node>readNodes = new Vector<Node>();
			NodeList filhos    = node.getChildNodes();


			for (int i = 0; i < filhos.getLength(); i++) {
				if (Node.ELEMENT_NODE == filhos.item(i).getNodeType()) {
					readNodes.add(filhos.item(i));
				}
			}

			Vector<Object> procList = new Vector<Object>();

			for (int i = 0; i < readNodes.size(); i++) {
				procList.add(parseFile(previousProc, (Node) readNodes.get(i)));
			}

			return procList;
		}
		return -1;
	}
	private Object iterativo(Object previousProc, Node node) throws IOException {
		// TODO Auto-generated method stub
		//VirdLogger.timeLogger("VirdProcLoader: iterativo encontrado... preparando-se para processar",1);
		if (getNodeAttribute(node, "tipo").equals("iterativo")) {
			//VirdLogger.timeLogger("VirdProcLoader: processo iterativo encontrado",1);
			Vector   <Node>readNodes  = new Vector<Node>();
			NodeList childNodes = node.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				if (Node.ELEMENT_NODE == childNodes.item(i).getNodeType()) {
					readNodes.add(childNodes.item(i));
				}
			}
			//VirdLogger.timeLogger("VirdProcLoader: " + readNodes.size() + " processos para serem iterados",1);
			String range = getNodeAttribute( node, "range");
			//VirdLogger.timeLogger("VirdProcLoader: range de iteracao " + range,1);
			String [] range_attrs = range.split(":");

			int start_iterator = Integer.parseInt(range_attrs[0]);
			int jump_iterator = Integer.parseInt(range_attrs[2]);
			int end_iterator = Integer.parseInt(range_attrs[1]);

			if (getNodeAttribute(node, "acao").equals("parfor")) {
				Vector <Object>procList = new Vector<Object>();
				for (int i = start_iterator; i < end_iterator; i+=jump_iterator) {
					//VirdLogger.timeLogger("VirdProcLoader: [parfor] replicando processo " + i,1);
					((Node) readNodes.get(0)).setUserData("iterator", i, null);
					procList.add(parseFile(previousProc, (Node) readNodes.get(0)));					
				}
				return procList;
			}
			else if (getNodeAttribute(node, "acao").equals("seqfor")) {
				for (int i = start_iterator; i < end_iterator; i+=jump_iterator) {
					//VirdLogger.timeLogger("VirdProcLoader: [seqfor] replicando processo " + i,1);
					((Node) readNodes.get(0)).setUserData("iterator", i, null);
					previousProc = parseFile(previousProc, (Node) readNodes.get(0));
				}

				return previousProc;
			}
		}
		return -1;
	}
	private Object detProj(Object previousProc, Node node) throws IOException {
		// TODO Auto-generated method stub
		//VirdLogger.timeLogger("VirdProcLoader: operador detproj encontrado... preparando-se para processar",1);
		
		String       inputPosAttr  = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		
		VirdProcElem virdProcElem  = new VirdProcElem("", "", "", inputPosAttr, controlListAttr, procID, 0);
		virdProcElem.setProjNode(true);

		//VirdLogger.timeLogger("VirdProcLoader: operador detproj criado: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader: ",1);

		if (previousProc instanceof Integer) {
			Vector <Object>previous = new Vector<Object>();

			previous.add(previousProc);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
			this.virdNode = new VirdNode(procID, previous, node);


		} else if (previousProc instanceof Vector) {

			Vector <Object>previous = new Vector<Object>();
			Vector <Object>aux      = new Vector<Object>();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector <Object>aux2 = new Vector<Object>(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previousProc,2);
			this.virdNode = new VirdNode(procID, previous, node);
		} else {
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previousProc,2);
			this.virdNode = new VirdNode(procID, previousProc, node);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		////VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		
		procID++;
		
		int id = procID - 1;
		int i = 1;
		String idt = ""+i;
		//analisa os processos que compoem as macros que representam as projecoes
		while((Node)node.getUserData(idt) != null){
			Node node_l = (Node)node.getUserData(idt);
			parseFile(id, node_l);
			virdProcElem.setNodeList(node_l);
//			System.out.println("Node List      :"+virdProcElem.getNodeList());
			i++;
			idt = ""+i;
		}
	
		return procID - 1;
	}
	private Object sumDet(Object previousProc, Node node) throws IOException {
		// TODO Auto-generated method stub
		//VirdLogger.timeLogger("VirdProcLoader: processo soma deterministica encontrado... preparando-se para processar",1);
		String       actionAttr    = getNodeAttribute(node, "acao");
		String valueAttr = getNodeAttribute(node, "valor");
		String       outputPosAttr = getNodeAttribute(node, "pos");
		String       inputPosAttr  = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		Integer iterator = (Integer) node.getUserData("iterator");

		VirdProcElem virdProcElem  = new VirdProcElem(actionAttr, valueAttr, outputPosAttr, inputPosAttr, controlListAttr, procID, iterator);
		virdProcElem.setSomdetNode(true);

		//VirdLogger.timeLogger("VirdProcLoader: processo elementar criado: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader: ",1);

		if (previousProc instanceof Integer) {
			Vector previous = new Vector();

			previous.add(previousProc);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
			this.virdNode = new VirdNode(procID, previous, node);


		} else if (previousProc instanceof Vector) {

			Vector previous = new Vector();
			Vector aux      = new Vector();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector aux2 = new Vector(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previousProc,2);
			this.virdNode = new VirdNode(procID, previous, node);
		} else {
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previousProc,2);
			this.virdNode = new VirdNode(procID, previousProc, node);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		////VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		procID++;

		Node node_true = (Node) node.getUserData("true_part");
		Node node_false = (Node) node.getUserData("false_part");


		Vector procList = new Vector();

		procList.add(parseFile(procID - 1, node_true));
		procList.add(parseFile(procID - 2, node_false));

		virdProcElem.setSomdetNodeTrue(node_true);
		////VirdLogger.timeLogger("NODO-TRUE: " + node_true);
		virdProcElem.setSomdetNodeFalse(node_false);
		////VirdLogger.timeLogger("NODO-FALSO: " + node_false);
		return procID - 1;
	}
	private Object envelope(Object previousProc, Node node) throws IOException {
		this.envNodes.add(node);
		//VirdLogger.timeLogger("VirdProcLoader: envelope encontrado... preparando-se para processar",1);
		if (getNodeAttribute(node, "tipo").equals("seq")) {
			//VirdLogger.timeLogger("VirdProcLoader: envelope sequencial encontrado",1);
			Vector   <Node>readNodes  = new Vector<Node>();
			NodeList childNodes = node.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {
				if (Node.ELEMENT_NODE == childNodes.item(i).getNodeType()) {
					readNodes.add(childNodes.item(i));
				}
			}

			for (int i = 0; i < readNodes.size(); i++) {
				if ((getNodeAttribute((Node) readNodes.get(i), "repr").equals("terminicio") != true)
						&& (getNodeAttribute((Node) readNodes.get(i), "repr").equals("termfim") != true)) {
					previousProc = parseFile(previousProc, (Node) readNodes.get(i));
//					System.out.println("Previous Proc:  "+previousProc);
				}
			}

			return previousProc;
		}

		if (getNodeAttribute(node, "tipo").equals("paralelo")) {
			//VirdLogger.timeLogger("VirdProcLoader: envelope paralelo encontrado",1);
			Vector   <Node>readNodes = new Vector<Node>();
			NodeList filhos    = node.getChildNodes();


			for (int i = 0; i < filhos.getLength(); i++) {
				if (Node.ELEMENT_NODE == filhos.item(i).getNodeType()) {

					readNodes.add(filhos.item(i));
				}
			}

			Vector <Object>procList = new Vector<Object>();

			for (int i = 0; i < readNodes.size(); i++) {
				Object id = (parseFile(previousProc, (Node) readNodes.get(i)));
				if (id instanceof Integer) {
					procList.add(id);
				}else if(id instanceof Vector){
					for(int j=0; j<((Vector)id).size(); j++){
						procList.add(((Vector)id).get(j));
					}
				}
			}
			return procList;
		}

		if (getNodeAttribute(node, "tipo").equals("nondet")) {
			//VirdLogger.timeLogger("VirdProcLoader: envelope nao-deterministico encontrado",1);
			Vector   <Node>readNodes = new Vector<Node>();
			NodeList filhos    = node.getChildNodes();

			for (int i = 0; i < filhos.getLength(); i++) {
				if (Node.ELEMENT_NODE == filhos.item(i).getNodeType()) {
					readNodes.add(filhos.item(i));
				}
			}
			Vector <Object>procList = new Vector<Object>();
			
			int ndValue = getNonDeterministicValue("javaSimpleRandom", readNodes.size());
							
			return procList.add(parseFile(previousProc, (Node) readNodes.get(ndValue)));
			
		}
		//
		if (getNodeAttribute(node, "tipo").equals("somdet")) {
			//VirdLogger.timeLogger("VirdProcLoader: envelope soma-deterministica (aka teste deterministico ) encontrado",1);
			Vector <Node>readNodes = new Vector<Node>();
			NodeList filhos    = node.getChildNodes();

			//VirdLogger.timeLogger("VirdProcLoader: tamanho envelope = "  + filhos.getLength(),1);

			for (int i = 0; i < filhos.getLength(); i++){
				if (Node.ELEMENT_NODE == filhos.item(i).getNodeType()){
					readNodes.add(filhos.item(i));
				}
			}


			Vector <Object>procList = new Vector<Object>();

			((Node)readNodes.get(0)).setUserData("true_part", readNodes.get(1), null);
			((Node)readNodes.get(0)).setUserData("false_part", readNodes.get(2), null);
			return procList.add(parseFile(previousProc, (Node) readNodes.get(0)));
		}
		
		//REPRESENTACAO DA SOMA NAO DETERMINISTICA P/ O SIMULADOR VPE-qGM
		//na extrutura interna ele possui:
		//	operador detproj - responsavel por realizar os calculos probabilisticos 
		//	envenlopes q representam as projecoes - (macros)
		//ANDERSON 26/02/10
		//nondet QUANTICO
		if (getNodeAttribute(node, "tipo").equals("nondet1")) {
			//VirdLogger.timeLogger("VirdProcLoader: envelope soma-nao-deterministica encontrado",1);
			Vector <Node>readNodes = new Vector<Node>();
			NodeList filhos    = node.getChildNodes();

			//VirdLogger.timeLogger("VirdProcLoader: tamanho envelope = "  + filhos.getLength(),1);
			//seleciona todos os elementos internos
			for (int i = 0; i < filhos.getLength(); i++){
				if (Node.ELEMENT_NODE == filhos.item(i).getNodeType()){
//					System.out.println("FILHO    :"+filhos.item(i).toString());
					readNodes.add(filhos.item(i));
				}
			}
			Vector <Object>procList = new Vector<Object>();
			//indica para o operador detproj os processos que representam as projecoes 
			//o userData do nodo indica a projecao
//			System.out.println("READ NODES:     "+readNodes);
			for (int i=1; i<readNodes.size();i++){
					String s = String.valueOf(i);
					((Node)readNodes.get(0)).setUserData(s, readNodes.get(i), null);
//					System.out.println("NODO SET USERDATA    :"+readNodes.get(i).getUserData(s));
			}
			//retorna o operador detproj que será executado no VirdLauncher em tempo de execucao
			procList.add(parseFile(previousProc, (Node) readNodes.get(0)));
//			System.out.println("ProcList:  "+procList);
			return procList; 
		}
		return -1;
	}
	private Object projection(Object previousProc, Node node) {
		// TODO Auto-generated method stub
		this.procelemNodes.add(node);

		//VirdLogger.timeLogger("VirdProcLoader: projeção encontrada... preparando-se para processar",1);

		String       actionAttr    = getNodeAttribute(node, "acao");			
		String       outputPosAttr = getNodeAttribute(node, "pos");
		String       inputPosAttr  = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		Integer iterator = (Integer) node.getUserData("iterator");
		
		VirdProcElem virdProcElem  = new VirdProcElem(actionAttr, "", outputPosAttr, inputPosAttr, controlListAttr, procID, iterator);

		//VirdLogger.timeLogger("VirdProcLoader: projeção criada: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader:  ",1);

		if (previousProc instanceof Integer) {
			Vector<Object> previous = new Vector<Object>();

			previous.add(previousProc);
			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE?? " + previous,2);


		} else if (previousProc instanceof Vector) {

			Vector <Object>previous = new Vector <Object>();
			Vector <Object>aux      = new Vector <Object>();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector  <Object>aux2 = new Vector(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}

			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
		} else {

			this.virdNode = new VirdNode(procID, previousProc, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????????????? " + previousProc,2);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		node.setUserData("proc_id", procID, null);
		procID++;

		return procID - 1;
	}
	private Object probability(Object previousProc, Node node) {
		// TODO Auto-generated method stub
		this.procelemNodes.add(node);

		//VirdLogger.timeLogger("VirdProcLoader: probabilidade encontrado... preparando-se para processar",1);

		String       actionAttr    = getNodeAttribute(node, "acao");
		//String 		 valueAttr = getNodeAttribute(node, "value");
		String       outputPosAttr = getNodeAttribute(node, "pos");
		String       inputPosAttr  = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		//Integer iterator = (Integer) node.getUserData("iterator");
		VirdProcElem virdProcElem  = new VirdProcElem(actionAttr, "", outputPosAttr, inputPosAttr, controlListAttr, procID, 0);

		//VirdLogger.timeLogger("VirdProcLoader: probabilidade criado: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader:  ",1);

		if (previousProc instanceof Integer) {
			Vector<Object> previous = new Vector<Object>();

			previous.add(previousProc);
			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE?? " + previous,2);


		} else if (previousProc instanceof Vector) {

			Vector <Object>previous = new Vector <Object>();
			Vector <Object>aux      = new Vector <Object>();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector  <Object>aux2 = new Vector(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}

			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
		} else {

			this.virdNode = new VirdNode(procID, previousProc, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????????????? " + previousProc,2);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		node.setUserData("proc_id", procID, null);
		procID++;

		return procID - 1;
	}
	
	private Object conselem(Object previousProc, Node node){
		this.procelemNodes.add(node);
		//VirdLogger.timeLogger("VirdProcLoader: processo elementar encontrado... preparando-se para processar",1);

		String       actionAttr    = getNodeAttribute(node, "acao");
		String 		 valueAttr = getNodeAttribute(node, "value");
		String       outputPosAttr = getNodeAttribute(node, "pos");
		String       inputPosAttr  = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		Integer iterator = (Integer) node.getUserData("iterator");

		VirdProcElem virdProcElem  = new VirdProcElem(actionAttr, valueAttr, outputPosAttr, inputPosAttr, controlListAttr, procID, iterator);

		//VirdLogger.timeLogger("VirdProcLoader: processo elementar criado: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader:  ",1);
		

		if (previousProc instanceof Integer) {
			Vector<Object> previous = new Vector<Object>();

			previous.add(previousProc);
			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE?? " + previous,2);


		} else if (previousProc instanceof Vector) {

//			System.out.println("Previous Proc:  "+previousProc);
			Vector <Object>previous = new Vector <Object>();
			Vector <Object>aux      = new Vector <Object>();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector  <Object>aux2 = new Vector(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}

			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
		} else {

			this.virdNode = new VirdNode(procID, previousProc, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????????????? " + previousProc,2);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		node.setUserData("proc_id", procID, null);
		procID++;

		return procID - 1;
	}
	
	private Object quantumprocess(Object previousProc, Node node){
		this.procelemNodes.add(node);
		//VirdLogger.timeLogger("VirdProcLoader: processo elementar encontrado... preparando-se para processar",1);

		String       actionAttr      = getNodeAttribute(node, "acao");
		String 		 valueAttr       = getNodeAttribute(node, "funcao");
		String       inputPosAttr    = getNodeAttribute(node, "pos");
		String       outputPosAttr   = getNodeAttribute(node, "parametro");
		String       controlListAttr = getNodeAttribute(node, "controlList");
		

		VirdProcElem virdProcElem  = new VirdProcElem(actionAttr, valueAttr, outputPosAttr, inputPosAttr, controlListAttr, procID, -1);

		//VirdLogger.timeLogger("VirdProcLoader: processo elementar criado: " + virdProcElem,1);

		this.virdProcElem.add(virdProcElem);

		//VirdLogger.timeLogger("VirdProcLoader:  ",1);
		

		if (previousProc instanceof Integer) {
			Vector<Object> previous = new Vector<Object>();

			previous.add(previousProc);
			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE?? " + previous,2);


		} else if (previousProc instanceof Vector) {

//			System.out.println("Previous Proc:  "+previousProc);
			Vector <Object>previous = new Vector <Object>();
			Vector <Object>aux      = new Vector <Object>();    // Check

			for (int i = 0; i < ((Vector) previousProc).size(); i++) {
				if(((Vector) previousProc).get(i) instanceof Integer) { // Check
					Vector  <Object>aux2 = new Vector(); // Check

					aux2.add(((Vector) previousProc).get(i)); // Check
					((Vector) previousProc).set(i, aux2); // Check
				}

				aux = (Vector) ((Vector) previousProc).get(i);

				for (int j = 0; j < aux.size(); j++) {
					previous.add(aux.get(j));
				}
			}

			this.virdNode = new VirdNode(procID, previous, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????? " + previous,2);
		} else {

			this.virdNode = new VirdNode(procID, previousProc, node);
			//VirdLogger.timeLogger("VirdProcLoader: TRUE????????????? " + previousProc,2);
		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo " + virdGraph,1);
		this.virdGraph.addNode(virdNode);
		node.setUserData("proc_id", procID, null);
		procID++;

		return procID - 1;
	}

	
	/**Metodo responsavel por determinar um valor para soma nao deterministica
	 * @param randAlg	*/
	private int getNonDeterministicValue(String randAlg, int maxValue) throws IOException {
		if(randAlg.equals("javaSimpleRandom") ) {
			Random random = new Random();
			int randomIndex = random.nextInt(maxValue);

			return randomIndex;
		}

		else if(randAlg.equals("javaSecureRandom") ) {
			SecureRandom random = new SecureRandom();
			int randomIndex = random.nextInt(maxValue);

			return randomIndex;
		}


		return -1;

	}

	public int[][] createAdjMatrix() throws IOException {
		int     idNode;
		Integer val;
		Vector <Integer> adj;
		//VirdLogger.timeLogger("VirdGraph: 1 :" + this.virdGraph,1);
		adjMatrix = new int[this.virdGraph.size()][this.virdGraph.size()];

		for (int i = 0; i < this.virdGraph.size(); i++) {
			for (int j = 0; j < this.virdGraph.size(); j++) {
				adjMatrix[i][j] = 0;
			}
		}

		for (int x = 0; x < this.virdGraph.size(); x++) {
			idNode = this.virdGraph.getNode(x).getId();
			adj    = ((Vector) (this.virdGraph.getNode(x)).getAdj());

			for (int i = 0; i < adj.size(); i++) {
				val = (Integer) adj.get(i);
				adjMatrix[val][idNode] = 1;
			}
		}

		for (int i = 0; i < this.virdGraph.size(); i++) {
			for (int j = 0; j < this.virdGraph.size(); j++) {
			}

		}

		//VirdLogger.timeLogger("VirdProcLoader: grafo final -> " + virdGraph,1);

		return adjMatrix;
	}

	//retorna uma lista com os processos a serem executados em uma macro
	public Vector<Vector<String>> getProcess(String input){
		
		input = input.substring(0,0)+input.substring(0+1);
		input = input.substring(0,(input.length()-1))+input.substring((input.length()-1)+1);
		
		String [] attrs      = input.split(";");

        for (int i = 0; i < attrs.length; i++) {
            String n = attrs[i];
            n = n.replace("[","");
    		n = n.replace("]","");
    		n = n.replace("'","");
    		attrs[i] = n;
        }     
        
        Vector<Vector<String>> process = new Vector<Vector<String>>();
        
        for (int i = 0; i < attrs.length; i++) {
        	String [] n = attrs[i].split(",");
//        	aux.add(n);
        	Vector <String> aux1 = new Vector <String>();
        	for (int j =0; j<n.length;j++){
//        		System.out.println(n[j]);
        		aux1.add(n[j]);
        	}
        	process.add(aux1);
        }    
        
		return process;
	}
	
	public static String getNodeAttribute(Node nodo, String nome) {
		for (int i = 0; i < nodo.getAttributes().getLength(); i++) {
			if (nodo.getAttributes().item(i).getNodeName().equals(nome)) {
				return nodo.getAttributes().item(i).getNodeValue();
			}
		}

		return "";
	}
}