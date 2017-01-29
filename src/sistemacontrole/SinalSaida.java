/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

import java.util.Random;
import javax.swing.ImageIcon;

/**
 *
 * @author alexandre
 */
public class SinalSaida {
    private static PID pid;
    private static PID pidEscravo;
    private static ObservadorDeEstados obsEstados;
    private static SeguidorDeReferencia seg;
    
    private double sinal_calculado, sinal_tratado;
    ImageIcon diagramaON = new ImageIcon(LeituraEscritaCanais.class.getResource("/imagens/diagrama_on.png"));
    ImageIcon diagramaREV = new ImageIcon(LeituraEscritaCanais.class.getResource("/imagens/diagrama_reverse.png"));
    ImageIcon diagramaOFF = new ImageIcon(LeituraEscritaCanais.class.getResource("/imagens/diagrama_off.png"));

    private double amplitude, 
                    periodo, 
                    offset, 
                    periodoMinimo, 
                    periodoMaximo,
                    amplitudeMinima,
                    amplitudeMaxima,
                    duracao,
                    aleatorioInicial;
    
    private static boolean runSinal;
    
    public boolean isGerarNovoAleatorio, isMalhaFechada;
    
    private final LeituraEscritaCanais leituraEscritaCanais;
    private final FuncoesWindow funcoesWindow;
    
    Thread sinalSaida;
    
    SinalSaida(LeituraEscritaCanais leituraEscritaCanais, FuncoesWindow funcoesWindow, PID pid, PID pidEscravo, ObservadorDeEstados obsEstados, SeguidorDeReferencia seg){
        this.leituraEscritaCanais = leituraEscritaCanais;
        this.funcoesWindow = funcoesWindow;
        SinalSaida.pid = pid;
        SinalSaida.pidEscravo = pidEscravo;
        SinalSaida.obsEstados = obsEstados;
        SinalSaida.seg = seg;
        
        this.isGerarNovoAleatorio = true;
        runSinal = true;
        this.leituraEscritaCanais.criarGraficoSaida();
        
        int tipoOnda = 0;
        
        //se for degrau
        if("degrau".equals(this.funcoesWindow.GetFuncaoSelecionada())){
            this.amplitude = this.funcoesWindow.GetAmplitude();
            this.offset = this.funcoesWindow.GetOffset();
            tipoOnda = 2;
        }
        //se for senoide, serra ou quadrada
        else if(!"aleatoria".equals(this.funcoesWindow.GetFuncaoSelecionada())){
            this.amplitude = this.funcoesWindow.GetAmplitude();
            this.offset = this.funcoesWindow.GetOffset();
            this.periodo = this.funcoesWindow.GetPeriodo();

            if("senoidal".equals(this.funcoesWindow.GetFuncaoSelecionada())){
                tipoOnda = 1;   
            }
            else if("serra".equals(this.funcoesWindow.GetFuncaoSelecionada())){
                tipoOnda = 4;
            }
            else if("quadrada".equals(this.funcoesWindow.GetFuncaoSelecionada())){
                tipoOnda = 3;   
            }

        }
        else{//caso seja aleatorio
            this.offset = this.funcoesWindow.GetOffset();
            this.amplitudeMaxima = this.funcoesWindow.GetAmplitudeMaxima();
            this.amplitudeMinima = this.funcoesWindow.GetAmplitudeMinima();
            this.periodoMaximo = this.funcoesWindow.GetPeriodoMaximo();
            this.periodoMinimo = this.funcoesWindow.GetPeridoMinimo();
            tipoOnda = 5;
        }
        
        if(this.funcoesWindow.isMalhaFechada()){
            sinalSaida = new Thread(new setSaidaMalhaFechada(tipoOnda));
        }
        else{
            sinalSaida = new Thread(new setSaidaMalhaAberta(tipoOnda));
        }
        
        sinalSaida.start();
    }
    
    public class setSaidaMalhaFechada extends Thread{
        private int tipoOnda;
        setSaidaMalhaFechada(int tipoOnda){
            this.tipoOnda = tipoOnda;
        }
        
