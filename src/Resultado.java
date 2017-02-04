/** Esta classe tem como objetivo fornecer uma esrutura de dados para 
*   para armazenar o resultado da computação executada sobre o nó.
*   As classes "userNode" e "manager" utilizam esta classe.
*/

// Necessário o uso do Serializable para enviar a classe "Resultado" através do socket.
// Isso indica a JVM que a classe pode ser Serializable, e assim enviada através de um socket.
import java.io.Serializable;

public class Resultado implements Serializable {
	
	// Necessário para a implementação do Serializable.
	// Isto realiza a diferenciação entre as instâncias das classes.
	private static final long serialVersionUID = 1L;
	private String codUser;
	private long numero; 
	private boolean resultado;
	// Para checagem do resultado, 15 é verdadeiro, 25 é falso.
	private int checagemResultado;
	
	/**
	* Construtor da classe "resultado"
	* @param codUser String - Indica o código de identificação do usuário, 
	*                         para indicar qual é o usuário que gerou o resultado.
	* @param codUser long - Indica qual foi o número analisado pelo nó.
 	* @param resultado boolean - Indica se o número analisado é primo, 
	*                            verdadeiro se o número é primo,
	*							 falso se o número ´não é primo.
	* @param checagemResultado int - Este parâmetro é utilizado para avaliar o resultado, 
	*                                pois podem ocorrer erros na escrita do resultado, 
	*                                erros na transmissão dos dados, ataques, entre outros...
    *                                Assim temos uma maneira de avaliar o resultado obtido 
    *                                pela computação do nó.	
	*/
	public Resultado(String codUser, long numero, boolean resultado, int checagemResultado) {
		this.codUser = codUser;
		this.numero = numero;
		this.resultado = resultado;
		this.checagemResultado = checagemResultado;
	}	
	
	/** Método para obter o código de usuário, para identificar o nó.
	* @return String - Código do usuário.
	*/
	public String getCodUser() {
		return codUser;
	}

	/** Método para obter o conteúdo da checagem do resultado.
	* @return String - Código do usuário.
	*/	
	public int getChecagemResultado() {
		return checagemResultado;
	}

	/** Método para obter qual foi o número verificado pelo nó.
	* @return long - Retorna o número verificado pelo nó.
	*/	
	public long getNumero() {
		return numero;
	}

	/** Método para obter o conteúdo do resultado da computação.
	* @return boolean - Retorna o resultado da computação, indicando se o número é primo ou não.
	*/	
	public boolean getResultado() {
		return resultado;
	}
}
