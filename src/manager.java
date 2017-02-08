/** Classe dedicada a implementação do gerente da rede grid.
* Aqui estão implementados todos os itens necessário para o funcionamento
* do gerente da rede grid.
* Esta classe executa o controle das tarefas e armazena os resultados enviados pelos nós.
*/

// PACOTE COM RECURSOS DE REDE
import java.net.*; 
// PACOTE COM RECUROS DE ENTRADA E SAIDA
import java.io.*;  
// PACOTE COM RECURSOS DE ARMAZENAMENTO
import java.util.*;

// PACOTE COM OS RECURSOS DE CRIPTOGRAFIA DE DADOS
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class manager extends Thread{
	public static final int DATA_PORT = 5000;
	// Localização do certificado.
	public static final String KEYSTORE_LOCATION = "managerKey.jks";
	// Senha do certificado.
	public static final String KEYSTORE_PASSWORD = "TCC2017";

	// Socket para a conexão de cada thread com a nó.
	private Socket conexaoNode;
	//indica qual o primeiro número para o teste de números primos
	private static long numeroEmTeste = 10; 
	
	private static Vector <controleTarefa> controladorTarefas;

	/** Construtor da classe "manager" 
	* @param conexaoNode Socket - Armazena o socket para a thread que esta se comunicando com um nó.
	*/
	public manager( Socket conexaoNode ){
		this.conexaoNode = conexaoNode;
		// inicializa o vetor que mantem o controle de tarefas.
		this.controladorTarefas = new Vector <controleTarefa> ();
	}

	/** Método para remover tarefas da lista.
	* @return boolean - Retorna se a tarefa foi removida da lista.
	*/
	private boolean removeTarefaControlador( long numero ) {
		int auxBusca = 0;

		for (auxBusca = 0; auxBusca < controladorTarefas.size(); auxBusca++) {
			if (controladorTarefas.elementAt(auxBusca).getNumero() == numero) {
				// remove a tarefa da lista.
				// não para a execução do loop, pois como os nós podem atrasar
				// vários nós podem estar processando o mesmo número.
				controladorTarefas.removeElementAt(auxBusca);
			}
		}

		return true;
	}

	/** Método para verificar se há tarefas atrasadas e que necessitam
	*   ser processadas novamente.
	* @return long - Retorna 0 se não há tarefas atrasadas,
	*                e retorna o numero enviado para processamento que
	*                em um nó atrasado.
	*/
	private long verificaTarefaAtrasadas() {
		int auxBusca = 0;
		for (auxBusca = 0; auxBusca < controladorTarefas.size(); auxBusca++) {
			if ( controladorTarefas.elementAt(auxBusca).validaTempoProcessamento() ) {
				// informa qual é o número que o nó está atrasando o resultado.
				return controladorTarefas.elementAt(auxBusca).getNumero() ;
			}
		}

		// Não há tarefas atrasadas.
		return 0;
	}

	/** Método para verificar se o resultada recebido foi gerado através de uma tarefa
	*   gerada pelo gerente. Isso evita que um nó malicioso execute um ataque no gerente
	*   enviado um resultado inválido e/ou não solicitado.
	* @param codUser String - Indica o código de identificação do usuário,
	*                         para indicar qual é o usuário que gerou o resultado.
	* @param numero long - Indica qual foi o número analisado pelo nó.
	* @return boolean - Retorna se o resultado recebido é válido.
	*/
	private boolean verificaTarefaSolicitada( String codUser, long numero ) {
		int auxBusca = 0;
		for (auxBusca = 0; auxBusca < controladorTarefas.size(); auxBusca++) {
			if (( controladorTarefas.elementAt(auxBusca).getNumero() == numero ) &&
				( controladorTarefas.elementAt(auxBusca).getCodUser().equals(codUser))) {
				// Informa que o resultado que foi recebido, foi gerado
				// para uma tarefa solicitada pelo gerente.
				return true;
			}
		}

		// indica que a tarefa não foi solicitada pelo gerente, ou foi solicitada para outro nó.
		// pode indicar que está ocorrendo uma tentativa de ataque.
		return false;
	}

	/** Método para inserir tarefas na lista.
	* Este método verifica se há tarefas atrasadas e delega novas tarefas aos nós.
	* @param codUser String - Indica o código de identificação do usuário,
	*                         para indicar para qual usuário a tarefa foi delegada.
	* @param codUser long - Indica qual foi o número delegado para o nó analisar.
	* @return boolean - Retorna se a tarefa foi inserida na lista.
	*/
	private boolean insereTarefaControlador( String codUser, long numero) {
		controladorTarefas.add(new controleTarefa(codUser, numero));
		return true;
	}

	/** Método para gerar tarefas para os usuários.
	* Este método verifica se há tarefas atrasadas e delega novas tarefas aos nós.
	* @param codUser String - Indica o código de identificação do usuário,
	*                         para indicar para qual usuário a tarefa foi delegada.
	* @return long - Retorna qual o número foi enviado para o nó para o processamento
	*                Este número será enviado pelo canal de comunicação.
	*/
	private synchronized long gerarTarefa( String codUser) {

		System.out.println ( "Gerando numero para a nó " +  codUser);
		long resulTarefa = 0;
		resulTarefa = verificaTarefaAtrasadas();

		if ( resulTarefa > 0) {
			// Indica que há tarefas atrasadas e que necessitam de processamento.
			insereTarefaControlador( codUser, resulTarefa);
			System.out.println ( "Enviando uma tarefa atrasada para o nó " + codUser + " numero:" + resulTarefa);
		} else if ( resulTarefa == 0) {
			resulTarefa = numeroEmTeste;
			insereTarefaControlador( codUser, numeroEmTeste);
			numeroEmTeste++;
			System.out.println ( "Enviando a tarefa numero:" + resulTarefa + " para o nó " + codUser );
		}

		// este número sera enviado para o nó processar.
		return resulTarefa;
	}
	
	/** Método para escrever os resultados recebidos dos nós usuário em disco.
	* Este método é implementado como synchronized para permitir o acesso de apenas 
	* uma thread por vez.
	* @param codUser String - Indica o código de identificação do usuário, 
	*                         para indicar qual é o usuário que gerou o resultado.
	* @param numero long - Indica qual foi o número analisado pelo nó.
 	* @param resultado boolean - Indica se o número analisado é primo, 
	*                            verdadeiro se o número é primo,
	*							 falso se o número ´não é primo.
	* @return boolean - Retorna o resultada operação, indicando se houve erros ou se a 
	*                   operação foi concluída corretamente.
	*/
	private synchronized boolean escreverArquivo( String codUser, long numero, boolean resultado) {
		/* Utilizando synchronized no método você garante que mais nenhuma
		outra instância está utilizando o mesmo método, mantendo o arquivo liberado para uso dentro desta função.
		Removendo a necessidade de semáforos e sendo mais eficiente computacionalmente.*/

		if ( ! verificaTarefaSolicitada( codUser, numero)){
			// Se a tarefa não foi solicitada, ou enviado por outro nó
			// pode ser um ataque, e o resultado deve ser inválidado.
			return false;
		}

		try {
			// Cria um arquivo em formato CSV para permitir abrir no Excel.
			FileWriter Arquivo = new FileWriter("resultado.csv", true); // por que nao usar synchronized?
			BufferedWriter escreve = new BufferedWriter(Arquivo);

			String linhaResultado;
			linhaResultado = numero + "; " + 
			                 (resultado ? "primo" : "não primo") + "; " +
					         codUser + "; ";
	        escreve.write(linhaResultado);
	        escreve.newLine();
	        escreve.flush();
	        Arquivo.close();

			// apos escrever no arquivo, podemos remover a tarefa da lista.
			removeTarefaControlador( numero );
		} 
		// Catch para falha no arquivo.
		catch ( Exception e ) { 
			System.out.println( e ); 
			return false;
		}
		
		System.err.println ( "Finalizando a escrita do arquivo..." );
		
		return true;
	}
	
	/** Método para executar o login do usuário, verificando se o nó está cadastrado no sistema,
    * é se o usuário está ativo e confiável para receber novas tarefas e enviar resultados.
	* @param codUser String - Indica o código de identificação do usuário.
	* @return boolean - Retorna o resultada operação, indicando se o usuário pode ser autenticado.
    *                   verdadeiro, se o usuário pode acessar a rede grid.
    *                   falso, se o usuário não estiver apto a acessar a rede grid.	
	*/
	private boolean loginUsuario ( String codUser ) {
		try {
			FileReader loginArq = new FileReader("login.csv");
		    BufferedReader lerLoginArq = new BufferedReader(loginArq);
		    
		    String strLogin = lerLoginArq.readLine();
			while (strLogin != null) {
				System.out.printf("%s\n", strLogin);
				String arrLogin[] = new String[2];
				arrLogin = strLogin.split(";");
				
				// Compara o codUser lido no arquivo com o codUser enviado pelo Nó. 
				if (codUser.compareTo(arrLogin[0]) == 0) {
					// Se o usuário está ativo o login é aceito.
					if (arrLogin[1].compareTo("ativo") == 0) {
						System.err.println ( "Permitindo o login do usuario " + arrLogin[0]);
						return true;
					} else {
						System.err.println ( "Negando o login do usuario " + arrLogin[0]);
						return false;
					}
				}
				
				// lê o próxio registro.
				strLogin = lerLoginArq.readLine();
			}
  
			lerLoginArq.close();
			loginArq.close();
		}
		// Catch para falha no arquivo.
		catch ( Exception e ) { 
			System.out.println( e ); 
			return false;
		}
		
		// Caso tenha lido todo o arquivo e não encontrou o usuário, deve falhar.
		return false;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Iniciar o servidor.
		System.err.println ( "Iniciando o servidor..." );
		System.setProperty("javax.net.ssl.keyStore", KEYSTORE_LOCATION);
		System.setProperty("javax.net.ssl.keyStorePassword", KEYSTORE_PASSWORD);
		try {
			// cria um servidor de sockets, referenciado por ss, na porta 5000
			SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
	        // Máximo número de conexões em backlog é 50, este número pode ser alterado conforme a necessidade.

		    SSLServerSocket SrvSckt = (SSLServerSocket) sslserversocketfactory.createServerSocket(DATA_PORT);
		    System.err.println ( "Servidor iniciado na porta 5000..." );
		    
		    // Loop esperando as conexões dos usuários
		    while (true) {
			    try {
			    	System.err.println ( "Aguardando conexao..." );
			    	Socket novaConexao = SrvSckt.accept();

			    	System.err.println ( "Iniciando nova thread..." );
			    	Thread novoNode = new manager(novaConexao);
			    	novoNode.start();
			    	
			    } catch ( Exception e) {
					System.err.println( e ); 
					System.err.println ( "Houve uma falha para fornecer acesso a um nodo..." );
			    }
		    }
			
		} catch ( Exception e ) { 
			System.err.println( e ); 
			System.err.println ( "O Servidor falhou ao criar o server socket para os nodos ..." );
		} 
	}

	/** Execução da thread de comunicação com o servidor.
	*/
	 public void run() {
		 try {
			System.err.println ( "Iniciando thread..." );
			
			// Obtem um canal para recepção de dados com o servidor
			InputStream rx = this.conexaoNode.getInputStream( );
			DataInputStream canalRxDados = new DataInputStream ( rx );
			
			// Obtem um canal para transmissão de dados com o servidor
			OutputStream tx = this.conexaoNode.getOutputStream( );
			DataOutputStream canalTxDados = new DataOutputStream ( tx );
			
			// Obtem um canal para recepção de estruturas de dados
			ObjectInputStream canalRxObjeto = new ObjectInputStream ( rx );
			
			// Obtem um canal para trassmissão de estruturas de dados
			// Not in use yet.
			//ObjectOutputStream canalTxObjeto = new ObjectOutputStream ( tx );
			
			// Verifica as informações de login.
			String leuCodUser = canalRxDados.readUTF();
			if ( loginUsuario(leuCodUser)) {
				// Indica que servidor aceitou o login do usuário
				canalTxDados.writeBoolean(true);
			} else {
				// Indica que servidor rejeitou o login do usuário
				canalTxDados.writeBoolean(false);
				
				// finalizando a thread aqui
				System.out.println ( "Fechando o socket devido a falha de login...");
				this.conexaoNode.close();
				return;
			}
			
			
			// Verifica qual o tipo de operação que o node pretende executar.
			int tipoOperacaoNode = canalRxDados.readInt();
			System.err.println ( "Tipo de operacao: " + tipoOperacaoNode );
			
			// Entra no modo para enviar uma tarefa ao node.
			if (tipoOperacaoNode == 1) {
				System.out.println ( "Enviando o numero: " + numeroEmTeste + " para teste" );
				canalTxDados.writeLong( gerarTarefa( leuCodUser ) );
				System.out.println ( "Proximo numero que vai ser testado: " + numeroEmTeste );
				
				// Verifica que o nodo entendeu a tarefa.
				boolean nodeAceitouTarefa = canalRxDados.readBoolean();
				System.out.println ( "Node aceitou a tarefa: " + nodeAceitouTarefa );
			}
			
			// Entra no modo para receber os resultados do node.
			if (tipoOperacaoNode == 2) {
				
				System.out.println ( "Lendo a resposta" );
				Resultado receberResultadoNodo = null;
				
                receberResultadoNodo = (Resultado) canalRxObjeto.readObject();
                
                // Verificar o resultado e escrever no arquivo
                if ( ((receberResultadoNodo.getResultado() == true) && 
                	 (receberResultadoNodo.getChecagemResultado() == 15)) ||
            		 ((receberResultadoNodo.getResultado() == false) && 
                     (receberResultadoNodo.getChecagemResultado() == 25)) ){
                	
                	// Escrever o resultado em disco.
                	if ( !escreverArquivo(receberResultadoNodo.getCodUser(), 
                			        receberResultadoNodo.getNumero(),
                			        receberResultadoNodo.getResultado()) ){
                		System.out.println ( "Falhou para escrever o resultado para o nodo: " +
                			        receberResultadoNodo.getCodUser());
                	}
                }
                
                receberResultadoNodo = null;
			}
			
			System.out.println ( "Fechando o socket...");
			this.conexaoNode.close();
			 
		 } catch ( Exception e ) { 
			System.err.println( e ); 
			System.err.println ( "A Thread falhou ao criar o server socket para os nodos ..." );
		} 
	 }
	 
}
