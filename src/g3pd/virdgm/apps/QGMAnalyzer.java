package g3pd.virdgm.apps;

import org.jscience.mathematics.number.Complex;

import static jcuda.driver.JCudaDriver.*;
import static jcuda.runtime.JCuda.*;

import java.io.*;

import jcuda.*;
import jcuda.driver.*;


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import g3pd.virdgm.core.VirdApp;
import g3pd.virdgm.core.VirdMemory;

public class QGMAnalyzer implements VirdApp{
	public int qubits;
	public int num_reads;
	public String partial_read;
	public int num_writes;
	public int partial_write;
	public String shift_pos;
	public int sf_pos;
	public String shift_par;
	public int sf_par;
	
	public int num_qpps;
	public int qpp_atual;
	public int totalElements;
	public int iterations;
	public int soma_dim;
	public int dimGrid, dimBlock;
	public int outerMatrices;
	public Vector <Integer> len;
	public Vector <Integer> values;
	public Vector <Integer> positions;
	public Vector <Integer> widths;
	public Vector <Integer> elementsList;
	public Vector <Integer> Zero;
	public Vector <Integer> One;
	public boolean GPU = false;
	public boolean firstTime = true;
	
	public float matrixList[];
	public float lastMatrix[];
	public int lineZero[];
	public int lineOne[];
	public int initMatrix[];
	public int initPos[];
	public byte widthLine[];
	public byte widthList[];
	public int dimensions[];
	public int dimensions_sum[];
	public byte exps[];
	public int elementsNumber[];
	public int columns[];
	public int tamanho;
	public int vp;
	
	
	
