
public class Resultado {
	private int codUser;
	private long numero; 
	private boolean resultado;
	// Para checagem do resultado, 15 é verdadeiro, 25 é falso.
	private int checagemResultado;
	
	public Resultado(int codUser, long numero, boolean resultado, int checagemResultado) {
		this.codUser = codUser;
		this.numero = numero;
		this.resultado = resultado;
		this.checagemResultado = checagemResultado;
	}	
	
	public int getCodUser() {
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
