import java.net.*; // PACOTE COM RECURSOS DE REDE
import java.io.*;  // PACOTE COM RECUROS DE ENTRADA E SAIDA


public class manager extends Thread{

	private Socket conexaoNode;
	//Controla qual é o número que está sendo testado como primo.
	private static long numeroEmTeste = 10; 
	private static boolean arquivoOcupado = false;
	
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
	
	private boolean escreverArquivo(int codUser, long numero, boolean resultado) {
		
		System.err.println ( "Tentando escrever no arquivo..." );
		while (checkArquivo()) {
			// Melhorar este loop
			// Usar semaforo
		}
		
		setLockArquivo(true);
		try {
			FileWriter Arquivo = new FileWriter("resultado.csv", true);
			BufferedWriter escreve = new BufferedWriter(Arquivo);
			
			String linhaResultado;
			linhaResultado = "" + numero + "; " + 
			                 (resultado ? "primo" : "não primo") + "; " +
					         codUser + "; ";
			escreve.newLine();
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
		
		// Remove o Lock
		setLockArquivo(false);
		
		System.err.println ( "Finalizando a escrita do arquivo..." );
		
		return true;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// Iniciar o servidor.
		System.err.println ( "Iniciando o servidor..." );
		try {
			// cria um servidor de sockets, referenciado por ss, na porta 5000
		    ServerSocket SrvSckt = new ServerSocket( 5000 );
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
			
			// Verifica qual o tipo de operação que o node pretende executar.
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
			//this.conexaoNode.close();
			 
		 } catch ( Exception e ) { 
			System.err.println( e ); 
			System.err.println ( "A Thread falhou ao criar o server socket para os nodos ..." );
		} 
	 }
	 
}
