Projeto Final: Desenvolvimento de Jogo Sério para Reabilitação com IoT e libGDX

1.0 Ideia de jogo
	A proposta é um jogo em terceira pessoa, no qual um motorista deve lutar contra o tempo em diferentes estradas para atingir seu destino intacto, desviando de outros veículos e controlando sua velocidade.

2.0 Controle com IoT
Pedais (acelerador e freio): estrutura de pedal para aceleração e frenagem do veículo, baseado em pressão.
Volante: estrutura de volante responsável pelo controle de direção (movimentação lateral) do veículo. 

3.0 Estrutura do jogo
	O jogador deve sair de um ponto inicial x até um ponto final y, percorrendo uma distância de n kilometros - em que y e n são variáveis mutáveis de acordo com a fase de jogo. O percurso deve ser realizado dentro de determinado intervalo de tempo (também variável de acordo com a fase), estimulando o jogador a utilizar o comando de acelerar. Os obstáculos (outros veículos - NPC’s) estão presentes em todas as fases, forçando o jogador a desacelerar ou frear, bem como controlar o movimento lateral para desviar dos mesmos. 

4.0 Conexão com a reabilitação

	4.1 Reabilitação física
Objetivo Terapêutico: Ganho de amplitude de movimento (ADM) e fortalecimento muscular para flexão plantar (pressionar) e dorsiflexão (soltar/retornar o pé), essenciais para a marcha normal pós-lesão ou cirurgia.
Controle da Força e Coordenação Fina: Em vez de ser apenas um botão "liga/desliga", o pedal com Arduino mede a graduação do movimento. Manter a velocidade desejada treina a isometria muscular e a precisão do controle motor, evitando que o paciente apenas "chute" o pedal sem controle.

	4.2 Coordenação motora
Integração Visuomotora: O cérebro precisa processar a informação visual (posição dos obstáculos) e enviar comandos motores simultâneos e diferentes para duas partes do corpo (pés para velocidade, mãos para direção).
Treino de Dupla Tarefa (Dual-Tasking): Na reabilitação real, focar apenas no movimento do pé pode ser entediante ou cansativo. Ao adicionar a tarefa do volante (desvio de obstáculos) e a meta temporal (cronômetro), o jogo desvia o foco da dor/cansaço consciente para o desafio gamificado, estimulando a neuroplasticidade.