	@SuppressWarnings("unchecked")
	public void app(String valueAttr, String input, String outputPosAttr, Integer iterator, VirdMemory memory, ObjectOutputStream oos) throws IOException {
			
		qubits = 0;
			
		if (iterator == -1){//Processo Quântico
			GPU = memory.GPU;
			num_qpps = 1;
			
			System.out.println("Processo Quantico\n");
			String[][] funcao = this.parserInput(valueAttr);
			String[][] pos = this.parserInput(input);
			String[][] par = this.parserInput(outputPosAttr);
			
			Vector <Page> Pages;
			Pages = new Vector <Page>();
			Vector <Integer> sizesList = new Vector <Integer>();
			
			positions = new Vector<Integer>();
			len = new Vector<Integer>();
			
			if (GPU){
				widths = new Vector<Integer>();
				elementsList = new Vector<Integer>();
			}
			
			iterations = 1;
			totalElements = 0;
			soma_dim = 0;
			
			num_reads = 1;
			num_writes = 1;
			shift_pos = "";
			shift_par = "";
			
			System.out.println("START");
			
			int opIndex = 0;
			
			for (int pageID = 0; pageID < pos.length; pageID++){
				Pages.add(this.PageBuilder(funcao[pageID], pos[pageID], par[pageID]));
				opIndex += funcao[pageID].length;
				sizesList.add((int)Math.pow(2, qubits-opIndex));
			}
			
			SetZeroOne(funcao);
			
			System.out.println("Num Reads: " + num_reads + "\nNum Writes: " + num_writes);
			
			if (!GPU){
				Set <Integer> newValues = new HashSet<Integer>();
				ConcurrentHashMap <Integer, Complex> mem_out = new ConcurrentHashMap <Integer, Complex> ();
				this.eraseMemory(mem_out, qubits);

				this.ApplyValuesForQP(Pages, Pages.size(), sizesList, memory, mem_out, newValues, Complex.ONE, 0, 0, 0);
				
				mem_out = adjustMemory(mem_out, newValues);
				memory.updateMemory(mem_out);
				
				System.out.println("Enviou Resultado");
			}
			else{
				System.out.println("JCuda");
				tamanho = (int)Math.pow(2, qubits);
				
				sf_par = Integer.parseInt(shift_par,2);
				sf_pos = Integer.parseInt(shift_pos,2);
				
				//SetZeroOne(funcao);
				CreateGpuArrays(Pages);
				PrintGpuArrays();
				
			    Dimensions();
			    
		        ConcurrentHashMap<Integer, float[]> m = (ConcurrentHashMap<Integer, float[]>) memory.readMemory(0);
		        float men_in[] = m.get(0);
		        int valid_pos[] = new int[tamanho/num_reads + 1];
		        
		        System.out.println("\nMEN_IN " + men_in.length);
		        
		        for (int i = 0; i < 10; i++){
		        	System.out.print(men_in[i] + " " + men_in[i+1] + ", ");
		        	i++;
		        }
		        
		        int b = tamanho + 1;
		        valid_pos[tamanho/num_reads] = b;
		        System.out.println(tamanho/num_reads + " " + b);
		        
		        for (int i = men_in.length - 2; i >= 0; i--){
		        	if ((men_in[i] != 0) || (men_in[i+1] != 0)){
		        		b = i/2;
		        	}
		        	valid_pos[i/2] = b;
		        	i--;
		        }
		        
		        cuInit(0);
			    
			    String ptxFileName = CreatePtxFile();
			    
			    cuInit(0);
		        CUdevice device = new CUdevice();
		        
		        cuDeviceGet(device, 0);
		        CUcontext context = new CUcontext();
		        cuCtxCreate(context, 0, device);
		        
		        cudaDeviceSetCacheConfig(2);

		        CUmodule module = new CUmodule();
		        cuModuleLoad(module, ptxFileName);
		        

		        CUfunction function = new CUfunction();
		        cuModuleGetFunction(function, module, "ApplyValues");
		        
		        
		        ConstantDataGPU(module);
		        int aloc;
		        CUdeviceptr deviceInputV = new CUdeviceptr();
		        aloc = cuMemAlloc(deviceInputV, tamanho * Sizeof.FLOAT * 2 / num_reads);
		        cuMemcpyHtoD(deviceInputV, Pointer.to(men_in), tamanho * Sizeof.FLOAT * 2 / num_reads);
		        
		        System.out.println("\nALOC_1: " + aloc);
		        
		        CUdeviceptr deviceInputP = new CUdeviceptr();
		        aloc = cuMemAlloc(deviceInputP, (tamanho+1) * Sizeof.INT);
		        cuMemcpyHtoD(deviceInputP, Pointer.to(valid_pos), (tamanho/num_reads + 1) * Sizeof.INT);
		        System.out.println("\nALOC_2: " + aloc);
		        
		        CUdeviceptr deviceOutput = new CUdeviceptr();
		        aloc = cuMemAlloc(deviceOutput, tamanho * Sizeof.FLOAT * 2 / num_writes);
		        
		        System.out.println("\nALOC_3: " + aloc);
		        
		        Pointer kernelParameters = Pointer.to(
		        		Pointer.to(deviceInputV),
		        		Pointer.to(deviceInputP),
		        		Pointer.to(deviceOutput)
				);
		        
		        System.out.println("\nGrid: " + dimGrid + "   BLock: "+ dimBlock);
		        
		        
		        //dimBlock= dimGrid = 1;
		        
		        int teste = cuLaunchKernel(function,
		                dimGrid,  1, 1,      // Grid dimension
		                dimBlock, 1, 1,      // Block dimension
		                0, null,               // Shared memory size and stream
		                kernelParameters, null // Kernel- and extra parameters
		        );
		        int teste2 = cuCtxSynchronize();
		        System.out.println("\nVERIFICANDO KERNEL1: " + teste + " " + teste2);
		        
		    	
		    	float hostOutput[] = new float[tamanho*2 / num_writes];
		    	
		    	teste = cuMemcpyDtoH(Pointer.to(hostOutput), deviceOutput, tamanho * Sizeof.FLOAT * 2 / num_writes);
		    	
		    	System.out.println("\nVERIFICANDO DADOS1: " + teste);
		    	
		    	
		    	System.out.println("MEMORIA FINAL");
		    	
		    	for (int i = 0; i < 128; i++){
		    		System.out.println("pos: " + i/2 + "  " + hostOutput[i] + " " + hostOutput[i+1]);
		    		i++;
		    	}
		    	
		    	cuMemFree(deviceInputV);
		    	cuMemFree(deviceInputP);
		        cuMemFree(deviceOutput);
		        
		        
		        new File("kernel" + sf_par + "_" + sf_pos + ".cu").delete();
		        new File("kernel" + sf_par + "_" + sf_pos + ".ptx").delete();
		        
		        memory.updateMemory(hostOutput, Integer.parseInt(shift_pos,2));
		        
		        hostOutput = null;
		        
		        System.out.println("\n\nFIM");
			}
		}
		
		else{//Processo Elementar
			Integer output = Integer.parseInt(outputPosAttr);
			String [] op = input.split("\\+");
			int numStates = (int)Math.pow(2, op[0].split(",").length);
			int tam = numStates / (memory.getMemorySize()/2);
			int resto = numStates%(memory.getMemorySize()/2);
			ConcurrentHashMap<Integer, Complex> result = new ConcurrentHashMap<Integer, Complex>();
			String [] operations;
			int begin = 0;
			int end = 0;
			if(output<resto){
				begin = output*(tam+1);
				end = (output+1)*(tam+1);
			}else{
				begin = output*tam;
				end = (output+1)*(tam);
			}
			System.out.println("Processo "+output+":  ["+begin+","+end+"]");
			
			int pos = 0;
			Complex newAmplitude;
			for(int i=begin; i<end; i++){
				operations = ReadOperations(input, i);
				newAmplitude = Execute(tam, operations, i, memory);
				result.put(pos, newAmplitude);
				pos++;
			}
			memory.updateMemory(result, (output+(memory.getMemorySize()/2)));
		}
	}
	
/*	private void clear(){
		len.clear();
		positions.clear();
		widths.clear();
		elementsList.clear();
		
		matrixList = null;
		lastMatrix = null;
		initPos = null;
		widthList = null;
		dimensions = null;
		exps = null;
		elementsNumber = null;
		columns = null;
	}
*/	
	//@SuppressWarnings("unchecked")
	private void SetZeroOne(String[][] funcao){
		Zero = new Vector <Integer>();
		One = new Vector <Integer>();
		System.out.println("SomaDim: " + soma_dim);
		for (int i = 0; i < funcao.length; i++){
			SetRec(funcao[i], 0, 0, 0, 0);
			SetRec(funcao[i], 1, 0, 0, 0);
		}		
	}
	
