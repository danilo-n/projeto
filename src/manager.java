/** Classe dedicada a implementa��o do gerente da rede grid.
* Aqui est�o implementados todos os itens necess�rio para o funcionamento
* do gerente da rede grid.
* Esta classe executa o controle das tarefas e armazena os resultados enviados pelos n�s.
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
	// Localiza��o do certificado.
	public static final String KEYSTORE_LOCATION = "managerKey.jks";
	// Senha do certificado.
	public static final String KEYSTORE_PASSWORD = "TCC2017";

	// Socket para a conex�o de cada thread com a n�.
	private Socket conexaoNode;
	//indica qual o primeiro n�mero para o teste de n�meros primos
	private static long numeroEmTeste = 10; 
	
	private static Vector <controleTarefa> controladorTarefas;

	/** Construtor da classe "manager" 
	* @param conexaoNode Socket - Armazena o socket para a thread que esta se comunicando com um n�.
	*/
	public manager( Socket conexaoNode ){
		this.conexaoNode = conexaoNode;
		// inicializa o vetor que mantem o controle de tarefas.
		this.controladorTarefas = new Vector <controleTarefa> ();
	}

	/** M�todo para remover tarefas da lista.
	* @return boolean - Retorna se a tarefa foi removida da lista.
	*/
	private boolean removeTarefaControlador( long numero ) {
		int auxBusca = 0;

		for (auxBusca = 0; auxBusca < controladorTarefas.size(); auxBusca++) {
			if (controladorTarefas.elementAt(auxBusca).getNumero() == numero) {
				// remove a tarefa da lista.
				// n�o para a execu��o do loop, pois como os n�s podem atrasar
				// v�rios n�s podem estar processando o mesmo n�mero.
				controladorTarefas.removeElementAt(auxBusca);
			}
		}

		return true;
	}

	/** M�todo para verificar se h� tarefas atrasadas e que necessitam
	*   ser processadas novamente.
	* @return long - Retorna 0 se n�o h� tarefas atrasadas,
	*                e retorna o numero enviado para processamento que
	*                em um n� atrasado.
	*/
	private long verificaTarefaAtrasadas() {
		int auxBusca = 0;
		for (auxBusca = 0; auxBusca < controladorTarefas.size(); auxBusca++) {
			if ( controladorTarefas.elementAt(auxBusca).validaTempoProcessamento() ) {
				// informa qual � o n�mero que o n� est� atrasando o resultado.
				return controladorTarefas.elementAt(auxBusca).getNumero() ;
			}
		}

		// N�o h� tarefas atrasadas.
		return 0;
	}

	/** M�todo para verificar se o resultada recebido foi gerado atrav�s de uma tarefa
	*   gerada pelo gerente. Isso evita que um n� malicioso execute um ataque no gerente
	*   enviado um resultado inv�lido e/ou n�o solicitado.
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio,
	*                         para indicar qual � o usu�rio que gerou o resultado.
	* @param numero long - Indica qual foi o n�mero analisado pelo n�.
	* @return boolean - Retorna se o resultado recebido � v�lido.
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

		// indica que a tarefa n�o foi solicitada pelo gerente, ou foi solicitada para outro n�.
		// pode indicar que est� ocorrendo uma tentativa de ataque.
		return false;
	}

	/** M�todo para inserir tarefas na lista.
	* Este m�todo verifica se h� tarefas atrasadas e delega novas tarefas aos n�s.
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio,
	*                         para indicar para qual usu�rio a tarefa foi delegada.
	* @param codUser long - Indica qual foi o n�mero delegado para o n� analisar.
	* @return boolean - Retorna se a tarefa foi inserida na lista.
	*/
	private boolean insereTarefaControlador( String codUser, long numero) {
		controladorTarefas.add(new controleTarefa(codUser, numero));
		return true;
	}

	/** M�todo para gerar tarefas para os usu�rios.
	* Este m�todo verifica se h� tarefas atrasadas e delega novas tarefas aos n�s.
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio,
	*                         para indicar para qual usu�rio a tarefa foi delegada.
	* @return long - Retorna qual o n�mero foi enviado para o n� para o processamento
	*                Este n�mero ser� enviado pelo canal de comunica��o.
	*/
	private synchronized long gerarTarefa( String codUser) {

		System.out.println ( "Gerando numero para a n� " +  codUser);
		long resulTarefa = 0;
		resulTarefa = verificaTarefaAtrasadas();

		if ( resulTarefa > 0) {
			// Indica que h� tarefas atrasadas e que necessitam de processamento.
			insereTarefaControlador( codUser, resulTarefa);
			System.out.println ( "Enviando uma tarefa atrasada para o n� " + codUser + " numero:" + resulTarefa);
		} else if ( resulTarefa == 0) {
			resulTarefa = numeroEmTeste;
			insereTarefaControlador( codUser, numeroEmTeste);
			numeroEmTeste++;
			System.out.println ( "Enviando a tarefa numero:" + resulTarefa + " para o n� " + codUser );
		}

		// este n�mero sera enviado para o n� processar.
		return resulTarefa;
	}
	
	/** M�todo para escrever os resultados recebidos dos n�s usu�rio em disco.
	* Este m�todo � implementado como synchronized para permitir o acesso de apenas 
	* uma thread por vez.
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio, 
	*                         para indicar qual � o usu�rio que gerou o resultado.
	* @param numero long - Indica qual foi o n�mero analisado pelo n�.
 	* @param resultado boolean - Indica se o n�mero analisado � primo, 
	*                            verdadeiro se o n�mero � primo,
	*							 falso se o n�mero �n�o � primo.
	* @return boolean - Retorna o resultada opera��o, indicando se houve erros ou se a 
	*                   opera��o foi conclu�da corretamente.
	*/
	private synchronized boolean escreverArquivo( String codUser, long numero, boolean resultado) {
		/* Utilizando synchronized no m�todo voc� garante que mais nenhuma
		outra inst�ncia est� utilizando o mesmo m�todo, mantendo o arquivo liberado para uso dentro desta fun��o.
		Removendo a necessidade de sem�foros e sendo mais eficiente computacionalmente.*/

		if ( ! verificaTarefaSolicitada( codUser, numero)){
			// Se a tarefa n�o foi solicitada, ou enviado por outro n�
			// pode ser um ataque, e o resultado deve ser inv�lidado.
			return false;
		}

		try {
			// Cria um arquivo em formato CSV para permitir abrir no Excel.
			FileWriter Arquivo = new FileWriter("resultado.csv", true); // por que nao usar synchronized?
			BufferedWriter escreve = new BufferedWriter(Arquivo);

			String linhaResultado;
			linhaResultado = numero + "; " + 
			                 (resultado ? "primo" : "n�o primo") + "; " +
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
	
	/** M�todo para executar o login do usu�rio, verificando se o n� est� cadastrado no sistema,
    * � se o usu�rio est� ativo e confi�vel para receber novas tarefas e enviar resultados.
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio.
	* @return boolean - Retorna o resultada opera��o, indicando se o usu�rio pode ser autenticado.
    *                   verdadeiro, se o usu�rio pode acessar a rede grid.
    *                   falso, se o usu�rio n�o estiver apto a acessar a rede grid.	
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
				
				// Compara o codUser lido no arquivo com o codUser enviado pelo N�. 
				if (codUser.compareTo(arrLogin[0]) == 0) {
					// Se o usu�rio est� ativo o login � aceito.
					if (arrLogin[1].compareTo("ativo") == 0) {
						System.err.println ( "Permitindo o login do usuario " + arrLogin[0]);
						return true;
					} else {
						System.err.println ( "Negando o login do usuario " + arrLogin[0]);
						return false;
					}
				}
				
				// l� o pr�xio registro.
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
		
		// Caso tenha lido todo o arquivo e n�o encontrou o usu�rio, deve falhar.
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
	        // M�ximo n�mero de conex�es em backlog � 50, este n�mero pode ser alterado conforme a necessidade.

		    SSLServerSocket SrvSckt = (SSLServerSocket) sslserversocketfactory.createServerSocket(DATA_PORT);
		    System.err.println ( "Servidor iniciado na porta 5000..." );
		    
		    // Loop esperando as conex�es dos usu�rios
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

	/** Execu��o da thread de comunica��o com o servidor.
	*/
	 public void run() {
		 try {
			System.err.println ( "Iniciando thread..." );
			
			// Obtem um canal para recep��o de dados com o servidor
			InputStream rx = this.conexaoNode.getInputStream( );
			DataInputStream canalRxDados = new DataInputStream ( rx );
			
			// Obtem um canal para transmiss�o de dados com o servidor
			OutputStream tx = this.conexaoNode.getOutputStream( );
			DataOutputStream canalTxDados = new DataOutputStream ( tx );
			
			// Obtem um canal para recep��o de estruturas de dados
			ObjectInputStream canalRxObjeto = new ObjectInputStream ( rx );
			
			// Obtem um canal para trassmiss�o de estruturas de dados
			// Not in use yet.
			//ObjectOutputStream canalTxObjeto = new ObjectOutputStream ( tx );
			
			// Verifica as informa��es de login.
			String leuCodUser = canalRxDados.readUTF();
			if ( loginUsuario(leuCodUser)) {
				// Indica que servidor aceitou o login do usu�rio
				canalTxDados.writeBoolean(true);
			} else {
				// Indica que servidor rejeitou o login do usu�rio
				canalTxDados.writeBoolean(false);
				
				// finalizando a thread aqui
				System.out.println ( "Fechando o socket devido a falha de login...");
				this.conexaoNode.close();
				return;
			}
			
			
			// Verifica qual o tipo de opera��o que o node pretende executar.
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
