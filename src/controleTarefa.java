/** Esta classe tem como objetivo fornecer uma esrutura para o
*   controle dos nós, fornecendo suporte para o controle das respostas
*   e das executações de tarefas pelos nós.
*/

import java.time.*;

public class controleTarefa {
	// Atributo para o controle do momento que a nó recebeu a tarefa.
	private Instant horaInicial;
	private String codUser;
	private long numero;

	/**
	* Construtor da classe "controleTarafa"
	* @param codUser String - Indica o código de identificação do usuário.
	* @param codUser long - Indica qual foi o número enviado para o nó.
	*/
	public controleTarefa(String codUser, long numero) {
		this.codUser = codUser;
		this.numero = numero;
		this.horaInicial = Instant.now();
	}

	/** Método para obter o código de usuário, para identificar o nó.
	* @return String - Código do usuário.
	*/
	public String getCodUser() {
		return codUser;
	}

	/** Método para obter qual foi o número enviado para o nó.
	* @return long - Retorna o número enviado para o nó.
	*/	
	public long getNumero() {
		return numero;
	}

	/** Método para verificar se o tempo de processamento do nó.
	*   Neste método é possível verificar se o nó está levando um tempo anormal
	*   para responder a tarefa. Também podemos evitar possíveis ataques, pois um
	*   atacando pode solicitar uma tarefa e nunca entregar o resultado, afetando
	*   o processamento do resultado final.
	*   Se o tempo de execução for maior que o esperado, a tarefa pode ser repassada
	*   para outro nó realizar o procesamento.
	* @return long - Retorna o número enviado para o nó.
	*/	
	public boolean validaTempoProcessamento(){
		Instant horaAtual = Instant.now();

		Duration tempoTotalTarefa = Duration.between( this.horaInicial, horaAtual);
		long longTempoTotalTarefa = tempoTotalTarefa.toMinutes();

		if (longTempoTotalTarefa > 5) {
			// Interrompe a execução da tarefa e a repassa para outro nó.
			return true;
		} else {
			// Mantem a tarefa em execução pelo nó.
			return false;
		}
	}
}