	//@SuppressWarnings("unchecked")
	private void SetRec(String[] funcao, int l, int f, int zero, int one){
		Complex value0, value1;
		
		value0 = GetValue(funcao[f], l, 0);
		value1 = GetValue(funcao[f], l, 1);
			
		zero *= 2;
		one *= 2;
			
		if (value0.equals(Complex.ZERO)) zero += 1;
		if (value1.equals(Complex.ZERO)) one += 1;
		
		if (f < funcao.length - 1){
			SetRec(funcao, 0, f+1, zero, one);
			SetRec(funcao, 1, f+1, zero, one);
		}
		else{
			Zero.add(zero);
			One.add(one);
		}
		
	}
	
	
	private Page PageBuilder(String[] funcao, String[] pos, String[] par){
		Page page= new Page();
		Bloco bloco;
		page.blocos = new Vector <Bloco>();
		
		Complex value;
		int mod;
		//int lineZ, lineO;
		int line, column;
		
		shift_pos  += pos[0];
		shift_par += par[0];
		
		if (GPU) widths.add((int)Math.pow(2, funcao.length));
		
		num_reads = num_reads * (int)Math.pow(2, funcao.length) / par.length;
		num_writes = num_writes * (int)Math.pow(2, funcao.length) / pos.length;
		
		mod = 0;
		//int position = 0;
		//int total = 0;
		
		page.dim = (int) Math.pow(2, funcao.length);
		
		soma_dim += page.dim;
		
		len.add(funcao.length);
		
		for (int l = 0; l < pos.length; l++){
			bloco = new Bloco();
			bloco.pos = new Vector<Integer>();
			bloco.complex = new Vector<Complex>();
	
			Integer p = 0;
			
			for (int c = 0; c < par.length; c++){
				
				Complex temp = Complex.ONE;
				//lineZ = 0;
				//lineO = 0;
				int op = 0;

				while (!temp.equals(Complex.ZERO) && op < funcao.length){ //verificar se tem que tirar a comparação com ZERO
					line = Integer.parseInt(pos[l].substring(op, op+1), 2);
					column = Integer.parseInt(par[c].substring(op, op+1), 2);
					value = GetValue(funcao[op], line, column);
					
					temp = temp.times(value);
					op+=1;
				}
				
				bloco.pos.add(Integer.parseInt(par[c], 2));
				bloco.complex.add(temp);
				positions.add(Integer.parseInt((pos[l]+par[c]),2));
				//total++;
				p++;
				//position++;
			}
			
			mod += bloco.complex.size();
			
			bloco.VPPid = Integer.parseInt(pos[l], 2);
			
			page.blocos.add(bloco);
		}
		
		if (GPU){
	    	iterations *= mod;
	    	elementsList.add(totalElements);
	    	totalElements += page.dim * page.blocos.size();
		}
		return page;
	}

