/** Classe dedicada a implementação do Nó de usuário.
* Aqui estão implementados todos os itens necessário para o funcionamento
* do nó de usuário.
* Esta classe executa a computação da verificação se um número é primo, 
* e executa todas as comunicações de rede com a sistema gerente da rede grid.
*/

// PACOTE DE REDE
import java.net.*; 

// PACOTES PARA CRIPTOGRAFIA DOS DADOS NA REDE
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

// PACOTE COM RECUROS DE ENTRADA E SAIDA
import java.io.*;  
import java.math.BigInteger;


public class userNode {

	private String codNode;
	private long numeroParaVerificar;
	private boolean resultadoTeste;
	public static final String TRUSTTORE_LOCATION = "userNodeKey.jks";
	
	/** Construtor da classe "userNode"
	*  @param  codNode String - Indica o código do usuário na rede grid.
	*/
	public userNode(String codNode) {
		this.codNode = codNode;
		this.numeroParaVerificar = 0;
		this.resultadoTeste = false;
	}
	
	/** Método para configurar qual o número a ser verificado se é primo ou não.
	*  @param  numeroParaVerificar long - Número para verificar se é primo ou não.
	*/
	private void setNumero(long numeroParaVerificar) {
		this.numeroParaVerificar = numeroParaVerificar;
	}
	
	/** Método para configurar a resultado da verificação.
	*  @param  resultadoTeste boolean - Resultado da computação do nó.
	*                                   verdadeiro se o número é primo,
	*							        falso se o número ´não é primo.
	*/
	private void setResultado(boolean resultadoTeste) {
		this.resultadoTeste = resultadoTeste;
	}

	/** Método para obter o código de usuário do nó, configurado na instanciação da classe.
	* @return String - Código do usuário.
	*/	
	public String getCodNode(){
		return codNode;
	}

	/** Método para obter qual foi o número verificado pelo nó.
	* @return long - Retorna o número verificado pelo nó.
	*/
	public long getNumero(){
		return numeroParaVerificar;
	}
	
	/** Método para obter o conteúdo do resultado da computação.
	* @return boolean - Retorna o resultado da computação, indicando se o número é primo ou não.
	*/	
	public boolean getResultado(){
		return resultadoTeste;
	}
	
	/** Método para configurar a conexão com o servidor gerente da rede grid.
	* @param tipoAcesso int - Indica o tipo de conexão que o nó pretende estabelecer com o gerente da rede grid.
	*                         valor "1" - Indica que o nó quer receber uma nova tarefa.
	*                         valor "2" - Indica que o nó quer enviar os resultados para o gerente da rede grid.
	* @return boolean - indica se a conexão foi estabelecida corretamente, ou se ocorreram falhas.
	*/
	public boolean conexaoServidor( int tipoAcesso, String enderecoServidor, int portServidor) {
		try {
			System.out.println( "Tentando conexao com o servidor ..." );

			System.setProperty("javax.net.ssl.trustStore", TRUSTTORE_LOCATION);

			SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();
			/* Adicionando encriptação a conexão */
		    Socket socketServidor = ssf.createSocket(enderecoServidor, portServidor);

		    SSLSession session = ((SSLSocket) socketServidor).getSession();
		    // session.getPeerCertificates();
		    // Verificação de certificado

		    System.out.println("Peer host é " + session.getPeerHost());
		    System.out.println("Cifra é " + session.getCipherSuite());
		    System.out.println("Protocolo é " + session.getProtocol());
		    System.out.println("ID é " + new BigInteger(session.getId()));
		    System.out.println("Session criada no " + session.getCreationTime());
		    System.out.println("Session acessada no " + session.getLastAccessedTime());

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
			
			// Envia dados de login
			canalTxDados.writeUTF(codNode);
			if ( !canalRxDados.readBoolean() ) {
				System.out.println( "O servidor não validou o login, encerrando..." );
// TODO: podemos inserir outras possibilidades de testes aqui, como código malicioso.
				return false;
			}
			
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
	
	/** Este método computa a tarefa solicitada pelo servidor, nesse caso este método verifica
	* se o número repassado pelo servidor é primo ou não.
	* @return boolean - Retorna o resultado da computação, indicando se o número é primo ou não.
	*/
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
	
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		System.out.println ( "Iniciando o modulo cliente...\n" );
		
		// Verificando a lista de argumentos
		// Exemplo de execução userNode <codUser> <IP Manager> <Port Manager>
		if ( (args.length < 3) || (args.length > 4) ) {
			System.out.println ( "Exemplo de execucao: noUsuarioExe <codUser> <IP Manager> <Port Manager> <pausa>(opcional)" );
			System.out.println ( "Args" + args.length );
			return;
		}
		
		//Criar um objeto da classe.
		userNode objTestePrimo = new userNode(args[0]);
		
		// Iniciando uma conexão para solicitar uma tarefa para o servidor.
		if ( !objTestePrimo.conexaoServidor(1, args[1], Integer.parseInt(args[2])) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para solicitar uma tarefa.\n" );
			return;
		}
		
		// Inicia a computação da tarefa.
		if ( !objTestePrimo.computaTarefa() ){
			System.out.println ( "Falha para computar a tarefa.\n" );
		}
		
		// Insere um atraso para simular a diferença de tempo
		// entre o processamento em cada nó.
		// Assim as respostas são recebidas em momentos diferentes.
		if (args.length == 3) {
			int tempoAtraso = (int) Math.round( Math.random() * (20));
			System.out.println ( "Iniciando uma espera de " + tempoAtraso + " segundos ...\n" );
			Thread.sleep( (tempoAtraso *1000) );
		} else {
			Thread.sleep( (Integer.parseInt(args[3]) *1000) );
		}

		// Iniciando uma conexão para enviar os resultados para o servidor.
		if ( !objTestePrimo.conexaoServidor(2, args[1], Integer.parseInt(args[2])) ){
			System.out.println ( "Falha ao iniciar a conexao com o servidor para enviar os resultados.\n" );
			return;
		}
		
		System.out.println ( "Finalizando o modulo cliente...\n" );
	}
}
