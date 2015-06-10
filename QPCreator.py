from math import pi as Pi
from math import sqrt as sqrt
from cmath import exp
import numpy
import datetime
#import QPExecution

class QPCreator:
    def __init__(self):
    	self.MININPAGE = 3
    
        funcao = "Control1(1),Target1(H)"
        partialFunctions, partialPositions, partialParameters, qubits, controlList = self.StartExecution(funcao)
        print "partialFunctions:  \n", partialFunctions
        print "partialPositions:  \n", partialPositions
        print "partialParameters: \n", partialParameters
        print "qubits:            \n", qubits

        partialEscrita = 1
        partialLeitura = 1

        partialFunction, partialPosition, partialParameter = partialFunctions[0], partialPositions[0], partialParameters[0]

        pFunctions  = []
        pPositions  = []
        pParameters = []

        for pFun in partialFunctions:
            for i in range(0, partialEscrita):
                pPos = self.parcializa(partialFunction, partialPosition, qubits, partialEscrita, i)
                for j in range(0, partialLeitura):
                    pPar = self.parcializa(partialFunction, partialParameter, qubits, partialLeitura, j)
         
                    pFunctions.append(pFun)
                    pPositions.append(pPos)
                    pParameters.append(pPar)

        complementList = self.GeraComplemento(qubits, controlList)
        print "complementList: \n", complementList
        print "\n\n"
        self.CreateXML(pFunctions, pPositions, pParameters, qubits, partialEscrita, controlList, complementList)
        
    def GeraComplemento(self, qubits, controlList):
        complementList = []
        binario1 = ""
        binario2 = ""

        for i in xrange(qubits):
            verifica = 0
            for j in controlList:
                if i == int(j[0]):
                    verifica = 1
                    binario1 += "1"
                    binario2 += j[1]
            if verifica == 0:
                binario1 += "0"
                binario2 += "0"
        complementList.append([int("0b" + binario1, 2), int("0b" + binario2, 2)])

        return complementList

    def GeraBin(self, qubits, numero):
    	binario = ""

    	binario = str(bin(numero)[2:])

    	for i in xrange(len(binario), qubits):
    		binario = '0' + binario

    	return binario

    def CreateXML(self, pFunctions, pPositions, pParameters, qubits, partial, controlList, complementList):
    	xml = ""
    
    	xml += "<save arq=\"name\" pos=\"\">\n<process repr=\"env\" tipo=\"seq\" pos=\"\">\n<process repr=\"terminicio\" x=\"0\" y=\"0\" cor=\"ORANGE\"/>\n"
    	
    	if (len(pFunctions) > 1):
    		xml += "<process repr=\"env\" tipo=\"paralelo\" pos=\"\">\n"
    		
    	for i in xrange (len(pFunctions)):
    		xml+= "<process repr=\"partquantum\" qubits=\"" + str(qubits) + "\" acao=\"QGMAnalyzer\" "
    		xml+= "funcao=\"["
    		
    		for j in xrange (len(pFunctions[i])):
    			xml += "["
    			for k in xrange (len(pFunctions[i][j])):
    				xml += pFunctions[i][j][k]
    					
    				if (k != (len(pFunctions[i][j]) - 1)):
    					xml += ","
    			xml+= "]"
    			if (j != (len(pFunctions[i]) - 1)):
    				xml+= ","
    		xml += "]\" "
    			

    		xml+= "pos=\"["
    			
    		for j in xrange (len(pPositions[i])):
    			xml += "["
    			for k in xrange (len(pPositions[i][j])):
    				xml += pPositions[i][j][k]
    					
    				if (k != (len(pPositions[i][j]) - 1)):
    					xml += ","
    			xml+= "]"
    			if (j != (len(pPositions[i]) - 1)):
    				xml+= ","
    		xml += "]\" "
    			
    		xml+= "parametro=\"["
    			
    		for j in xrange (len(pParameters[i])):
    			xml += "["
    			for k in xrange (len(pParameters[i][j])):
    				xml += pParameters[i][j][k]
    					
    				if (k != (len(pParameters[i][j]) - 1)):
    					xml += ","
    			xml+= "]"
    			if (j != (len(pParameters[i]) - 1)):
    				xml+= ","
    		xml += "]\" "

    		xml+= "controlList=\"["
    			
    		for j in xrange (len(controlList)):
    			xml += "["
    			xml += controlList[j][0]
    			xml += ","
    			xml += controlList[j][1]
    			xml+= "]"
    			if (j != (len(controlList) - 1)):
    				xml+= ","
    		xml += "]\" "

    		xml+= "complementList=\"["
    		for j in xrange (len(complementList)):
    			xml += str(complementList[j][0])
    			xml += ","
    			xml += str(complementList[j][1])
    			if (j != (len(complementList) - 1)):
    				xml+= ","
    		xml += "]\" "
    			
    		xml+= "x=\"0.0\" y=\"0.0\" cor=\"BLACK\"/>\n"
    	
    	if (len(pFunctions) > 1):
    		xml+= "</process>\n"
    	
    	xml+= "<process repr=\"termfim\" x=\"1171.0\" y=\"1260.0\" cor=\"BLACK\"/>\n</process>\n</save>"
    	
    	arq = open("H" + str(qubits) + "Q_teste_" + str(partial) +".xml", "w")
    	
    	arq.write(xml)


        
    def StartExecution(self,function):
        exp = function.split("+")
        op = exp[0].split(",")
        qubits = len(op)
        sizeVPP = []
        if qubits > (self.MININPAGE + 1): # 6
            minInPage = self.MININPAGE # 5
            qtdPages = qubits/minInPage
            for i in range (qtdPages):
                sizeVPP.append(minInPage)
                
            rest = qubits%minInPage
            if rest < self.MININPAGE: # 5
                while rest > 0:
                    for i in range (qtdPages-1):
                        if rest > 0:
                            sizeVPP[i] += 1
                            rest -=1
            else:
                sizeVPP.append(rest)
                qtdPages += 1
        else:
            qtdPages = 1
            sizeVPP = [qubits]
            rest = 0
        op_of_page, reference, qubits_of_controls = self.StringToListForQP(function, sizeVPP, rest, qtdPages)

        controlList = []

        count = 0
        for i in xrange(len(op_of_page)):
            for j in xrange(len(op_of_page[i])):
            	
                for k in xrange(len(op_of_page[i][j])):
                    if op_of_page[i][j][k][0:7] == "Control":
                        controlList.append([str(count), op_of_page[i][j][k][9:10]])
                        op_of_page[i][j][k] = "Id"
                    elif op_of_page[i][j][k][0:6] == "Target":
                        op_of_page[i][j][k] = op_of_page[i][j][k][8:9]
                    count = count + 1
        
        partialFunctions, partialPositions, partialParameters = self.Case1(op_of_page[0], reference[0], qtdPages, qubits) ## For synchronization of non-controlled gates
        return partialFunctions, partialPositions, partialParameters, qubits, controlList
    
    
    def Case1(self, op_of_page, reference, qtdPages, qubits):
        positions_of_page = [] ## Values that will generate the VPP's indexes in the qGM-Analyzer. e.g. for H,H,H,H with two pages[[[0,1],[0,1]],[[0,1],[0,1]]]
        parameters_of_page = [] ## Values that will generate the first parameters of the functions in the qGM-Analyzer. e.g. [[[0,1],[0,1]],[[0,1],[0,1]]] 
        functions_of_page = []
        for pageId in xrange (qtdPages):
            qtdFunctions = len(op_of_page[pageId])
            functions, positions, param1, ref = self.DefineParameters(op_of_page, reference, qtdFunctions, qtdFunctions, pageId)
            positions_of_page.append(positions)
            parameters_of_page.append(param1)
            functions_of_page.append(functions)
        return [functions_of_page], [positions_of_page], [parameters_of_page]
    
    def DefineParameters(self, op_of_page, reference, qtdVPP, qtdFunctions, pageId, swap = ""):
        functions = []
        positions = [] ## Values that indicates the VPP line within the Page
        param1 = [] ## First parameter of each function to fill the QPPs
        for VPPIndex in xrange (qtdFunctions):
            functions.append(op_of_page[pageId][VPPIndex])
            
        ref = []
        for VPPIndex in xrange (2**qtdVPP): ## Creates each VPP of a Lvpp
            binValue = numpy.binary_repr(VPPIndex,qtdVPP)
            
            newBin = "" ## Value that indicates the VPP line within the Page
            binParam1 = ""
            pos = 0
            bits = []
            swapInd = 0
            for bit in xrange(qtdFunctions):
                if reference[pageId][bit] in ["Target", "DC"]:
                    newBin = newBin + binValue[pos:pos + 1]
                    binParam1 = binParam1 + binValue[pos:pos + 1]
                    pos += 1
                elif reference[pageId][bit] == "Swap":
                    bit = swap[swapInd:swapInd + 1]
                    binParam1 = binParam1 + bit
                    newBin = newBin + str(1 - int(bit))
                    swapInd += 1
                else:
                    newBin = newBin + reference[pageId][bit]
                    binParam1 = binParam1 + reference[pageId][bit]

            positions.append(newBin)
            param1.append(binParam1)
            ref.append(newBin)

        return functions, positions, param1, ref
    
    def ChangeLines(self, op_of_page, binFromReference, qtdVPP, qtdFunctions, pageId):
        ## Creates one Page with the disjunct lines of previous QPP.
        functions = []
        positions = [] ## Values that indicates the VPP line within the Page
        param1 = [] ## First parameter of each function to fill the QPPs
        reference = []
        for VPPIndex in xrange (qtdFunctions):
            functions.append(op_of_page[pageId][VPPIndex])
        for VPPIndex in xrange (2**qtdFunctions): ## Creates each VPP of a Lvpp
            binValue = numpy.binary_repr(VPPIndex,qtdFunctions)
            if binValue not in binFromReference[pageId]:
                positions.append(binValue)
                param1.append(binValue)
        if positions == reference:
            return [], "No Page", []
        else:
            return functions, positions, param1
    
    def Case3(self, QPPsOperators, QPPsReferences, qubits_of_controls, qtdPages, qubits, hasNonId):
        QPPPositions = []
        QPPParameters = []
        QPPFunctions = []
        
        for qpp in xrange (len(QPPsOperators)):
            functions_of_page, positions_of_page, parameters_of_page = self.Case3_1(QPPsOperators[qpp], QPPsReferences[qpp], qtdPages, qubits)
            QPPPositions.append(positions_of_page)
            QPPParameters.append(parameters_of_page)
            QPPFunctions.append(functions_of_page)
        
        searching = True
        index = -1
        while searching:
            index += 1
            ref = QPPsReferences[index]
            searching = False
            for page in xrange (len(ref)):
                for op in xrange (len(ref[page])):
                    if ref[page][op] == "Target" and QPPsOperators[index][page][op] == "Id":
                        searching = True
        if hasNonId:
            op_of_page = QPPsOperators[index]
            reference = QPPsReferences[index]
            for pageId in xrange (qtdPages): 
                qtdFunctions = len(op_of_page[pageId])
                for cont in xrange (qtdFunctions):
                    if reference[pageId][cont] == "Target":
                        op_of_page[pageId][cont] = "Id" ## Change whatever the target is to "Id"

            lastQPPFunctions, lastQPPPositions, lastQPPParameters = self.Case3_2(op_of_page, reference, qubits_of_controls, qtdPages, qubits)
            
            for partial in xrange(len(lastQPPPositions)):
                QPPFunctions.append(lastQPPFunctions[partial])
                QPPPositions.append(lastQPPPositions[partial])
                QPPParameters.append(lastQPPParameters[partial])
        return QPPFunctions, QPPPositions, QPPParameters

    def Case3_1 (self, op_of_page, reference, qtdPages, qubits):
        positions_of_page = []
        parameters_of_page = []
        functions_of_page = []
        for pageId in xrange (qtdPages):
            qtdFunctions = len(op_of_page[pageId])
            qtdTargets = 0
            qtdDCs = 0
            for cont in xrange (qtdFunctions):
                if reference[pageId][cont] == "Target":
                    qtdTargets += 1
                elif reference[pageId][cont] == "DC":
                    qtdDCs += 1

            qtdVPP = qtdDCs + qtdTargets
            
            functions, positions, param1, ref = self.DefineParameters(op_of_page, reference, qtdVPP, qtdFunctions, pageId)

            positions_of_page.append(positions)
            parameters_of_page.append(param1)
            functions_of_page.append(functions)
        
        return functions_of_page, positions_of_page, parameters_of_page

    def Case3_2 (self, op_of_page, reference, qubits_of_controls, qtdPages, qubit):
        QPPPositions = []
        QPPParameters = []
        QPPFunctions = []
        self.ChangeControls(0, op_of_page, reference, qubits_of_controls, qtdPages, qubit, QPPPositions, QPPParameters, QPPFunctions)
        return QPPFunctions, QPPPositions, QPPParameters
    
    def Case4(self, op_of_page, reference, qtdPages, qubits):
        QPPPositions = []
        QPPParameters = []
        QPPFunctions = []
        
        for qpp in xrange(2,0,-1):
            positions_of_page = []
            parameters_of_page = []
            functions_of_page = []

            for pageId in xrange (qtdPages):
                qtdFunctions = len(op_of_page[pageId])
                qtdSwaps = 0
                for op in op_of_page[pageId]:
                    if op == "Swap":
                        qtdSwaps += 1
                qtdVPPs = qtdFunctions - qtdSwaps
                bin = numpy.binary_repr(qpp, 2)

                functions, positions, param1, ref = self.DefineParameters(op_of_page, reference, qtdVPPs, qtdFunctions, pageId, swap = bin[pageId:pageId+qtdSwaps:])
    
                positions_of_page.append(positions)
                parameters_of_page.append(param1)
                functions_of_page.append(functions)

            QPPPositions.append(positions_of_page)
            QPPParameters.append(parameters_of_page)
            QPPFunctions.append(functions_of_page)

        return QPPFunctions, QPPPositions, QPPParameters ## Returns only one quantum process

    def ChangeControls(self, index, op_of_page, references, qubits_of_controls, qtdPages, qubit, QPPPositions, QPPParameters, QPPFunctions):
        if index < len(qubits_of_controls):
            if index < len(qubits_of_controls) -1:
                for i in xrange (len(qubits_of_controls[index])):
                    newReferences = self.ChangeOP(op_of_page, references, qubits_of_controls, i, index)
                    self.ChangeControls (index+1, op_of_page, newReferences, qubits_of_controls, qtdPages, qubit, QPPPositions, QPPParameters, QPPFunctions)

                for i in xrange (len(qubits_of_controls[index])-2, -1, -1):
                    newReferences = self.ChangeOP(op_of_page, newReferences, qubits_of_controls, i, index)
                    self.ChangeControls (index+1, op_of_page, newReferences, qubits_of_controls, qtdPages, qubit, QPPPositions, QPPParameters, QPPFunctions)

            else:
                for i in xrange (len(qubits_of_controls[index])):
                    newReferences = self.ChangeOP(op_of_page, references, qubits_of_controls, i, index)
                    functions_of_page, positions_of_page, parameters_of_page = self.CreatePages(op_of_page, newReferences, qtdPages)
                    QPPPositions.append(positions_of_page)
                    QPPParameters.append(parameters_of_page)
                    QPPFunctions.append(functions_of_page)

                for i in xrange (len(qubits_of_controls[index])-2,-1,-1):
                    newReferences = self.ChangeOP(op_of_page, newReferences, qubits_of_controls, i, index)
                    functions_of_page, positions_of_page, parameters_of_page = self.CreatePages(op_of_page, newReferences, qtdPages)
                    QPPPositions.append(positions_of_page)
                    QPPParameters.append(parameters_of_page)
                    QPPFunctions.append(functions_of_page)
    
    def ChangeOP(self, op_of_page, references, qubits_of_controls, position, idControl):
        control = qubits_of_controls[idControl]
        newReferences = []
        for page in references:
            temp = []
            for op in page:
                temp.append(op)
            newReferences.append(temp)
            
        page = control[position][0]
        opPos = control[position][1]

        if references[page][opPos] == "0":
            newReferences[page][opPos] = "1"
        elif references[page][opPos] == "1":
            newReferences[page][opPos] = "0"

        return newReferences
    
    def CreatePages(self, op_of_page, reference, qtdPages):
        functions_of_page = []
        positions_of_page = []
        parameters_of_page = []
        for pageId in xrange (qtdPages): 
            qtdFunctions = len(op_of_page[pageId])
            qtdTargets = 0
            qtdDCs = 0
            for cont in xrange (qtdFunctions):
                if reference[pageId][cont] == "Target":
                    qtdTargets += 1
                    op_of_page[pageId][cont] = "Id" ## Change whatever the target is to "Id"
                elif reference[pageId][cont] == "DC":
                    qtdDCs += 1
            qtdVPP = qtdTargets + qtdDCs
            
            functions, positions, parameters, ref = self.DefineParameters(op_of_page, reference, qtdVPP, qtdFunctions, pageId)
            functions_of_page.append(functions)
            positions_of_page.append(positions)
            parameters_of_page.append(parameters)
        return functions_of_page, positions_of_page, parameters_of_page

    def StringToListForQP( self, string, sizeVPP, rest, numPages):
        if string[:1:] == "(": ## If the string contains controlled operations
            operations = string.split("+")
            if len(operations) == 1:
                return self.ParseCase3(operations[0], sizeVPP, rest, numPages) ## Parse for cases where only 1 quantum controlled operation occurs.
            else:
                return self.ParseCase4(operations, sizeVPP, rest, numPages) ## Parse for synchronization of quantum controlled operations. 

        else: ## Otherwise
            exp = string.split(",")
            return self.ParseCase1(exp, sizeVPP, rest, numPages)

    def ParseCase1(self, exp, sizeVPP, rest, numPages): ## Parse for cases where only basic gates occurs
        values = ""
        op_of_page = []
        reference = []
        pageId = 0
        count = 0
        for op1 in xrange(len(sizeVPP)):
            temp = []
            temp2 = []
            for op2 in range(sizeVPP[op1]):
                temp.append(exp[count])
                if exp[count] == "Swap":
                    temp2.append("Swap")
                else:
                    temp2.append("DC")
                count += 1
                    
            op_of_page.append(temp)
            reference.append(temp2)
            pageId += 1
        return [op_of_page], [reference], []
    
    def ParseCase3(self, operations, sizeVPP, rest, numPages): ## Parse for cases where only 1 quantum controlled operation occurs.
        operations = operations.strip()
        operations = operations[1:len(operations)-1:]
        
        exp = operations.split(",")
        values = ""
        op_of_page = []
        pageId = 0
        for op1 in xrange(len(sizeVPP)):
            temp = []
            for op2 in range(sizeVPP[op1]):
                temp.append(exp[op1*len(sizeVPP)+op2])
            op_of_page.append(temp)
            pageId += 1
        
        reference = []
        for page in xrange (len(op_of_page)):
            temp = []
            for op in xrange(len(op_of_page[page])):
                if (op_of_page[page][op][:7:] == "Control"):
                    begin = op_of_page[page][op][:7:]
                    ind = op_of_page[page][op].find("(")
                    temp.append(op_of_page[page][op][ind+1:ind+2:])
                    end = op_of_page[page][op][ind::]
                    op_of_page[page][op] = op_of_page[page][op][:7:]
                elif (op_of_page[page][op][:6:] == "Target"):
                    ind = op_of_page[page][op].find("(")
                    end = op_of_page[page][op][ind::]
                    temp.append("Target") ## Target
                    op_of_page[page][op] = end
                    op_of_page[page][op] = op_of_page[page][op].replace("(","")
                    op_of_page[page][op] = op_of_page[page][op].replace(")","")
                else:
                    temp.append("DC") ## DC = Don't Care
            reference.append(temp)
        return [op_of_page], [reference], []

    def ParseCase4(self, operations, sizeVPP, rest, numPages): ## Parse for cases where only 1 quantum controlled operation occurs.
        QPPsOperations = []
        QPPsReferences = []
        qubits_of_controls = []
        controls = []
        firstExpression = True
        
        for operation in operations:
            operation = operation.strip()
            operation = operation[1:len(operation)-1:]
            
            exp = operation.split(",")
            values = ""
            op_of_page = []
            pageId = 0
            for op1 in xrange(len(sizeVPP)):
                temp = []
                for op2 in range(sizeVPP[op1]):
                    temp.append(exp[op1*len(sizeVPP)+op2])
                op_of_page.append(temp)
                pageId += 1
            
            reference = []
            for page in xrange (len(op_of_page)):
                temp = []
                for op in xrange(len(op_of_page[page])):
                    if (op_of_page[page][op][:7:] == "Control"):
                        ind = op_of_page[page][op].find("(")
                        ## Information about controls for synchronization of controlled gates
                        if firstExpression:
                            if op_of_page[page][op][:8:] not in controls:
                                controls.append(op_of_page[page][op][:8:])
                                qubits_of_controls.append([[page,op]])
                            else:
                                index = controls.index(op_of_page[page][op][:8:])
                                qubits_of_controls[index].append([page,op])
