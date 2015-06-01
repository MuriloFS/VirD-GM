package g3pd.virdgm.core;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//~--- JDK imports ------------------------------------------------------------

import g3pd.virdgm.misc.Semaphore;
import g3pd.virdgm.misc.VPEConnection;
import g3pd.virdgm.misc.XmlExport;

import org.jscience.mathematics.number.Complex;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Principal funcionalidade, o gerenciamento do disparo da GM-app, da sua execução e processamento.
 * 
 * */
public class VirdLauncher {
	Vector<VirdProcElem>					procLocked		= new Vector<VirdProcElem>();
	Vector<VirdProcElem>					procReady		= new Vector<VirdProcElem>(); 
	Vector<String>							freeNodes 		= new Vector<String>();
	int[][]              					adjMatrix; 
	Integer 								numproc;
	private final Object					lock			= new Object();
	VirdMemLoader							virdMemLoader;
	VirdProcLoader							virdProcLoader;
	Vector<Node>							envNodes; 
	Vector<Node> 							procelemNodes;
	public static Integer					executionID; 
	public static Integer   				priority		= 0; //Modo Depuracao com ou sem logger
	ConcurrentHashMap<Integer, String>		memoryType;
	ConcurrentHashMap<Integer, Object>		memoryData;
	public static VirdMemory 				virdMemory;
	private boolean 						memOK			= false;
	public java.util.concurrent.Semaphore	memStop			= new java.util.concurrent.Semaphore(1);

	/**
	 * Construtor responsavel por disparar o componente VirdLoader, que obtem as informacoes dos arquivos
	 * descritores da memoria e processos, e identifica a lista de nodos disponiveis
	 * 
	 * @param memInputFile  	Nome do arquivo descritor da memoria
	 * @param procInputFile 	Nome do arquivo descritor de processos
	 * @param nodesToExec 		Identificacao dos nodos para execucao
	 * @exception IOException
	 * */
	public VirdLauncher (){}

