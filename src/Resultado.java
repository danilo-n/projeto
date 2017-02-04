/** Esta classe tem como objetivo fornecer uma esrutura de dados para 
*   para armazenar o resultado da computa��o executada sobre o n�.
*   As classes "userNode" e "manager" utilizam esta classe.
*/

// Necess�rio o uso do Serializable para enviar a classe "Resultado" atrav�s do socket.
// Isso indica a JVM que a classe pode ser Serializable, e assim enviada atrav�s de um socket.
import java.io.Serializable;

public class Resultado implements Serializable {
	
	// Necess�rio para a implementa��o do Serializable.
	// Isto realiza a diferencia��o entre as inst�ncias das classes.
	private static final long serialVersionUID = 1L;
	private String codUser;
	private long numero; 
	private boolean resultado;
	// Para checagem do resultado, 15 � verdadeiro, 25 � falso.
	private int checagemResultado;
	
	/**
	* Construtor da classe "resultado"
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio, 
	*                         para indicar qual � o usu�rio que gerou o resultado.
	* @param codUser long - Indica qual foi o n�mero analisado pelo n�.
 	* @param resultado boolean - Indica se o n�mero analisado � primo, 
	*                            verdadeiro se o n�mero � primo,
	*							 falso se o n�mero �n�o � primo.
	* @param checagemResultado int - Este par�metro � utilizado para avaliar o resultado, 
	*                                pois podem ocorrer erros na escrita do resultado, 
	*                                erros na transmiss�o dos dados, ataques, entre outros...
    *                                Assim temos uma maneira de avaliar o resultado obtido 
    *                                pela computa��o do n�.	
	*/
	public Resultado(String codUser, long numero, boolean resultado, int checagemResultado) {
		this.codUser = codUser;
		this.numero = numero;
		this.resultado = resultado;
		this.checagemResultado = checagemResultado;
	}	
	
	/** M�todo para obter o c�digo de usu�rio, para identificar o n�.
	* @return String - C�digo do usu�rio.
	*/
	public String getCodUser() {
		return codUser;
	}

	/** M�todo para obter o conte�do da checagem do resultado.
	* @return String - C�digo do usu�rio.
	*/	
	public int getChecagemResultado() {
		return checagemResultado;
	}

	/** M�todo para obter qual foi o n�mero verificado pelo n�.
	* @return long - Retorna o n�mero verificado pelo n�.
	*/	
	public long getNumero() {
		return numero;
	}

	/** M�todo para obter o conte�do do resultado da computa��o.
	* @return boolean - Retorna o resultado da computa��o, indicando se o n�mero � primo ou n�o.
	*/	
	public boolean getResultado() {
		return resultado;
	}
}