#                        ###########
                        
                        begin = op_of_page[page][op][:7:]
                        temp.append(op_of_page[page][op][ind+1:ind+2:])
                        end = op_of_page[page][op][ind::]
                        op_of_page[page][op] = op_of_page[page][op][:7:]

                    elif (op_of_page[page][op][:6:] == "Target"):
                        ind = op_of_page[page][op].find("(")
                        end = op_of_page[page][op][ind::]
                        temp.append("Target") ## Target
                        op_of_page[page][op] = end
                        op_of_page[page][op] = op_of_page[page][op].replace("(","")
                        op_of_page[page][op] = op_of_page[page][op].replace(")","")
                    else:
                        temp.append("DC") ## DC = Don't Care
                reference.append(temp)
            QPPsOperations.append(op_of_page)
            QPPsReferences.append(reference)
            firstExpression = False

        return QPPsOperations, QPPsReferences, qubits_of_controls


    def parcializaEscrita(self, partialFunction, partialPositions, partialParameter, qubits, partialEscrita, partialNum):
        parte_tam = (2**qubits)/partialEscrita

        minimo = parte_tam * partialNum
        maximo = parte_tam * (partialNum+1) -1

        bin_min = numpy.binary_repr(minimo, qubits)
        bin_max = numpy.binary_repr(maximo, qubits)
        """
        print "bin_min ",
        print bin_min
        print "bin_max ",
        print bin_max
        """
   
        
        novos_Positions = []
        novos_Parameters = []
        bin_pont = 0
        for i in range (0, len(partialPositions)):
            t = len(partialFunction[i])
            """
            print partialPositions[i]
            print t
            """
            novo_position = []


            bin_init = bin_pont
            bin_end = bin_init + t

            for j in range(0, len(partialPositions[i])):
                pos = partialPositions[i][j]
                #print pos

                parte_binaria_min = bin_min[bin_init:bin_end]
                parte_binaria_max = bin_max[bin_init:bin_end]


                num_min = int (parte_binaria_min, 2)
                num_max = int (parte_binaria_max, 2)
                
                app = False
                parte_num = int (pos[0:bin_end-bin_init], 2)
                if parte_num >= num_min and parte_num <= num_max:
                    app = True
                                          
                if app:
                    novo_position.append(pos)
                
                if bin_end >= qubits:
                    j = len(partialPositions[i])

            if novo_position != []:
                novos_Positions.append(novo_position)

            bin_pont += t
            if bin_pont >= qubits:
                i = len(partialPositions)

        return partialFunction, novos_Positions, partialParameter




    
    def parcializaLeitura(self, partialFunction, partialPosition, partialParameter, qubits, partialLeitura, partialNum):
        parte_tam = (2**qubits)/partialLeitura

        minimo = parte_tam * partialNum
        maximo = parte_tam * (partialNum+1) -1

        bin_min = numpy.binary_repr(minimo, qubits)
        bin_max = numpy.binary_repr(maximo, qubits)
        """
        print "bin_min ",
        print bin_min
        print "bin_max ",
        print bin_max
        """
   
        novos_Parameters = []
        bin_pont = 0
        for i in range (0, len(partialParameter)):
            t = len(partialFunction[i])
            """
            print partialParameter[i]
            print t
            """

            novo_parameter = []

            bin_init = bin_pont
            bin_end = bin_init + t

            for j in range(0, len(partialParameter[i])):
                par = partialParameter[i][j]
                
                #print par

                parte_binaria_min = bin_min[bin_init:bin_end]
                parte_binaria_max = bin_max[bin_init:bin_end]
                """
                print "parte_binaria_min " ,                
                print parte_binaria_min
                print "parte_binaria_max " ,
                print parte_binaria_max
                """
                num_min = int (parte_binaria_min, 2)
                num_max = int (parte_binaria_max, 2)
                """
                print "num_min ",
                print num_min
                print "num_max ",
                print num_max
                """
                app = False
                parte_num = int (par[0:bin_end-bin_init], 2)
                """
                print "parte_num: ",
                print parte_num
                """
                if parte_num >= num_min and parte_num <= num_max:
                    app = True
                                          
                if app:
                    novo_parameter.append(par)
                
                if bin_end >= qubits:
                    j = len(partialParameter[i])

            if novo_parameter != []:
                novos_Parameters.append(novo_parameter)

            bin_pont += t
            if bin_pont >= qubits:
                i = len(partialParameter)

        return partialFunction, partialPosition, novos_Parameters

    

    def parcializa(self, partialFunction, lista, qubits, parcialidade, parte):
        parte_tam = (2**qubits)/parcialidade

        minimo = parte_tam * parte
        maximo = parte_tam * (parte+1) -1

        bin_min = numpy.binary_repr(minimo, qubits)
        bin_max = numpy.binary_repr(maximo, qubits)
        """
        print "bin_min ",
        print bin_min
        print "bin_max ",
        print bin_max
        """
   
        nova_lista = []
        bin_pont = 0
        for i in range (0, len(lista)):
            t = len(partialFunction[i])
            """
            print partialParameter[i]
            print t
            """

            novos_valores = []

            bin_init = bin_pont
            bin_end = bin_init + t

            for j in range(0, len(lista[i])):
                valor = lista[i][j]
                
                #print par

                parte_binaria_min = bin_min[bin_init:bin_end]
                parte_binaria_max = bin_max[bin_init:bin_end]
                """
                print "parte_binaria_min " ,                
                print parte_binaria_min
                print "parte_binaria_max " ,
                print parte_binaria_max
                """
                num_min = int (parte_binaria_min, 2)
                num_max = int (parte_binaria_max, 2)
                """
                print "num_min ",
                print num_min
                print "num_max ",
                print num_max
                """
                app = False
                parte_num = int (valor[0:bin_end-bin_init], 2)
                """
                print "parte_num: ",
                print parte_num
                """
                if parte_num >= num_min and parte_num <= num_max:
                    app = True
                                          
                if app:
                    novos_valores.append(valor)
                
                if bin_end >= qubits:
                    j = len(lista[i])

            if novos_valores != []:
                nova_lista.append(novos_valores)

            bin_pont += t
            if bin_pont >= qubits:
                i = len(lista)

        return nova_lista


        
        
    def printaMatrizes(self, partialPosition, partialParameter):
        for i in range (0, len(partialPosition)):
            positions = partialPosition[i]
            parameters = partialParameter[i]
            for pos in positions:
                for par in parameters:
                    print pos+par+" ",
                print
            print