	//@SuppressWarnings("unchecked")
	private void CreateGpuArrays(Vector <Page> Pages){
		int i, tam, pageNum, posNum;//, mod, m1, m2, num_paginas;
		short matrixNum;
		Enumeration <Bloco> b;
		Enumeration <Page> p;
		Enumeration <Integer> posList;
		Enumeration <Complex> c;
		Page page;
		Bloco bloco;
		Complex complex;
		int pos;
		//
		tam = widths.size();
		
		matrixList = new float[(elementsList.lastElement())*2];
		widthList = new byte[tam];
		lineZero = new int[soma_dim];
		lineOne = new int[soma_dim];
		initMatrix = new int[tam];
		initPos = new int[soma_dim];
		widthLine = new byte[soma_dim];
		dimensions = new int[tam];
		dimensions_sum = new int[tam];
		exps = new byte[tam];
		elementsNumber = new int[tam];
		columns = new int[tam];
		//
		//
		matrixNum = 0; //Contador do elemnto da matriz
		pageNum = 0;   //Contador das paginas percorridas
		posNum = 0;
		bloco = null;
		
		posList = positions.elements();
		p = Pages.elements();
		page = p.nextElement();
		
		int init = 0;
		int np = matrixNum;
		
		for (; p.hasMoreElements(); ){ //Percorre as páginas
			b = page.blocos.elements();
			//num_paginas = page.blocos.size();
					
			//mod = 0;
			bloco = page.blocos.firstElement();
			np -= (bloco.VPPid * page.dim);
			initMatrix[pageNum] = np;
			
			while(b.hasMoreElements()){ //Percorre os seus blocos
				bloco = b.nextElement();
				c = bloco.complex.elements();
				posList = bloco.pos.elements();

				initPos[init+bloco.VPPid] = matrixNum;
				widthLine[init+bloco.VPPid] = (byte)bloco.complex.size(); 
				//mod += bloco.complex.size();
				for (; c.hasMoreElements(); ){ //Percorre o vetor de complexos e posições dos blocos
					complex = c.nextElement();
					pos = posList.nextElement();
					
					matrixList[(matrixNum+pos) * 2] = (float)complex.getReal();
					matrixList[(matrixNum+pos) * 2 +1] = (float)complex.getImaginary();
					
					posNum++;
				}
				matrixNum += page.dim;
			}
			np = matrixNum;
			init += page.dim;
			
			widthList[pageNum] = (byte)bloco.complex.size();
			dimensions[pageNum] = page.dim;
			
			page = p.nextElement();
			pageNum++;
		}

		
		lastMatrix = new float[(positions.size()-posNum)*2];

		b = page.blocos.elements();
		matrixNum = 0;
		posNum = 0;
		//mod = 0;
		np = matrixNum;
		
		bloco = page.blocos.firstElement();
		np -= (bloco.VPPid * page.dim);
		initMatrix[pageNum] = np;
		
		for (; b.hasMoreElements(); ){ //Percorre os seus blocos
			bloco = b.nextElement();
			c = bloco.complex.elements();
			posList = bloco.pos.elements();
			
			//mod += bloco.complex.size();
			
			initPos[init+bloco.VPPid] = matrixNum;
			
			for (; c.hasMoreElements(); ){ //Percorre o vetor de complexos e posições dos blocos
				complex = c.nextElement();
				pos = posList.nextElement();
				
				lastMatrix[(matrixNum + pos) * 2] = (float)complex.getReal();
				lastMatrix[(matrixNum + pos) * 2 +1] = (float)complex.getImaginary();
				
				posNum++;
			}
			matrixNum += page.dim;
		}
		dimensions[pageNum] = page.dim;

		
		iterations = 1; 
		for (i = 0; i < tam; i++){
			elementsNumber[i] = elementsList.elementAt(i);
			exps[i] = len.elementAt(i).byteValue();
			iterations *= columns[i];
		}
		for (i = 0; i < (soma_dim - dimensions[dimensions.length-1]); i++){
			lineZero[i] = Zero.elementAt(i);
			lineOne[i] = One.elementAt(i);
		}
		lineZero[i] = 0;
		lineOne[i] = 0;
		for (int j = i ; j < soma_dim; j++){
			lineZero[i] = lineZero[i] & Zero.elementAt(j);
			lineOne[i] = lineOne[i] & One.elementAt(j);
		}
		
		dimensions_sum[0] = dimensions[0];
		for (i = 1; i < tam; i++){
			dimensions_sum[i] = dimensions_sum[i-1] + dimensions[i];
		}

		elementsList.clear();
		positions.clear();
	}

	
	private void PrintGpuArrays(){
		System.out.println("stack_size " + (dimensions.length-1));
		System.out.println("stack_lines " + (soma_dim));
		System.out.println("bits " + (qubits));
		System.out.println("amp " + (dimensions[dimensions.length-1]));
		System.out.println("last_exp " + (exps[exps.length-1]));
		System.out.println("last_matrix " + (lastMatrix.length));
		System.out.println("size " + (tamanho/num_reads));
		System.out.println("shift_read " + (sf_par));
		System.out.println("shift_write " + (sf_pos));
		System.out.println("totalElements: "+ totalElements);
		System.out.println("\n\nmatrixList: ");
		for (int i = 0; i < matrixList.length; i++){
			System.out.print(i/2 + ": " +matrixList[i]+" "+matrixList[i+1]+"\n");
			i++;
		}
		System.out.println("\n\nlastMatrix: ");
		for (int i = 0; i < lastMatrix.length; i++){
			System.out.print(+lastMatrix[i]+" "+lastMatrix[i+1]+", ");
			i++;
		}
		System.out.println("\n\nlineOne: ");
		for (int i = 0; i < lineOne.length; i++){
			System.out.print(lineOne[i]+", ");
		}
		System.out.println("\n\nlineZero: ");
		for (int i = 0; i < lineZero.length; i++){
			System.out.print(lineZero[i]+", ");
		}
		System.out.println("\n\ninitMatrix: ");
		for (int i = 0; i < initMatrix.length; i++){
			System.out.print(initMatrix[i]+", ");
		}
		System.out.println("\n\ninitPos: ");
		for (int i = 0; i < soma_dim; i++){
			System.out.print(initPos[i]+", ");
		}
		System.out.println("\n\nDimension: ");
		for (int i = 0; i < dimensions.length; i++){
			System.out.print(dimensions[i]+", ");
		}
		System.out.println("\n\nExps: ");
		for (int i = 0; i < exps.length; i++){
			System.out.print(((int) exps[i] & 0xFF)+", ");
		}
		System.out.println("\n\niterations: "+iterations);
		System.out.println("\nouterMatrices: "+outerMatrices);
	}

	private void Dimensions(){
		/*
        Defines the blocks and grid dimension.
        Each threads calculates 4 amplitudes.
    	*/
		iterations = 1;
		for (int i =0; i< (columns.length-1); i++){
			iterations *= columns[i];
		}
		
		int threads = 1;
		for (int i= 0; i < dimensions.length - 1;i++){
			threads *= (dimensions[i]);
		}
		threads /= num_writes;
		System.out.println("Threads (each thread calculates 4 amplitudes): "+threads);
		
		int size = 256;
		dimBlock = size;
		dimGrid = 1;
		if (threads > size){
			dimGrid = threads/size;
		}
		else{
			dimBlock = threads;
		}
	}
	