        @Override
        public void run(){
            boolean isPolos = funcoesWindow.isPolos();
            while(runSinal){
                long startTime = System.currentTimeMillis();
                try{
                    switch(this.tipoOnda){
                    case 1://senoidal
                        gerarSenoidal();
                        break;
                    case 2://degrau
                        gerarDegrau();
                        leituraEscritaCanais.addSetPointCurva(sinal_calculado);
                        break;
                    case 3://quadrada
                        gerarQuadrada();
                        leituraEscritaCanais.addSetPointCurva(sinal_calculado);
                        break;
                    case 4://serra
                        gerarSerra();
                        break;
                    case 5://aleatoria
                        gerarAleatoria();
                        leituraEscritaCanais.addSetPointCurva(sinal_calculado);
                        break;
                    default:
                        System.out.println("Nenhuma onda selecionada.");
                        break;
                    }
                    if(funcoesWindow.isObsEstados()){//se o observador de estados for selecionado
                        if(isPolos){
                            //se os polos estiverem selecionados, pega os valores e roda as matrizes
                            float polos[] = funcoesWindow.getPolos();
                            float img = funcoesWindow.getImg();
                            SinalSaida.obsEstados.setP1(polos[0]);
                            SinalSaida.obsEstados.setP2(polos[1]);
                            SinalSaida.obsEstados.setImg1(img);
//                            SinalSaida.obsEstados.setQL();
                            SinalSaida.obsEstados.setL();
                        }
                        else{
                            //se os ganhos forem dados, apenas seta os ganhos
                            float ganhos[] = funcoesWindow.getGanhos();
                            SinalSaida.obsEstados.setLs(ganhos[0], ganhos[1]);
                            float polos[] = SinalSaida.obsEstados.calcularPolosObservador();
                            funcoesWindow.inputPolo1.setText(polos[0]+"");
                            funcoesWindow.polo1Img.setText(polos[1]+"");
                            funcoesWindow.inputPolo2.setText(polos[2]+"");
                            funcoesWindow.polo2Img.setText(polos[3]+"");
                        }
                    }
                    //se for PID
                    if(!funcoesWindow.seguidorRefRadio.isSelected())
                    {
                        SinalSaida.pid.setSetPoint(getSinalCalculado(), true);
                        double valorPID = SinalSaida.pid.getValorCalculado();
                        if(funcoesWindow.isCascata()){//se for cascata, calcula escravo
                            SinalSaida.pidEscravo.setSetPoint(valorPID, false);
                            valorPID = SinalSaida.pidEscravo.getValorCalculado();
                        }
                        setSinalCalculado(valorPID);
                    }
                    //se for seguidor
                    else
                    {
                        seg.Seguir(getNivelTanque(0), getNivelTanque(1), getSinalCalculado(), 100);
                        setSinalCalculado(seg.getvalorAtual());
                        double[] ganhos = seg.getGanhos();
                        funcoesWindow.inputL0.setText(ganhos[0]+"");
                        funcoesWindow.inputL1.setText(ganhos[1]+"");
                        funcoesWindow.ganhoK2input.setText(ganhos[2]+"");
                    }
                    
                    if(funcoesWindow.isObsEstados()){//se o observador de estados for selecionado
                        SinalSaida.obsEstados.setNiveisObservados(getSinalTratado(), getNivelTanque(1));
                        //atualiza ganhos na janela de funcoes
                        funcoesWindow.setL0(SinalSaida.obsEstados.getL1());
                        funcoesWindow.setL1(SinalSaida.obsEstados.getL2());
                        //adiciona valores observados ao grafico
                        leituraEscritaCanais.addNivel1Obs(obsEstados.getNivelObservado1());
                        leituraEscritaCanais.addNivel2Obs(obsEstados.getNivelObservado2());
                    }
                    checarTravas();
                    enviarBomba();
                    long stopTime = System.currentTimeMillis();
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }
    
    class setSaidaMalhaAberta extends Thread{
        private int tipoOnda;
        setSaidaMalhaAberta(int tipoOnda){
            this.tipoOnda = tipoOnda;
        }
        
        public void run(){
            while(runSinal){
                switch(this.tipoOnda){
                    case 1://senoidal
                        gerarSenoidal();
                        break;
                    case 2://degrau
                        gerarDegrau();
                        break;
                    case 3://quadrada
                        gerarQuadrada();
                        break;
                    case 4://serra
                        gerarSerra();
                        break;
                    case 5://aleatoria
                        gerarAleatoria();
                        break;
                    default:
                        System.out.println("Nenhuma onda selecionada.");
                        break;
                }

                checarTravas();
                enviarBomba();

                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    System.out.println("Sleep parado.");
                }
            }
        }
    }
    
    class SemSinal extends Thread{
        @Override
        public void run(){
            while(runSinal){
                try{
                    setSinalCalculado(0);
                    SinalSaida.pid.setSetPoint(0, false);
                    checarTravas();
                    enviarBomba();
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("SemSinal exceção :" + e);
                }
            }
        }
    }
    
    public void gerarSenoidal(){
        this.sinal_calculado = this.offset + (this.amplitude * Math.sin(Math.toRadians((getTempoGlobal() * 360) / this.periodo)));
    }
    
    public void gerarQuadrada(){
        this.sinal_calculado = this.amplitude;
            
        if(getTempoGlobal()%this.periodo > this.periodo/2){}
        else
        {
            this.sinal_calculado *= (-1);
        }
        
        this.sinal_calculado += this.offset;
    }
    
    public void gerarDegrau(){
        this.sinal_calculado = this.amplitude + this.offset;
    }
    
    public void gerarSerra(){
        this.sinal_calculado = this.offset + 2 * (this.amplitude/this.periodo) * (getTempoGlobal()%this.periodo) - this.amplitude;
    }
    
    public void gerarAleatoria(){
        if(this.isGerarNovoAleatorio){
            this.aleatorioInicial = this.leituraEscritaCanais.getTempoGlobal();
            Random r = new Random();
            this.duracao = (this.periodoMinimo) + ((this.periodoMaximo - this.periodoMinimo) * r.nextDouble());
            this.sinal_calculado = this.offset + (this.amplitudeMinima) + ((this.amplitudeMaxima - this.amplitudeMinima) * r.nextDouble());
            this.isGerarNovoAleatorio = false;
        }
        
        if(this.leituraEscritaCanais.getTempoGlobal() - this.aleatorioInicial >= this.duracao)
            this.isGerarNovoAleatorio = true;
    }
    
    public void checarTravas(){
        this.sinal_tratado = this.sinal_calculado;
        
        if (this.sinal_calculado > 4) {
            this.sinal_tratado = 4.0;
        }
        else if (this.sinal_calculado < -4) {
            this.sinal_tratado = -4.0;
        }
        
        if(this.leituraEscritaCanais.getCanalLeitura(0) > 28){
            if(this.sinal_calculado > 3.25){
                this.sinal_tratado = 2.9;
            }
            if(this.leituraEscritaCanais.getCanalLeitura(0) > 29){
                this.sinal_tratado = 0;
            }
        }
        else if(this.leituraEscritaCanais.getCanalLeitura(0) < 4 && this.sinal_calculado < 0){
            this.sinal_tratado = 0;
        }
        
        if(this.leituraEscritaCanais.getCanalLeitura(1) > 28){
            this.sinal_tratado = 0;
        }
    }

    //retorna o tempo atual do sistema
    public double getTempoGlobal(){
        return this.leituraEscritaCanais.getTempoGlobal();
    }
    
    //finaliza o sinal que estiver sendo enviado no momento
    public void stopSinal(boolean isNewSinal){
        runSinal = false;
        
        while(this.sinalSaida.isAlive()){
            try {
                Thread.sleep(5);
            } catch (InterruptedException ex) {
                System.out.println("stopSinal() Exception: "+ex);
            }
        }
        if(!isNewSinal){
            runSinal = true;
            this.sinalSaida = new Thread(new SemSinal());
            this.sinalSaida.start();
        }
    }
    
    //envia tensão para a bomba
    public void enviarBomba(){
        this.leituraEscritaCanais.setCanalSaida(0, this.sinal_tratado, this.sinal_calculado);

    }

    //get offset
    public double getOffset(){
        return this.offset;
    }
    
    //seta o sinal calculado, que é o valor sem travas
    public void setSinalCalculado(double num){
        this.sinal_calculado = num;
    }
    
    //retorna sinal calculado antes de ter travas aplicadas
    public double getSinalCalculado(){
        return this.sinal_calculado;
    }
    
    //retorna o valor tratado após ser aplicado as travas
    public double getSinalTratado(){
        return this.sinal_tratado;
    }
    
    //retorna o valor do tanque lido pela classe LeituraEscritaCanais
    public double getNivelTanque(int tanque){
        return this.leituraEscritaCanais.getCanalLeitura(tanque);
    }
}
