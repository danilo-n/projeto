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
//       por parâmetro. 
			System.out.println( "Tentando conexao com o servidor ..." );
			Socket socketServidor = new Socket( enderecoServidor, portServidor );
			System.out.println( "Conectado ao servidor" );
			
			// Obtem um canal para recepção de dados com o servidor
			InputStream rx = socketServidor.getInputStream( );
			DataInputStream canalRxDados = new DataInputStream ( rx );
			
			// Obtem um canal para transmissão de dados com o servidor
			OutputStream tx = socketServidor.getOutputStream( );
			DataOutputStream canalTxDados = new DataOutputStream ( tx );
			
			// Obtem um canal para recepção de estruturas de dados
			// Not in use yet.
			//ObjectInputStream canalRxObjeto = new ObjectInputStream ( rx );
			
			// Obtem um canal para trassmissão de estruturas de dados
			ObjectOutputStream canalTxObjeto = new ObjectOutputStream ( tx );
			
// TODO: Enviar dados de login
			
			// Indica o tipo de acesso para o servidor.
			System.out.println ( "Conectando ao servidor, Acesso " + 
			                     (tipoAcesso == 1 ? "solicitando tarefa" : "enviando resultados") +
			                     ", tipoAcesso: " +  tipoAcesso);
			canalTxDados.writeInt(tipoAcesso);
			
			// Entra no estado para recebendo tarafas 
			if (tipoAcesso == 1) {
				// Recebe uma tarefa do servidor, nesse caso o recebe o número para verificar se é primo.
				long numeroParaVerificar = canalRxDados.readLong();
				setNumero(numeroParaVerificar);
				System.out.println ( "Recebido numero " + getNumero() );
				
				System.out.println ( "Informando ao servidor que vamos iniciar a computação dos dados.");
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
				
				// Cria uma estrutura de dados para enviar as informações de resultado para o servidor.
				Resultado enviarResultado = new Resultado(getCodNode(), getNumero(), getResultado(), checagemResultado);
				
				System.out.println ( "Enviando os resultados obtidos para o servidor...");
				// Transmite a estrutura com a resposta.
				canalTxObjeto.writeObject(enviarResultado);
				canalTxObjeto.flush();
				System.out.println ( "Resultados enviados ao servidor...");
			}
			
			// Como a computação pode levar muito para retornar o resultado,
			// temos que fechar o socket para evitar deixar uma conexão sem uso em aberto,
			// que nesse caso, pode ser uma brecha de segurança no sistema do computador Nó.
			System.out.println ( "Encerrando o socket...\n" );
			socketServidor.close();
		}
		// Catch para o inicialização das conexões.
		catch ( Exception e ) { 
			System.out.println( e ); 
			return false;
		}
		
		return true;
	}
	
	// Este método computa a tarefa solicitada pelo servidor, nesse caso este método verifica
	// se o número repassado pelo servidor é primo ou não.
	public boolean computaTarefa(){
		// Armazena a quantidade de divisores descobertos.
		int quantDivisores = 0;
		
		for (long i = 1; i <= getNumero(); i++){
			if ((getNumero() % i) == 0) {
				quantDivisores++;
			}
		}
		
		if (quantDivisores == 2){
			// O número em teste é primo
			setResultado(true);
			System.out.println ( "O numero " + getNumero() + " é primo...\n" );
		} else {
			// O número em teste não é primo
			setResultado(false);
			System.out.println ( "O numero " + getNumero() + " não é primo...\n" );
		}
		
		return true;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println ( "Iniciando o modulo cliente...\n" );
		
		//TODO: Ler dados do servidor e do usuário por parâmetro.
		// TODO: criar um sistema de login.
		
		//Criar um objeto da classe.
		userNode objTestePrimo = new  userNode(11);
		
		// Iniciando uma conexão para solicitar uma tarefa para o servidor.
		if ( objTestePrimo.conexaoServidor(1, "127.0.0.1", 5000) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para solicitar uma tarefa.\n" );
		}
		
		// Inicia a computação da tarefa.
		if ( objTestePrimo.computaTarefa() ){
			System.out.println ( "Falha para computar a tarefa.\n" );
		}
		
		// Iniciando uma conexão para enviar os resultados para o servidor.
		if ( objTestePrimo.conexaoServidor(2, "127.0.0.1", 5000) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para enviar os resultados.\n" );
		}
		
		System.out.println ( "Finalizando o modulo cliente...\n" );
	}
}
