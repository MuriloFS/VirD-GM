package g3pd.virdgm.core;

import java.util.Vector;

import org.w3c.dom.Node;
/**
 * Implementacao de um Processo Elementar
 * */
public class VirdProcElem {
    Integer     procID = 0;
    String  actionAttr;
    Boolean done;
    Boolean somdet;
    Boolean proj;
    String  inputPosAttr;
    String  outputPosAttr;
    Integer iterator;
    String valueAttr;
    Boolean quantum;

    Node	node_true;
    Node	node_false;
    Vector <Node> nodeList;

    /**
     * Construtor que recebe os parametros que caracterizam o processo
     * 
     * @param actionAttr	acao a ser avaliada
     * @param valueAttr		valor 
     * @param outputPosAttr	posicao de memoria onde o resultado da avaliacao sera armazenado
     * @param inputPosAttr 	posicao(es) de memoria a serem utilizadas na avaliacao com os dados de entrada
     * @param procID		ID do processo
     * @param iterator		Iterador do processo 
     * */
    VirdProcElem(String actionAttr, String valueAttr, String outputPosAttr, String inputPosAttr, int procID, Integer iterator) {
        this.inputPosAttr  = inputPosAttr;
        this.actionAttr    = actionAttr;
        this.outputPosAttr = outputPosAttr;
        this.procID        = procID;
        this.done          = false;
        this.somdet		   = false;
        this.proj		   = false;
        this.iterator =    iterator;
        this.valueAttr = valueAttr;
        this.quantum = false;
        if (iterator == null){
        	this.quantum = true;
        }
    }

    public boolean getDone() {
        return this.done;
    }

    public void setDone(boolean status) {
        this.done = status;
    }
    /**
     * Retorna a acao a ser avalida pelo processo
     * */
    public String getActionAttr() {
        return actionAttr;
    }
    /**Seta a acao a ser avaliada
     * @param actionAttr	Nome da acao a ser avaliada*/
    public void setActionAttr(String actionAttr) {
        this.actionAttr = actionAttr;
    }
    /**Retorna o valor da variavel value*/
    public String getValueAttr() {
        return valueAttr;
    }
    /**Seta a variavel value
     * @param valueAttr		Valor para comparacoes e testes*/
    public void setValueAttr(String valueAttr) {
        this.valueAttr = valueAttr;
    }
    /**Retorna a posicao de saida na memoria para o processo*/
    public String getOutputPosAttr() {
        return outputPosAttr;
    }
    /**Seta a posicao de saida na memoria
     * @param outputPosAttr 	Posicao de memoria para saida de dados*/
    public void setOutputPosAttr(String outputPosAttr) {
        this.outputPosAttr = outputPosAttr;
    }
    /**Retorna a(s) posicao(es) da memoria com os parametros de entrada*/
    public String getInputPosAttr() {
        return inputPosAttr;
    }
    /**Seta a(s) posicao(es) da memoria com os parametros de entrada
     * @param inputPosAttr	Posicao(es) de memoria com os parametros de entrada*/
    public void setInputPosAttr(String inputPosAttr) {
        this.inputPosAttr = inputPosAttr;
    }
    /**Retorna o ID do processo*/
    public int getProcID() {
        return procID;
    }
    /**Seta o valor do ID do processo
     * @param procID	Novo ID*/
    public void setProcID(int procID) {
        this.procID = procID;
    }
    /**Retorna o valor do iterador*/
    public Integer getIterator() {
    	return this.iterator;
    }
    /**Seta o valor do iterador
     * @param iterador	Novo valor do iterador*/
    public void setIterator(Integer iterator) {
    	this.iterator = iterator;
    }
    /**Seta o indicador de soma deterministica 
     * @param flag	TRUE indica que é uma soma deterministica*/
    public void setSomdetNode(Boolean flag)
    {
    	this.somdet = flag;
    }
    /**Retorna a indicacao de soma deterministica*/
    public Boolean getSomdetNode()
    {
    	return this.somdet;
    }
    /**Retorna a indicacao de soma deterministica*/
    public Boolean isSomdetNode()
    {
    	return this.somdet;
    }
    /**Seta o nodo como parte VERDADEIRA de uma soma deterministica
     * @param node_true		Indica que o nodo e a parte VERDADEIRA da soma deterministica*/
    public void setSomdetNodeTrue(Node node_true)
    {
    	this.node_true = node_true;
    }
    /**Retorna a indicacao se o nodo e a parte VERDADEIRA da soma deterministica*/
    public Node getSomdetNodeTrue()
    {
    	return this.node_true;
    }
    /**Seta o nodo como parte FALSA de uma soma deterministica
     * @param node_false	Indica que o nodo e a parte FALSA da soma deterministica*/
    public void setSomdetNodeFalse(Node node_false)
    {
    	this.node_false = node_false;
    }
    /**Indica se o nodo e a parte FALSA da soma deterministica*/
    public Node getSomdetNodeFalse()
    {
    	return this.node_false;
    }
    /**Descricao do processo*/
    public String toString() {
    	return "Proc: " + procID + " action: " + actionAttr + " value: " + valueAttr + " in " + inputPosAttr + " out: " + outputPosAttr + " iter: " + iterator;

    }
    public Boolean isProjNode(){
    	return proj;
    }
    public void setProjNode(Boolean flag){
    	this.proj = flag;
    	this.nodeList = new Vector<Node>();
    }
    public void setNodeList(Node node){
    	this.nodeList.add(node);
    }
    public Vector<Node> getNodeList(){
    	return this.nodeList;
    }
}