	/**
	 * Inicializa conexão com o VPE-qGM
	 * 
	 * */
	public VirdLauncher (String port, String clients){
		//System.out.println("Criando lista de clientes");
		String myHosts = clients;
		String[] hostNames = myHosts.split(",");

		for (int i = 0; i < hostNames.length; ++i) {
			Integer port_number = Integer.valueOf(3000);
			String hostname_string = hostNames[i];

			String[] host_number = hostname_string.split(":");

			Integer number = Integer.valueOf(Integer.parseInt(host_number[1]));

			for (int j = 0; j < number.intValue(); ++j) {
				this.freeNodes.add(host_number[0] + ":" + port_number);
				port_number = Integer.valueOf(port_number.intValue() + 1);
			}
		}
		int ports = Integer.parseInt(port);
		try {
			VPEConnection vpeCon = new VPEConnection(this,ports);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public VirdLauncher(String memInputFile, String procInputFile, String nodesToExec)
	throws IOException, InterruptedException
	{
		Random my_rand = new Random();
		long tempoInicial = System.currentTimeMillis();
		executionID = Integer.valueOf(my_rand.nextInt());
		File f = new File(System.getProperty("user.dir"));
		
	    
		this.virdMemLoader = new VirdMemLoader(memInputFile, "QGMAnalyzer", true);
		
		
		this.memoryType = this.virdMemLoader.getTypeAttr();
		this.memoryData = this.virdMemLoader.getDateAttr();
		virdMemory = new VirdMemory(this.memoryType, this.memoryData);
		virdMemory.GPU = this.virdMemLoader.GPU;
		
		//this.virdMemLoader = new VirdMemLoader(memInputFile);
		this.virdProcLoader = new VirdProcLoader(procInputFile);
		this.procLocked = this.virdProcLoader.getVirdProcElem();
		this.adjMatrix = this.virdProcLoader.getAdjMatrix();
		this.envNodes = this.virdProcLoader.getEnvNodes();
		this.procelemNodes = this.virdProcLoader.getProcelemNodes();
		this.numproc = this.procLocked.size();

		for (int i = 0; i < this.procLocked.size(); ++i) {
			VirdProcElem virdProcElem = (VirdProcElem)this.procLocked.get(i);
			if (ckeckReadyProc(virdProcElem.getProcID())) {
				this.procReady.add((VirdProcElem)this.procLocked.remove(i));
				--i;
			}
		}
		
		String myHosts = nodesToExec;
		String[] hostNames = myHosts.split(",");

		for (int i = 0; i < hostNames.length; i++) {
			Integer port_number = Integer.valueOf(3000);
			String hostname_string = hostNames[i];

			String[] host_number = hostname_string.split(":");

			Integer number = Integer.valueOf(Integer.parseInt(host_number[1]));

			for (int j = 0; j < number.intValue(); j++) {
				this.freeNodes.add(host_number[0] + ":" + port_number);
				port_number = Integer.valueOf(port_number.intValue() + 1);
			}
		}

		Thread vird = new Thread()
		{
			public void run() {
				try {
					scheduler();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		vird.start();
		vird.join();
		long tempoFinal = System.currentTimeMillis();
		System.out.println("Tempo total de execucao 1: " + (tempoFinal - tempoInicial) / 1000.0D);
		PrintWriter results = new PrintWriter(new BufferedWriter(new FileWriter("ProcessesExecution.txt",true)), true);    	
    	results.write(String.valueOf((tempoFinal - tempoInicial) / 1000.0D)+"\n");
    	results.close();
	}
	
	

	public VirdMemory inicLauncher(String procInputFile){
		long tempoInicial = System.currentTimeMillis();
		try
		{
			this.virdProcLoader = new VirdProcLoader(procInputFile);
			this.procLocked = this.virdProcLoader.getVirdProcElem();
			this.adjMatrix = this.virdProcLoader.getAdjMatrix();
			this.envNodes = this.virdProcLoader.getEnvNodes();
			this.procelemNodes = this.virdProcLoader.getProcelemNodes();
			this.numproc = this.procLocked.size();

			for (int i = 0; i < this.procLocked.size(); ++i) {
				VirdProcElem virdProcElem = (VirdProcElem)this.procLocked.get(i);

				if (ckeckReadyProc(virdProcElem.getProcID())) {
					this.procReady.add((VirdProcElem)this.procLocked.remove(i));
					--i;
				}
			}
			Thread vird = new Thread()
			{
				public void run() {
					try {
						scheduler();
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			vird.start();
			vird.join();
		} catch (InterruptedException localInterruptedException) {
		} catch (IOException e) {
			e.printStackTrace();
		}
		long tempoFinal = System.currentTimeMillis();
		System.out.println("Tempo total de execucao: " + (tempoFinal - tempoInicial) / 1000.0D);
		return this.virdMemory;
	}
	
	public void inicMem() throws IOException, InterruptedException{
		memStop.acquire();
		File f = new File(System.getProperty("user.dir"));
		this.virdMemLoader = new VirdMemLoader(f.getParent()+"/tempFiles/tempMemory.xml", "QGMAnalyzer", true);
		this.memoryType = this.virdMemLoader.getTypeAttr();
		this.memoryData = this.virdMemLoader.getDateAttr();
		virdMemory = new VirdMemory(this.memoryType, this.memoryData);	
		memOK = true;
		memStop.release();
	}
	/**
	 * Metodo principal responsavel por obter os dados referentes
	 * aos nomes dos arquivos descritores e inicializar a VirD-GM
	 * @param args args[0] memoria , args[1] processos, args[2] localizacao
	 * */
	public static void main(String[] args)
	throws Exception
	{
		//Runtime r = Runtime.getRuntime();
		//Process p = r.exec("EM.jar");

		if (args.length == 1) { new VirdClient(Integer.valueOf(Integer.parseInt(args[0])));
		} else if(args.length == 2){
			//System.out.println("Launcher 2");
			final VirdLauncher virdLauncher = new VirdLauncher(args[0],args[1]);
		}else{
			System.out.println("Launcher 3");
			try
			{
				priority = Integer.valueOf(Integer.parseInt(args[3]));
			}
			catch (Exception localException){}

			long tempoInicial = System.currentTimeMillis();
			try
			{
				checkInputArgs(args);

				final VirdLauncher virdLauncher = new VirdLauncher(args[0], args[1], args[2]);
				
				//virdLauncher.printMemoryLog();
				Thread vird = new Thread()
				{
					public void run() {
						try {
							virdLauncher.scheduler();
						}
						catch (IOException e) {
							e.printStackTrace();
						}
					}
				};
				vird.start();
				vird.join();
				
				//virdLauncher.printMemoryLog();
				//XmlExport export = new XmlExport();
				//export.exportXML(virdMemory);
				
			}
			catch (InterruptedException localInterruptedException) {}
			long tempoFinal = System.currentTimeMillis();
			PrintWriter results = new PrintWriter(new BufferedWriter(new FileWriter("TotalExecution.txt",true)), true);
	    	results.write(String.valueOf((tempoFinal - tempoInicial) / 1000.0D)+"\n");
	    	results.close();
	    	
	    	virdMemory.print(1);
	    	
			System.out.println("VirD-GM: Tempo total de execucao: " + ((tempoFinal - tempoInicial) / 1000.0D));
			System.out.println("Finalizando VirD-GM...");
			
	    	
		}
	}
	/**
	 * Coponente Scheduler responsavel pelo mapeamento fisico dos processos 
	 * inclusos na lista de execução
	 * @exception IOException
	 * */
	public void scheduler()
	throws IOException
	{
		final Semaphore procDone = new Semaphore(this.procReady.size() + this.procLocked.size());
		try {
			while ((!this.procLocked.isEmpty()) || (!this.procReady.isEmpty()) || (this.numproc > 0))
			{
				if ((!this.freeNodes.isEmpty()) && (!this.procReady.isEmpty()))
				{
					String hostport1 = (String)this.freeNodes.remove(0);

					String[] hostport2 = hostport1.split(":");
					final String host = hostport2[0];
					final Integer port = Integer.valueOf(Integer.parseInt(hostport2[1]));
					final VirdProcElem virdProcElem = (VirdProcElem)this.procReady.remove(0);
					final int procID = virdProcElem.getProcID();
					final String actionAttr = virdProcElem.getActionAttr();
					final String outputPosAttr = virdProcElem.getOutputPosAttr();
					final String inputPosAttr = virdProcElem.getInputPosAttr();
					final String valueAttr = virdProcElem.getValueAttr();
					final String controlListAttr = virdProcElem.getControlListAttr();
					final Integer iterator = virdProcElem.getIterator();

					Thread schdl = new Thread()
					{
						public void run()
						{
							VirdExec virdExec = new VirdExecImpl();
							if (virdProcElem.isProjNode())
								selectProj(virdProcElem, procDone);
							else {
								virdExec.send(actionAttr, valueAttr, inputPosAttr, outputPosAttr, controlListAttr, iterator, virdMemory, host, port);
							}
							long tempoFinalProc = System.currentTimeMillis();
							synchronized (lock){
								numproc --;
								lock.notify();
							}
							verifySomDet(virdProcElem, procDone);
							releaseProcToExec(procID);
							for (int i = 0; i < VirdLauncher.this.procLocked.size(); ++i) {
								if (VirdLauncher.this.ckeckReadyProc(((VirdProcElem)procLocked.get(i)).getProcID())) {
									procReady.add((VirdProcElem)procLocked.remove(i));
									--i;
								}
							}
							
							freeNodes.add(host + ":" + port);
							procDone.v();
							
							
							synchronized (freeNodes) {
								freeNodes.notify();
							}
						}
					};
					schdl.start();
				}
				else
				{
					synchronized (this.freeNodes) {
						this.freeNodes.wait(10L);
					}
				}
				procDone.p();
			}
		}
		catch (Exception localException)
		{}
	}
	/**
	 * Remove um processo através do seu ID
	 * @param proc_id 	ID do processo a ser eliminado
	 * @param semaphore 
	 * */
	public Boolean removeProc(Integer proc_id, Semaphore semaphore)
	{
		for(int i = 0; i < this.procReady.size(); i++)
		{
			if (this.procReady.get(i).getProcID() == proc_id)
			{
				//VirdProcElem vpe = this.procLocked.remove(i);
				numproc--;
				procReady.remove(i);
				this.releaseProcToExec(proc_id);
				semaphore.v();
				//semaphore.release();
				return true;
			}
		}

		for(int i = 0; i < this.procLocked.size(); i++)
		{
			if (this.procLocked.get(i).getProcID() == proc_id)
			{
				//VirdProcElem vpe = this.procLocked.remove(i);
				numproc--;
				procLocked.remove(i);
				this.releaseProcToExec(proc_id);
				semaphore.v();
				//semaphore.release();
				return true;
			}
		}

		return false;
	}
	/**
	 * Remove uma lista de processos que se encontram em um envelope
	 * @param procs 	Lista de processos a serem cancelados
	 * @param semaphore	
	 * */
	public void removeProcs(Vector <Object>procs, Semaphore semaphore)
	{
		for(int i = 0; i < procs.size(); i++)
		{
			Boolean f = removeProc((Integer) procs.get(i), semaphore);
			//			System.out.println("Anderson ----------- processo removido: " + procs.get(i) );
		}
	}
	/**
	 * Metodo responsavel por selecionar o caminho a seguir em uma soma 
	 * deterministica. 
	 * @param vpe		processo pai da soma deterministica
	 * @param semaphore
	 * */
	public void verifySomDet(VirdProcElem vpe, Semaphore semaphore)
	{
		if(vpe.isSomdetNode()) {
			Integer pos = Integer.parseInt(vpe.getOutputPosAttr());
			Boolean result_somdet = (Boolean) virdMemory.readMemory(pos);
			Boolean res_somdet = result_somdet;

			if(res_somdet)
			{
				Node n = vpe.getSomdetNodeTrue();
				Node n_false = vpe.getSomdetNodeFalse();
				if(VirdProcLoader.getNodeAttribute(n, "repr").equals("conselem"))
				{
					this.releaseProcToExec((Integer)n.getUserData("proc_id"));
					removeProc((Integer)n_false.getUserData("proc_id"), semaphore);
				}
				else if(VirdProcLoader.getNodeAttribute(n, "repr").equals("env"))
				{
					Vector<Object> proc_to_remove = findProcelem(n_false);
					removeProcs(proc_to_remove, semaphore);
				}
			}
			else
			{
				Node n = vpe.getSomdetNodeFalse();
				Node n_true = vpe.getSomdetNodeTrue();
				if(VirdProcLoader.getNodeAttribute(n, "repr").equals("conselem"))
				{
					removeProc((Integer)n_true.getUserData("proc_id"), semaphore);
					this.releaseProcToExec((Integer)n.getUserData("proc_id"));
				}
				else if(VirdProcLoader.getNodeAttribute(n, "repr").equals("env"))
				{
					Vector<Object> proc_to_remove = findProcelem(n_true);
					removeProcs(proc_to_remove, semaphore);
				}
			}
		}
	}
	//Metodo responsavel por excluir as projecoes que nao serao utilizadas 
	/**
	 * @param virdProcElem		processo que representa o operador DetProj
	 * @param procDone			
	 */
	public void selectProj(VirdProcElem virdProcElem, Semaphore semaphore){
		String input = virdProcElem.getInputPosAttr();
		int id = DetProj(input);	
		Vector<Node> childNodes = new Vector<Node>();
		childNodes = virdProcElem.getNodeList();
		for (int i=0; i<childNodes.size();i++){
			if (id!=i){
				if(VirdProcLoader.getNodeAttribute(childNodes.get(i), "repr").equals("macro"))
				{
					Vector<Object> proc_to_remove = findProcelem(childNodes.get(i));
					removeProcs(proc_to_remove, semaphore);
				}
			}
		}
	}

	//ADRIANO - CONJUNTO DE METODOS RESPONSAVEIS PELA SELECAO DA PROJECAO A SER EXECUTADA 
	//Metodo responsavel por identificar qual projecao sera executada
	public int DetProj(String projection){
		String [] operators = projection.split(";");
		Complex [] memValues = new Complex [virdMemory.getMemorySize()-1];
		for(int i=0; i<virdMemory.getMemorySize()-1; i++){
			memValues[i] = ((Complex) virdMemory.readMemory(i));
		}

		String [] probabilities = CalcProb(memValues);
		Vector<ArrayList<String>> statesList = GetStates(operators);
		Object [] ret = GetIntervals(probabilities);
		Vector<ArrayList<Integer>> intervalsList = (Vector<ArrayList<Integer>>) ret[0];
		Integer finalIntervalValue = (Integer) ret[1];
		int idx = GetProjId(statesList,intervalsList, finalIntervalValue);
		return idx;
	}
	private int GetProjId(Vector<ArrayList<String>> statesList, Vector<ArrayList<Integer>> intervalsList, Integer finalInterval) {
		int index;
		String value;
		Integer value1;
		Integer value2;

		Random random = new Random();
		int alpha = random.nextInt(finalInterval);

		for(int proj=0; proj<statesList.size(); proj++){
			ArrayList<String> projStates = statesList.get(proj);
			for (int state=0; state<projStates.size(); state++){
				value = projStates.get(state);
				index = Integer.parseInt(value, 2);
				ArrayList<Integer> interval = intervalsList.get(index);
				value1 = interval.get(0);
				value2 = interval.get(1);
				if((value1 <= alpha)&&(alpha < value2)){
					return proj;
				}
			}
		}
		return 0;
	}
	private Object [] GetIntervals(String [] probabilities) {
		Integer beginInterval = 1; 
		Vector<ArrayList<Integer>> intervals = new Vector<ArrayList<Integer>>();
		for(int prob=0; prob<probabilities.length; prob++){
			ArrayList<Integer> limits = new ArrayList<Integer> ();
			limits.add(beginInterval);
			limits.add(Integer.parseInt(probabilities[prob].substring(2)) + beginInterval);
			intervals.add(limits);
			beginInterval = Integer.parseInt(probabilities[prob].substring(2)) + beginInterval;
		}
		Object [] obj = new Object [] {intervals,beginInterval};
		return obj;
	}
	private Vector<ArrayList<String>> GetStates(String[] operators) {
		String [] projections = new String [operators[0].split(",").length];
		Vector<ArrayList<String>> statesList = new Vector<ArrayList<String>>();
		String binary = new String("");
		int numId;
		for(int proj=0; proj<operators.length; proj++){
			ArrayList<String> states = new ArrayList<String>();
			projections = operators[proj].split(",");
			binary = "";
			numId = 0;
			for(int op=0; op<projections.length; op++){
				if(projections[op].equals("P0")){
					binary += "0";
				}else if(projections[op].equals("P1")){
					binary += "1";
				}else if(projections[op].equals("Id")){
					binary += "0";
					numId ++;
				}
			}
			Vector<String> listBin = StartBinary(numId);
			listBin.removeElementAt(0);
			states.add(binary);

			while (!listBin.isEmpty()){
				String variations = new String(listBin.get(0));
				String answer = new String(binary);
				for(int op=0; op<projections.length; op++){
					if(projections[op].equals("Id")){
						answer = answer.substring(0, op) + variations.substring(0, 1) + answer.substring(op+1);
						variations = variations.substring(1);
					}
				}
				states.add(answer);
				listBin.removeElementAt(0);
			}
			statesList.add(states);
		}
		return statesList;

	}
	//Metodo responsavel por resgatar os valores das probabilidades associadas a cada possicao de memoria
	private String [] CalcProb(Complex[] memValues) {
		String [] probs = new String [memValues.length];
		Double data;
		DecimalFormat df = new DecimalFormat("0.000");
		for(int val=0; val<memValues.length; val++){
			data = memValues[val].pow(2).getReal();
			probs[val] = df.format(data);
		}		
		return probs;		
	}
	private Vector<String> StartBinary(int qubits){
		Vector<String> binary = new Vector<String>();
		String result;
		float x;
		for(int i=0; i<(int) Math.pow(2,qubits); i++){
			int res = i%2;
			result = String.valueOf(res); 
			x = i/2;
			while(x>=1){
				res = (int) x%2;
				result = String.valueOf(res) + result;
				x = x/2;
			}
			while(result.length() < qubits){
				result = "0" + result;
			}
			binary.add(result);
		}
		return binary;
	}
	/////ADRIANO FIM
	/**
	 * Metodo responsavel por selecionar os processos a serem excluidos na soma deterministica
	 * @param nodo 		nodo que representa um envenlope o qual todos os processos filhos serao excluidos
	 */
	public Vector<Object> findProcelem(Node nodo)
	{
		Vector <Object>proc_id_list = new Vector <Object>();
		//Vector   readNodes  = new Vector();
		Node child;

		NodeList childNodes = nodo.getChildNodes();

		for (int i = 0; i < childNodes.getLength(); i++) {
			if (Node.ELEMENT_NODE == childNodes.item(i).getNodeType()) {
				child = childNodes.item(i);
				if (VirdProcLoader.getNodeAttribute(child, "repr").equals("env")){
					proc_id_list.addAll(findProcelem(child));
				}
				else {
					proc_id_list.add(child.getUserData("proc_id"));
				}
			}
		}

		return proc_id_list;
	}
	/**
	 * Libera nodo para execucao 
	 * @param procID 		ID do processo a ser liberado
	 * */
	public void releaseProcToExec(int procID) {
		//VirdLogger.timeLogger("Liberando processo com ID" + procID,1);
		for (int column = 0; column < this.adjMatrix.length; column++) {
			this.adjMatrix[procID][column] = 0;
		}
	}
	public boolean ckeckReadyProc(int procID) {
		for (int line = 0; line < this.adjMatrix.length; line++) {
			if (this.adjMatrix[line][procID] == 1) {
				return false;
			}
		}

		return true;
	}
	/**
	 * Metodo responsavel pela verificacao dos parametros recebidos
	 * Use: memfile.xml procfile.xml nodesToExec
	 * @param args		parametros de entrada 
	 */
	private static void checkInputArgs(String[] args) throws IOException {
		//VirdLogger.timeLogger("Checando parametros de inicializacao",1);
		if (args.length < 3) {
			System.exit(-1);
		}
	}
	/**
	 * Metodo responsavel por escrever a memoria no log
	 * */
	public void printMemoryLog() throws IOException {
		String message = "Estado da memoria: \n\n";
		synchronized (virdMemory.data) {
			for (int i = 0; i < this.virdMemory.getMemorySize(); i++) {
				message += "POSICAO " + i + ": " + this.virdMemory.readMemory(i)+ "\n\n";
			}
			System.out.println(message);
		}
		
	}

}

