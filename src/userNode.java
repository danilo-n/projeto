import java.net.*; // PACOTE DE REDE
import java.io.*;  // PACOTE COM RECUROS DE ENTRADA E SAIDA


public class userNode {

	private int codNode;
	private long numeroParaVerificar;
	private boolean resultadoTeste;
	
	public userNode(int codNode) {
		this.codNode = codNode;
		this.numeroParaVerificar = 0;
		this.resultadoTeste = false;
	}
	
	private void setNumero(long numeroParaVerificar) {
		this.numeroParaVerificar = numeroParaVerificar;
	}
	
	private void setResultado(boolean resultadoTeste) {
		this.resultadoTeste = resultadoTeste;
	}
	
	public int getCodNode(){
		return codNode;
	}
	
	public long getNumero(){
		return numeroParaVerificar;
	}
	
	public boolean getResultado(){
		return resultadoTeste;
	}
	
	public boolean conexaoServidor( int tipoAcesso, String enderecoServidor, int portServidor) {
		try {
// TODO: Aqui o acesso ao servidor deve ser passado por 
//       por par�metro. 
			System.out.println( "Tentando conexao com o servidor ..." );
			Socket socketServidor = new Socket( enderecoServidor, portServidor );
			System.out.println( "Conectado ao servidor" );
			
			// Obtem um canal para recep��o de dados com o servidor
			InputStream rx = socketServidor.getInputStream( );
			DataInputStream canalRxDados = new DataInputStream ( rx );
			
			// Obtem um canal para transmiss�o de dados com o servidor
			OutputStream tx = socketServidor.getOutputStream( );
			DataOutputStream canalTxDados = new DataOutputStream ( tx );
			
			// Obtem um canal para recep��o de estruturas de dados
			// Not in use yet.
			//ObjectInputStream canalRxObjeto = new ObjectInputStream ( rx );
			
			// Obtem um canal para trassmiss�o de estruturas de dados
			ObjectOutputStream canalTxObjeto = new ObjectOutputStream ( tx );
			
// TODO: Enviar dados de login
			
			// Indica o tipo de acesso para o servidor.
			System.out.println ( "Conectando ao servidor, Acesso " + 
			                     (tipoAcesso == 1 ? "solicitando tarefa" : "enviando resultados") +
			                     ", tipoAcesso: " +  tipoAcesso);
			canalTxDados.writeInt(tipoAcesso);
			
			// Entra no estado para recebendo tarafas 
			if (tipoAcesso == 1) {
				// Recebe uma tarefa do servidor, nesse caso o recebe o n�mero para verificar se � primo.
				long numeroParaVerificar = canalRxDados.readLong();
				setNumero(numeroParaVerificar);
				System.out.println ( "Recebido numero " + getNumero() );
				
				System.out.println ( "Informando ao servidor que vamos iniciar a computa��o dos dados.");
				canalTxDados.writeBoolean(true);
			}
			
			// Entra no estado para enviar resultados 
			if (tipoAcesso == 2) {
				int checagemResultado = 0;
				if (getResultado()){
					// IF resultadoTeste igual true.
					checagemResultado = 15;
				} else {
					// IF resultadoTeste igual false.
					checagemResultado = 25;
				}
				
				// Cria uma estrutura de dados para enviar as informa��es de resultado para o servidor.
				Resultado enviarResultado = new Resultado(getCodNode(), getNumero(), getResultado(), checagemResultado);
				
				System.out.println ( "Enviando os resultados obtidos para o servidor...");
				// Transmite a estrutura com a resposta.
				canalTxObjeto.writeObject(enviarResultado);
				canalTxObjeto.flush();
				System.out.println ( "Resultados enviados ao servidor...");
			}
			
			// Como a computa��o pode levar muito para retornar o resultado,
			// temos que fechar o socket para evitar deixar uma conex�o sem uso em aberto,
			// que nesse caso, pode ser uma brecha de seguran�a no sistema do computador N�.
			System.out.println ( "Encerrando o socket...\n" );
			socketServidor.close();
		}
		// Catch para o inicializa��o das conex�es.
		catch ( Exception e ) { 
			System.out.println( e ); 
			return false;
		}
		
		return true;
	}
	
	// Este m�todo computa a tarefa solicitada pelo servidor, nesse caso este m�todo verifica
	// se o n�mero repassado pelo servidor � primo ou n�o.
	public boolean computaTarefa(){
		// Armazena a quantidade de divisores descobertos.
		int quantDivisores = 0;
		
		for (long i = 1; i <= getNumero(); i++){
			if ((getNumero() % i) == 0) {
				quantDivisores++;
			}
		}
		
		if (quantDivisores == 2){
			// O n�mero em teste � primo
			setResultado(true);
			System.out.println ( "O numero " + getNumero() + " � primo...\n" );
		} else {
			// O n�mero em teste n�o � primo
			setResultado(false);
			System.out.println ( "O numero " + getNumero() + " n�o � primo...\n" );
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println ( "Iniciando o modulo cliente...\n" );
		
		//TODO: Ler dados do servidor e do usu�rio por par�metro.
		// TODO: criar um sistema de login.
		
		//Criar um objeto da classe.
		userNode objTestePrimo = new  userNode(11);
		
		// Iniciando uma conex�o para solicitar uma tarefa para o servidor.
		if ( objTestePrimo.conexaoServidor(1, "127.0.0.1", 5000) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para solicitar uma tarefa.\n" );
		}
		
		// Inicia a computa��o da tarefa.
		if ( objTestePrimo.computaTarefa() ){
			System.out.println ( "Falha para computar a tarefa.\n" );
		}
		
		// Iniciando uma conex�o para enviar os resultados para o servidor.
		if ( objTestePrimo.conexaoServidor(2, "127.0.0.1", 5000) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para enviar os resultados.\n" );
		}
		
		System.out.println ( "Finalizando o modulo cliente...\n" );
	}
}
