/** Esta classe tem como objetivo fornecer uma esrutura para o
*   controle dos n�s, fornecendo suporte para o controle das respostas
*   e das executa��es de tarefas pelos n�s.
*/

import java.time.*;

public class controleTarefa {
	// Atributo para o controle do momento que a n� recebeu a tarefa.
	private Instant horaInicial;
	private String codUser;
	private long numero;

	/**
	* Construtor da classe "controleTarafa"
	* @param codUser String - Indica o c�digo de identifica��o do usu�rio.
	* @param codUser long - Indica qual foi o n�mero enviado para o n�.
	*/
	public controleTarefa(String codUser, long numero) {
		this.codUser = codUser;
		this.numero = numero;
		this.horaInicial = Instant.now();
	}

	/** M�todo para obter o c�digo de usu�rio, para identificar o n�.
	* @return String - C�digo do usu�rio.
	*/
	public String getCodUser() {
		return codUser;
	}

	/** M�todo para obter qual foi o n�mero enviado para o n�.
	* @return long - Retorna o n�mero enviado para o n�.
	*/	
	public long getNumero() {
		return numero;
	}

	/** M�todo para verificar se o tempo de processamento do n�.
	*   Neste m�todo � poss�vel verificar se o n� est� levando um tempo anormal
	*   para responder a tarefa. Tamb�m podemos evitar poss�veis ataques, pois um
	*   atacando pode solicitar uma tarefa e nunca entregar o resultado, afetando
	*   o processamento do resultado final.
	*   Se o tempo de execu��o for maior que o esperado, a tarefa pode ser repassada
	*   para outro n� realizar o procesamento.
	* @return long - Retorna o n�mero enviado para o n�.
	*/	
	public boolean validaTempoProcessamento(){
		Instant horaAtual = Instant.now();

		Duration tempoTotalTarefa = Duration.between( this.horaInicial, horaAtual);
		long longTempoTotalTarefa = tempoTotalTarefa.toMinutes();

		if (longTempoTotalTarefa > 5) {
			// Interrompe a execu��o da tarefa e a repassa para outro n�.
			return true;
		} else {
			// Mantem a tarefa em execu��o pelo n�.
			return false;
		}
	}
}