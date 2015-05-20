package g3pd.virdgm.core;

import java.util.*;

import org.w3c.dom.Node;
/**Classe que representa o grafo de execucao dos processos*/
public class VirdGraph {
    ArrayList<VirdNode> nodes;
    VirdNode         virdNode;
    /**Construtor que cria uma lista de VirdNodes*/
    VirdGraph() {
        this.nodes = new ArrayList<VirdNode>();
    }
    /**Metodo para adicao de um novo virdNode
     * @param virdNode 		Nodo a ser adicionado ao grafo*/
    void addNode(VirdNode virdNode) {
        this.nodes.add(virdNode);
    }
    /**Metodo para busca de um determinado nodo
     * @param idNode	ID do nodo a ser encontrado*/
    VirdNode getNode(int idNode) {
        for (int i = 0; i < nodes.size(); i++) {
            if ((nodes.get(i)).getId() == idNode) {
                virdNode = nodes.get(i);
            }
        }

        return virdNode;
    }
    /**Metodo para verificar o numero de nodos do grafo*/
    public int size() {
        return this.nodes.size();
    }
    /**Metodo para escrita do grafo*/
    public String toString() {
        String str = "{";

        for (int i = 0; i < nodes.size(); i++) {
            str += nodes.get(i) + ", ";
        }

        return str + "}";
    }
}

/**Classe que representa um VirdNode*/
class VirdNode {
    Object  adj;
    Integer idNode;
    /**Construtor 
     * @param id	Identificacao do nodo
     * @param adj	Objeto adjacente ao nodo*/
    VirdNode(Integer id, Object adj, Node node) {
        this.idNode = id;
        this.adj    = adj;
    }
    /**Metodo para busca do ID do nodo*/
    public Integer getId() {
        return idNode;
    }
    /**Metodo para verificar objetos adjacentes ao nodo*/
    public Object getAdj() {
        return adj;
    }
    /**Metodo para escrita do VirdNode*/
    public String toString() {
        return this.idNode + " : " + this.adj;
    }
}