	private String CreatePtxFile() throws IOException{
		
		PrintWriter p = new PrintWriter(new FileOutputStream("kernel" + sf_par + "_" + sf_pos + ".cu"));
		BufferedReader in = new BufferedReader(new FileReader("cuBaseTeste"));
		
		String base = "";
		StringBuffer sb = new StringBuffer();
		
		//s = in.readLine();
		
		while (in.ready()){
			sb.append(in.readLine() + "\n");
		}
		base = sb.toString();
		in.close();
		
		base = base.replaceFirst("stack_size", String.valueOf(dimensions.length-1));
		base = base.replaceFirst("stack_lines", String.valueOf(soma_dim));
		base = base.replaceFirst("bits", String.valueOf(qubits));
		base = base.replaceFirst("amp", String.valueOf(dimensions[dimensions.length-1]));
		base = base.replaceFirst("last_exp", String.valueOf(exps[exps.length-1]));
		base = base.replaceFirst("last_matrix", String.valueOf(lastMatrix.length));
		base = base.replaceFirst("total_elements", String.valueOf(totalElements));
		base = base.replaceFirst("size", String.valueOf(tamanho/num_reads));
		base = base.replaceFirst("shift_read", String.valueOf(sf_par));
		base = base.replaceFirst("shift_write", String.valueOf(sf_pos));
		
		p.write(base);
		p.close();
		
		return preparePtxFile("kernel" + sf_par + "_" + sf_pos + ".cu");
	}
	
	private static String preparePtxFile(String cuFileName) throws IOException
	{
	    int endIndex = cuFileName.lastIndexOf('.');
	    if (endIndex == -1)
	    {
	        endIndex = cuFileName.length()-1;
	    }
	    String ptxFileName = cuFileName.substring(0, endIndex+1)+"ptx";
	
	    File cuFile = new File(cuFileName);
	    if (!cuFile.exists())
	    {
	        throw new IOException("Input file not found: " + cuFileName);
	    }
	    String modelString = " -m" + System.getProperty("sun.arch.data.model");
	    String command =
	        "nvcc" + modelString + " -ptx "+
	        cuFile.getPath()+" -o "+ptxFileName; //-arch=sm_21 flag após nvcc para poder usar printf no codigo
	
	    System.out.println("Executing\n"+command);
	    Process process = Runtime.getRuntime().exec(command);
	
	    String errorMessage =
	        new String(toByteArray(process.getErrorStream()));
	    String outputMessage =
	        new String(toByteArray(process.getInputStream()));
	    int exitValue = 0;
	    try
	    {
	        exitValue = process.waitFor();
	    }
	    catch (InterruptedException e)
	    {
	        Thread.currentThread().interrupt();
	        throw new IOException(
	            "Interrupted while waiting for nvcc output", e);
	    }
	
	    if (exitValue != 0)
	    {
	        System.out.println("nvcc process exitValue "+exitValue);
	        System.out.println("errorMessage:\n"+errorMessage);
	        System.out.println("outputMessage:\n"+outputMessage);
	        throw new IOException(
	            "Could not create .ptx file: "+errorMessage);
	    }
	
	    System.out.println("Finished creating PTX file");
	    return ptxFileName;
	}

