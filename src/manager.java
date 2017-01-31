import java.net.*; // PACOTE COM RECURSOS DE REDE
import java.io.*;  // PACOTE COM RECUROS DE ENTRADA E SAIDA
import java.util.concurrent.Semaphore;


public class manager extends Thread{

	private Socket conexaoNode;
	//indica qual o primeiro n�mero para o teste de n�meros primos
	private static long numeroEmTeste = 10; 
	private static boolean arquivoOcupado = false;
	
	// iniciado em 1 por ser mutex
	static Semaphore mutexResultado = new Semaphore(1);
	
	public manager( Socket conexaoNode ){
		this.conexaoNode = conexaoNode;
	}
	
	private boolean checkArquivo(){
		return arquivoOcupado;
	}
	
	private boolean setLockArquivo(boolean lock){
		 arquivoOcupado = lock;
		 return true;
	}
	
	private synchronized boolean escreverArquivo(String codUser, long numero, boolean resultado) {
		/* Utilizando synchronized no m�todo voc� garante que mais nenhuma
		outra inst�ncia est� utilizando o mesmo m�todo, mantendo o arquivo liberado para uso dentro desta fun��o.
		Removendo a necessidade de sem�foros e sendo mais eficiente computacionalmente.*/
	/*	
		System.err.println ( "Tentando escrever no arquivo..." );
		while (checkArquivo()) {
			// Melhorar este loop
			// Usar semaforo
		}
		
		setLockArquivo(true);
		*/
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
		} 
		// Catch para falha no arquivo.
		catch ( Exception e ) { 
			System.out.println( e ); 
			return false;
		}
		
		System.err.println ( "Finalizando a escrita do arquivo..." );
		
		return true;
	}
	
	// Este m�todo � utilizado para o login do usu�rio quando ele solicita uma nova tarefa e
	// quando envia os resultados, para garantir que os resultados vem de N�s v�lidos e confi�veis.
	private boolean loginUsuario ( String codUser ) {
		try {
			FileReader loginArq = new FileReader("login.csv");
		    BufferedReader lerLoginArq = new BufferedReader(loginArq);
		    
		    String strLogin = lerLoginArq.readLine();
			while (strLogin != null) {
				System.out.printf("%s\n", strLogin);
				String arrLogin[] = new String[2];
				arrLogin = strLogin.split(";");
				System.out.println(" --> arrLogin[0]:" + arrLogin[0]);
				System.out.println(" --> arrLogin[1]:" + arrLogin[1]);
				
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
		try {
			// cria um servidor de sockets, referenciado por ss, na porta 5000
		    ServerSocket SrvSckt = new ServerSocket( 5000 );
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
				canalTxDados.writeLong(numeroEmTeste);
				numeroEmTeste++;
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
