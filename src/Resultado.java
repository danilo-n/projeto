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
	
	public Resultado(String codUser, long numero, boolean resultado, int checagemResultado) {
		this.codUser = codUser;
		this.numero = numero;
		this.resultado = resultado;
		this.checagemResultado = checagemResultado;
	}	
	
	public String getCodUser() {
		return codUser;
	}
	
	public int getChecagemResultado() {
		return checagemResultado;
	}
	
	public long getNumero() {
		return numero;
	}
	
	public boolean getResultado() {
		return resultado;
	}
}
