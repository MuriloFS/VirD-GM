package g3pd.virdgm.core;

import java.io.ObjectOutputStream;

/**Interface para execucao distribuida dos processos na VirD-GM*/
public interface VirdExec {
	//adiconar a ambos String valueAttr
	/**
	 * @param actionAttr	Acao a ser avaliada
	 * @param valueAttr		Valor para testes
	 * @param inputPosAttr	Posicoes da memoria com os paramentros de entrada
	 * @param outputAttr	Posicao da memoria para armazenar o resultado da avaliacao
	 * @param iteradorAttr	Iterador 
	 * * @param oos			Objeto para escrita na memoria
	 * */
    public boolean exec(String actionAttr, String valueAttr, String inputPosAttr, String outputPosAttr, String controlListAttr, String complementListAttr, Integer iteratorAttr, VirdMemory memory, ObjectOutputStream oos);
    /**
	 * @param actionAttr	Acao a ser avaliada
	 * @param valueAttr		Valor para testes
	 * @param inputPosAttr	Posicoes da memoria com os paramentros de entrada
	 * @param outputAttr	Posicao da memoria para armazenar o resultado da avaliacao
	 * @param iteradorAttr	Iterador 
	 * @param host			Indicacao do host que recebera o processo para avaliacao
	 * @param port			Porta onde ocorre a comunicacao com a base
	 * */
    public boolean send(String actionAttr, String valueAttr, String inputPosAttr, String outputPosAttr, String controlListAttr, String complementListAttr, Integer iteratorAttr, VirdMemory memory, String host, Integer port);
}