	private void ConstantDataGPU(CUmodule module){
		long sizeArray[] = {0};
		int size;
		
		CUdeviceptr matricesC = new CUdeviceptr();
		cuModuleGetGlobal(matricesC, sizeArray, module, "matricesC");
		size = (int)sizeArray[0];
        cuMemcpyHtoD(matricesC, Pointer.to(matrixList), size);
                
        CUdeviceptr lastMatrixC = new CUdeviceptr();
		cuModuleGetGlobal(lastMatrixC, sizeArray, module, "lastMatrixC");
		size = (int)sizeArray[0];
        cuMemcpyHtoD(lastMatrixC, Pointer.to(lastMatrix), size);

        CUdeviceptr lineZeroC = new CUdeviceptr();
      	cuModuleGetGlobal(lineZeroC, sizeArray, module, "lineZeroC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(lineZeroC, Pointer.to(lineZero), size);
        
        CUdeviceptr lineOneC = new CUdeviceptr();
      	cuModuleGetGlobal(lineOneC, sizeArray, module, "lineOneC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(lineOneC, Pointer.to(lineOne), size);
        
        CUdeviceptr initMatrixC = new CUdeviceptr();
      	cuModuleGetGlobal(initMatrixC, sizeArray, module, "initMatrixC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(initMatrixC, Pointer.to(initMatrix), size);
        
        CUdeviceptr initLinesC = new CUdeviceptr();
      	cuModuleGetGlobal(initLinesC, sizeArray, module, "initLinesC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(initLinesC, Pointer.to(initPos), size);
        
        CUdeviceptr dimensionC = new CUdeviceptr();
      	cuModuleGetGlobal(dimensionC, sizeArray, module, "dimensionC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(dimensionC, Pointer.to(dimensions), size);
        
        CUdeviceptr expC = new CUdeviceptr();
      	cuModuleGetGlobal(expC, sizeArray, module, "expC");
      	size = (int)sizeArray[0];
        cuMemcpyHtoD(expC, Pointer.to(exps), size);
	}
	
	private String [] ReadOperations(String inputPosAttr, Integer position){
		String [] finalValues;
		if(inputPosAttr.startsWith("(")){
			String begin = new String("");
			String end = new String("");
			int ind;
			String [] operations = inputPosAttr.split("\\+");
			String [][] finalOperators = new String [operations.length][operations[0].split(",").length];
			String [] exp;
			String values = new String("");
			Vector<String> controlValues = new Vector<String>();
			Vector<Integer> targetQubits = new Vector<Integer>();
			for(int comp=0; comp<operations.length; comp++){
				operations[comp] = operations[comp].trim();
				operations[comp] = operations[comp].substring(1, operations[comp].length()-1);
				targetQubits.clear();
				exp = operations[comp].split(",");
				values = "";
				for(int op=0; op<exp.length; op++){
					if(exp[op].length() >= 6){
						if(exp[op].substring(0, 7).equals("Control")){
							begin = exp[op].substring(0, 7);
							ind = exp[op].indexOf("(");
							values = values + (exp[op].substring(ind+1, ind+2));
							end = exp[op].substring(ind,exp[op].length()-1);
							exp[op] = begin + end;
							exp[op] = exp[op].replace("(", "");
							exp[op] = exp[op].replace(")", "");
						}
						else if(exp[op].substring(0, 6).equals("Target")){
							ind = exp[op].indexOf("(");
							end = exp[op].substring(ind, exp[op].length()-1);
							exp[op] = end;
							exp[op] = exp[op].replace("(", "");
							exp[op] = exp[op].replace(")", "");
							targetQubits.add(Integer.valueOf(op));
						}
						finalOperators[comp][op] = exp[op];
					}else
						finalOperators[comp][op] = exp[op];
					
				}
				controlValues.add(values);
			}
			
			int qubits = finalOperators[0].length;
	
			String binNumber = Integer.toBinaryString(position);
			while(binNumber.length() < qubits)
				binNumber = "0" + binNumber;
			
			ArrayList<Integer> struct = new ArrayList<Integer>();
			for(int cont=0; cont<qubits; cont++){
				if (finalOperators[0][cont].equals("Control0") || finalOperators[0][cont].equals("Control1"))
					struct.add(Integer.valueOf(cont));
			}
			String newBin = new String("");
			int posBit;
			for (int bit=0; bit<struct.size(); bit++){
				posBit = struct.get(bit);
				newBin = newBin + binNumber.charAt(posBit);
			}
			
			String [] opList = new String [finalOperators[0].length];
			boolean found = false;
			for(int j=0; j<controlValues.size(); j++){
				if(newBin.equals(controlValues.get(j))){
					opList = finalOperators[j];
					found = true;
				}
			}
			
			if(found == false){
				opList = finalOperators[0];
				for(int op=0; op<opList.length; op++){
					if (targetQubits.contains(Integer.valueOf(op))){
						ind = targetQubits.indexOf(Integer.valueOf(op));
						opList[op] = "Id";
						targetQubits.remove(ind);
					}else if(opList[op].length() >= 7){ 
							if (opList[op].substring(0, 7).equals("Control")){
							opList[op] = opList[op].substring(0, 7) + newBin.charAt(0);
							newBin = newBin.substring(1);
						}
					}
				}
			}
			finalValues = opList;
			
		}else{
			boolean hasSwap = false;
			String [] components = inputPosAttr.split(",");
			Integer qubits = components.length;
			String binNumber = Integer.toBinaryString(position);
			while(binNumber.length() < qubits)
				binNumber = "0" + binNumber;
			Vector<Integer> struct = new Vector<Integer>();
			for(int cont=0; cont<qubits; cont++){
				if(components[cont].length() >= 4){
					if(components[cont].substring(0, 4).equals("Swap")){
						hasSwap = true;
						struct.add(Integer.valueOf(cont));
					}
				}
			}
			String newBin = new String("");
			Integer posBit;
			for(int bit=0; bit<struct.size(); bit++){
				posBit = struct.get(bit);
				newBin = newBin + binNumber.substring(posBit, posBit+1);
			}
			finalValues = new String [components.length];
			
			if (hasSwap){
				for(int i=0; i<components.length; i++){
					if(components[i].length() > 4){
						if(components[i].substring(0,4).equals("Swap")){
							components[i] = components[i]+"("+newBin.substring(0,1)+")";
							newBin = newBin.substring(1);
						}
						components[i] = components[i].trim();
					}
					finalValues[i] = components[i];
				}
			}else{
				finalValues = components;
			}
			
			if (hasSwap){
				boolean found;
				int pos;
				String temp = new String("");
				for(int op=0; op<finalValues.length; op++){
					if(finalValues[op].length() > 4){
						if(finalValues[op].substring(0,4).equals("Swap")){
							found = false;
							pos = op+1;
							while (found == false && pos < finalValues.length){
								if(finalValues[op].length()>5 && finalValues[pos].length()>5){
									if(finalValues[op].substring(0,5).equals(finalValues[pos].substring(0,5))){
										temp = finalValues[op].substring(6,7);
										finalValues[op] = finalValues[pos].substring(0,4)+finalValues[pos].substring(6,7);
										
										finalValues[pos] = finalValues[pos].substring(0,4)+temp;
										found = true;
									}
								}
								pos++;
							}
						}
					}
					finalValues[op] = finalValues[op].trim();
				}
			}
		}
		return finalValues;
	}
	
	private Complex Execute(int posOnHashs, String [] operations, Integer ind, VirdMemory memory ){
		int numVPPs, sizeVPPs, rest, offset, iter, pos, op;
		Complex temp, res;
		String binValue;
		int qubits = operations.length;
		String binNumber = Integer.toBinaryString(ind);
		while(binNumber.length() < qubits)
			binNumber = "0" + binNumber;

		if(qubits > 5){
			numVPPs = qubits/4;
			sizeVPPs = 4;
			rest = qubits%4;
			if (rest > 1){
				numVPPs = numVPPs+1;
			}
		}else{
			numVPPs = 1;
			sizeVPPs = qubits;
			rest = 0;
		}
		
		offset = 0;
		Vector<Vector<Object>> Lvpp = new Vector<Vector<Object>>();
		int sizesList [] = new int [numVPPs];
		Complex zero = Complex.ZERO;
		
		for(int vpp=0; vpp<numVPPs; vpp++){
			if(rest == 1){
				iter = sizeVPPs + 1;
				rest = 0;
			}else if (rest > 1 && vpp == numVPPs - 1){
				iter = rest;
			}else{
				iter = sizeVPPs;
			}

			pos = 0;
			Vector<Object> vpps = new Vector<Object>();
			for(int value=0; value<Math.pow((double) 2, (double)iter); value++){
				binValue = Integer.toBinaryString(value);
				while(binValue.length() < qubits)
					binValue = "0" + binValue;
				binValue = binValue.substring(qubits-iter); // Se indice for < 0, irá ocorrer erro.
				temp = Complex.ONE;
				op = 0;
				while((!temp.equals(zero)) && (op < iter)){
					temp = temp.times(GetValue(operations[offset+op], Integer.parseInt(binNumber.substring(op,op+1)), Integer.parseInt(binValue.substring(op,op+1))));
					op ++;
				}
				if(!temp.equals(zero)){
					Vector<Object> tuple = new Vector<Object>();
					tuple.add(temp);
					tuple.add(pos);
					vpps.add(tuple);
				}
				pos ++;
			}
			binNumber = binNumber.substring(iter);
			Lvpp.add(vpps);
			offset += iter;
			sizesList[vpp] = (int)Math.pow(2, qubits-offset);
		}
		ConcurrentHashMap<Integer, Complex> hash = null;
		res = ApplyValues(hash, posOnHashs, qubits, Lvpp, sizesList, memory, Complex.ONE, 0, 0, Complex.ZERO);
		return res;
	}
	
	@SuppressWarnings("unchecked")
	private Complex ApplyValues(ConcurrentHashMap<Integer, Complex> hash, int posOnHashs, int qubits, Vector<Vector<Object>> Lvpp, int [] sizesList, VirdMemory memory, Complex value, int index, int startPos, Complex res){
		int pos;
		Vector<Object> line = Lvpp.get(index);
		for(int i=0; i<line.size(); i++){
			Vector<Object> tuple = (Vector<Object>) line.get(i);
			if(index == (Lvpp.size() - 1) ){
				pos = startPos + (Integer)tuple.get(1);
				if(posOnHashs>pos){
					hash = (ConcurrentHashMap<Integer, Complex>)memory.readMemory(0);
					res = res.plus(value.times(((Complex) tuple.get(0)).times((Complex)(hash.get(pos)))));
				}else{
					int array = pos/posOnHashs;
					int position = pos-(array*posOnHashs);
					res = res.plus(value.times(((Complex) tuple.get(0)).times((Complex)(hash.get(position)))));
					hash = (ConcurrentHashMap<Integer, Complex>)memory.readMemory(array);
				}
				
			}else{
				res = ApplyValues(hash, posOnHashs, qubits, Lvpp, sizesList, memory, value.times((Complex)tuple.get(0)), index+1, startPos+((Integer)tuple.get(1)*sizesList[index]), res);
			}
		}
		return res;
	}

	private Complex GetValue(String nameOperator, int line, int x) {
		if (nameOperator.equals("X")){
			if (line == 0)
				return Complex.valueOf((double)x, 0);
			else
				return Complex.valueOf((double) 1-x, 0);
		}
		
		else if (nameOperator.equals("Y")){
			if (line == 0)
				return Complex.valueOf(0, (double) x*-1);
			else
				return Complex.valueOf(0, (double) 1-x);
		}
		else if (nameOperator.equals("Z")){
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.valueOf((double) -1*x, 0);
		}
		else if (nameOperator.equals("H")){
			if (line == 0)
				return Complex.valueOf((double) 1/Math.sqrt(2),0);
			else
				return Complex.valueOf((double) (Math.pow(-1, (double)x) * (1/Math.sqrt(2))),0);
		}
		else if (nameOperator.equals("S")){
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.valueOf(0, (double) x);
		}
		else if (nameOperator.equals("T")){			
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.ONE.plus(Complex.I).times(Math.sqrt(0.5)).times(Complex.valueOf((double)x,0));
		}
		else if (nameOperator.equals("Id")){
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.valueOf((double) x, 0);
		}
		else if (nameOperator.equals("P0")){
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.ZERO;
		}
		else if (nameOperator.equals("P1")){
			if (line == 0)
				return Complex.ZERO;
			else
				return Complex.valueOf((double) x, 0);
		}
		else if (nameOperator.equals("Control0")){
				return Complex.valueOf((double) 1-x, 0);
		}
		else if (nameOperator.equals("Control1")){
				return Complex.valueOf((double) x, 0);
		}
		else if (nameOperator.equals("Swap0")){
				return Complex.valueOf((double) 1-x, 0);
		}
		else if (nameOperator.equals("Swap1")){
				return Complex.valueOf((double) x, 0);
		}
		else if (nameOperator.equals("Control")){
			if (line == 0)
				return Complex.valueOf((double) 1-x, 0);
			else
				return Complex.valueOf((double) x, 0);
			
		}
		return null;
	}
	
	private String[][] parserInput(String entrada){
		int begin, q;
		entrada = entrada.substring(1 , entrada.length() - 1);
		
		//separa emdim_soma substrings
		String [] partes = entrada.split("]");
		
		String[][] saida = new String[partes.length][];
		
		q = 0;
		//separa a entrada em vetores de listas de strings
		for(int i=0; i<partes.length; i++){
			begin = 2;
			if (i==0) {begin = 1;}
			
			//retira os '[' , ']' das substrings; 
			partes[i] = partes[i].substring(begin);
			
			//separa as substrings em uma lista de strings
			saida[i] = (partes[i].split(","));
			q += saida[i].length;
		}
		
		if (qubits == 0) qubits = q;
		
		return saida;
	}
	
	
    @SuppressWarnings("unchecked")
	private void ApplyValuesForQP(Vector <Page> Pages, int numPages, Vector <Integer> sizesList, VirdMemory memory, ConcurrentHashMap <Integer, Complex> men_out, Set <Integer> newValues, Complex partialValue, int pageIndex, int basePos, int memPos){
		int next_basePos, writePos, pos;
		Complex next_partialValue, temp, res;
		Page page;
		Integer VPPid;
		Enumeration <Bloco> line;
		Bloco b;
		Enumeration <Integer> p;
		Integer position;
		Enumeration <Complex> c;
		Complex complex;
		
    	if (pageIndex == (numPages - 1)){
    		ConcurrentHashMap<Integer, Complex> hash;
    		hash = (ConcurrentHashMap<Integer, Complex>)memory.readMemory(0);
    		page = Pages.elementAt(pageIndex);

    		for (line = page.blocos.elements(); line.hasMoreElements(); ){
    			res = Complex.ZERO;
    			b = line.nextElement();
    			VPPid = b.VPPid;
    			c = b.complex.elements();
    			for (p = b.pos.elements(); p.hasMoreElements(); ){
    				position = p.nextElement();
    				complex = c.nextElement();
    				pos = basePos + position;
    				res = res.plus(partialValue.times(complex.times(hash.get(pos))));
    			}
    			writePos = memPos + (VPPid *sizesList.elementAt(pageIndex));
    			temp = men_out.get(writePos);
    			if (temp != null){
    				res = res.plus(temp);
    			}
    			
    			men_out.put(writePos, res);
    			newValues.add(writePos);
    		}
    	}
    	   	
    	else{
    		page = Pages.elementAt(pageIndex);
    		for (line = page.blocos.elements(); line.hasMoreElements(); ){
    			b = line.nextElement();
    			VPPid = b.VPPid;
    			
    			c = b.complex.elements();
    			for (p = b.pos.elements(); p.hasMoreElements(); ){
    				position = p.nextElement();
    				complex = c.nextElement();
    				
    				next_basePos = basePos + (position * sizesList.elementAt(pageIndex));
    				next_partialValue = partialValue.times(complex);
    				this.ApplyValuesForQP(Pages, numPages, sizesList, memory, men_out, newValues, next_partialValue, pageIndex + 1, next_basePos, memPos + (VPPid * sizesList.elementAt(pageIndex)));
    			}
    		}
    	}
    }
	
	private static byte[] toByteArray(InputStream inputStream) throws IOException{
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        byte buffer[] = new byte[8192];
	        while (true)
	        {
	            int read = inputStream.read(buffer);
	            if (read == -1)
	            {
	                break;
	            }
	            baos.write(buffer, 0, read);
	        }
	        return baos.toByteArray();
	    }
	
	private void eraseMemory(ConcurrentHashMap<Integer, Complex> mem, int qubits){
		int valor = (int)Math.pow(2,qubits);
		
		for (int i =0; i < valor; i++)
			mem.put(i, Complex.ZERO);
	}
	
	private ConcurrentHashMap <Integer,Complex> adjustMemory(ConcurrentHashMap<Integer,Complex> mem, Set <Integer> newValues){
		ConcurrentHashMap <Integer,Complex> temp = new ConcurrentHashMap <Integer,Complex>();
		Iterator<Integer> i = newValues.iterator();
		Integer pos;
		
		for ( ;i.hasNext(); ){
			pos = i.next();
			temp.put(pos, mem.get(pos));
		}	
		
		return temp;
	}
	
/*    private Integer parseInt(String value){
    	Integer i, base;
    	base = 1;
    	i = 0;
    	char[] ch = value.toCharArray();
    	for (int it = (ch.length - 1); it >= 0; it --){
    		if (ch[it] != '0'){
    			i += base; 
    		}
    		base = base * 2;
    	}
    	return i;
    }*/
    
	@Override
	public void app(Integer value, Integer[] input, String outputAttr,
			Integer iterator, VirdMemory memory, ObjectOutputStream oos)
			throws IOException {
		// TODO Auto-generated method stub
	}
}
