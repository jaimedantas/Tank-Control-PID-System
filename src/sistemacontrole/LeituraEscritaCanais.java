/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

import javax.swing.JOptionPane;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author alexandre
 */
public class LeituraEscritaCanais {
    
    //Views a serem controladas
    private LoginWindow loginWindow;
    private MainWindow mainWindow;
    private Simulacao plantaSimulacao;
    
    //cria conexao
    private QuanserClient quanserClient;
    
    //variável para testes offline
    private boolean isOffline;
    
    //valor dos sinais calculados e do tratado
    private double sinalTratadoValor, sinalCalculadoValor;
    
    //vetor de valores atuais dos sensores
    private double[] valoresCanais = new double[7];
    
    //vetor de canais selecionados
    private boolean[] canaisSelecionados = new boolean[7];
            
    //tempo global de referência
    private double tempoGlobal;
    private static long tempoInicial;
    
    //curvas dos gráficos
    private final XYSeries sinal_tratado, sinal_calculado, setPointCurva;
    private final XYSeries[] sinal_entrada;
    private final XYSeries nivelObs1Graf, nivelObs2Graf;
    private final XYSeries valor_P, valor_I, valor_D, erro_PID;
    private static boolean isBusy;
    
    private static leituraEscritaSync leituraEscrita;
    
    Thread relogio;
            
    LeituraEscritaCanais(LoginWindow loginWindow, MainWindow mainWindow){
        //setar views
        this.loginWindow = loginWindow;
        this.mainWindow = mainWindow;
        this.plantaSimulacao = new Simulacao();
        
        //checar se foi login offline
        this.isOffline = this.loginWindow.isOffline();
        
        //cria a conexão com a planta caso não seja offline
        if(!this.isOffline){
            try{
                this.quanserClient = new QuanserClient(this.loginWindow.GetIP(), this.loginWindow.GetPorta());
            } catch (QuanserClientException ex) {
                JOptionPane.showMessageDialog(this.loginWindow,"Erro ao se conectar a planta!","Erro!", JOptionPane.ERROR_MESSAGE);
                System.out.println("Erro ao se conectar a planta!");
            }
        }
        else{
            JOptionPane.showMessageDialog(this.loginWindow,"Planta não conectada! \nModo de simulação ativado.","Atenção!", JOptionPane.WARNING_MESSAGE);
            this.mainWindow.setTitle(this.mainWindow.getTitle() + " (Offline)");
        }
        
        //variável de tempo global para gráfico
        this.tempoGlobal = 0;
        
        //inicializar curvas dos gráficos
        this.sinal_calculado = new XYSeries("Saida 0 - Calculado");
        this.sinal_tratado = new XYSeries("Saida 0 - Tratada");
        this.sinal_entrada = new XYSeries[7];
        this.valor_P = new XYSeries("Parametro P");
        this.valor_I = new XYSeries("Parametro I");
        this.valor_D = new XYSeries("Parametro D");
        this.erro_PID = new XYSeries("Erro calculado");
        this.setPointCurva = new XYSeries("Set Point");
        
        for(int i=0; i<7; ++i){
            this.sinal_entrada[i] = new XYSeries("Canal "+i);
        }
        
        //iniciar tempo global
        tempoInicial = System.nanoTime();
        
        //inicialização de threads
        this.relogio = new Thread(new atualizarTempoGlobal());//inicializar contador de tempo
        
        new Thread(new atualizarGraficos()).start();//inicializar atualização dos gráficos
        
        new Thread(new getCanaisValores()).start();//inicializar leitura dos canais
        
        //objetos de sincronização de leitura e escrita de canal
        leituraEscrita = new leituraEscritaSync();
        
        //graficos do observador de estados
        this.nivelObs1Graf = new XYSeries("Nivel Observado 1");
        this.nivelObs2Graf = new XYSeries("Nivel Observado 2");
    }
    
    //classe que irá incrementar o tempo global a cada 100ms
    class atualizarTempoGlobal extends Thread{
        @Override
        public void run(){
            while(true){
                try{
                    Thread.sleep(100);
                    incrementTempoGlobal();
                } catch (Exception ex) {
                    System.out.println("Erro ao atualizar tempo global");
                }
            }
        }
    }
    
    //classe que vai ficar capturando dados dos canais a cada 1ms
    public class getCanaisValores extends Thread{
        private double valor;
        private int canal;
        @Override
        public void run(){
            while(true){
                try{
                    if(!isOffline){
                        for(int i=0; i<2; ++i){
                            canal = i;
                            synchronized(leituraEscrita){
                                valor = leituraEscrita.lerValorCanal(i);
                            }
                            setCanalLeitura(canal, valor);
                            Thread.sleep(5);
                        }
                    }
                    else{
                        setCanalLeitura(0, plantaSimulacao.getNivelTanque1());
                        setCanalLeitura(1, plantaSimulacao.getNivelTanque2());
                        Thread.sleep(50);
                    }
                } catch (Exception e){
                    System.out.println("Erro ao ler canal "+canal+"."+ e);
                }
            }
        }
    }
    
    //atualizar gráficos de entrada e saida a cada 100ms
    class atualizarGraficos extends Thread{
        @Override
        public void run(){
            while(true){
                try{
                    getMainWindow().PainelSaida.repaint();
                    Thread.sleep(50);
                    getMainWindow().PainelEntrada.repaint();
                    getMainWindow().setTanque1Progress(((getCanalLeitura(0)/30)*100));
                    getMainWindow().setTanque2Progress(((getCanalLeitura(1)/30)*100));

                    Thread.sleep(400);
                } catch (Exception e){
                    System.out.println("Erro ao atualizar gráficos.");
                }
            }
        }
    }
    
    //retorna main view
    public MainWindow getMainWindow(){
        return this.mainWindow;
    }
    
    //retorna o valor atual do tempo global
    public double getTempoGlobal(){
        return this.tempoGlobal;
    }
    
    //incrementar o tempo global do programa
    public void incrementTempoGlobal(){
        double tempDiferenca;
        tempDiferenca = System.nanoTime() - LeituraEscritaCanais.tempoInicial;
        tempDiferenca /= 1e9;
        this.tempoGlobal = tempDiferenca;
    }
    
    //setar valor de um canal de leitura
    public void setCanalLeitura(int canal, double valor){
        this.valoresCanais[canal] = valor;
        this.sinal_entrada[canal].add(this.tempoGlobal, this.valoresCanais[canal]);
    }
    
    //get valor de um canal especifico
    public double getCanalLeitura(int canal){
        return this.valoresCanais[canal];
    }
    
    //enviar valor para um canal de saida
    public void setCanalSaida(int canal, double sinal_tratado_trava, double sinal_calculado){
        if(!this.isOffline){
                if(Double.isNaN(sinal_tratado_trava)){
                    System.out.println("Nao eh double");
                }
                synchronized(leituraEscrita){
                    leituraEscrita.enviarValorCanal(canal, (double) sinal_tratado_trava);
                }
        }
        else{
            plantaSimulacao.niveltank1Discreto(sinal_tratado_trava);
            plantaSimulacao.niveltank2Discreto();
        }
        
        this.sinal_tratado.add(this.tempoGlobal, sinal_tratado_trava);
        this.sinal_calculado.add(this.tempoGlobal, sinal_calculado);
        
        if(sinal_tratado_trava>0) {
            getMainWindow().AtualizarDiagrama("ON");
        }
        else if(sinal_tratado_trava<0){
            getMainWindow().AtualizarDiagrama("RV");
        }
        else if(sinal_tratado_trava==0){
            getMainWindow().AtualizarDiagrama("OFF");
        }
    }
    
    //checa se a planta está conectada ou não
    public boolean isOffline(){
        return this.isOffline;
    }
    
    //gera o gráfico das entradas e imprime na janela principal
    public void criarGraficoEntrada(){
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "",
                "Tempo (s)",
                "Altura (cm)",
                createDatasetEntrada(),
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel graficoGerado = new ChartPanel(xylineChart);

        this.mainWindow.PainelEntrada.setLayout(null);
        this.mainWindow.PainelEntrada.removeAll();
        this.mainWindow.PainelEntrada.add(graficoGerado);
        graficoGerado.setBounds(this.mainWindow.PainelEntrada.getVisibleRect());
        final XYPlot plot = xylineChart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(120.0);
    }
    
    //gera o gráfico da saida e imprime na janela principal
    public void criarGraficoSaida(){
        JFreeChart xylineChart = ChartFactory.createXYLineChart(
                "",
                "Tempo (s)",
                "Tensão (V)",
                createDataset(),
                PlotOrientation.VERTICAL,
                true, true, false);
        ChartPanel graficoGerado = new ChartPanel(xylineChart);

        this.mainWindow.PainelSaida.setLayout(null);
        this.mainWindow.PainelSaida.add(graficoGerado);
        graficoGerado.setBounds(this.mainWindow.PainelSaida.getVisibleRect());
        final XYPlot plot = xylineChart.getXYPlot();
        ValueAxis axis = plot.getDomainAxis();
        axis.setAutoRange(true);
        axis.setFixedAutoRange(120.0);
    }
    
    //criar o dataset dos canais de entrada
    public  XYDataset createDatasetEntrada() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            System.out.println("createDatasetEntrada() exception: " +ex);
        }
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        for(int i=0; i<7 ; ++i){
            if(this.canaisSelecionados[i]){
                dataset.addSeries(this.sinal_entrada[i]);
            }
        }
        dataset.addSeries(this.setPointCurva);
        
        if(mainWindow.isNivelObs1Visible()){
            dataset.addSeries(this.nivelObs1Graf);
        }
        if(mainWindow.isNivelObs2Visible()){
            dataset.addSeries(this.nivelObs2Graf);
        }
        return dataset;
    }
    
    //criar o dataset dos sinais de saida
    public  XYDataset createDataset() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            System.out.println("createDataset() exception: " +ex);
        }
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(this.sinal_calculado);
        dataset.addSeries(this.sinal_tratado);
        return dataset;
    }
    
    //pega os canais que foram selecionados
    public void setCanaisSelecionados(){
        this.canaisSelecionados = this.mainWindow.isCanalSelected();
    }
    
    //gerar e mostrar gráficos de entradas
    public void gerarGraficosEntrada(){
        setCanaisSelecionados();
        criarGraficoEntrada();
    }
    
    public void iniciarThreads(){
        this.relogio.start();
    }
    
    //funções sincronizadas de leitura e escrita dos canais, para evitar
    //leitura e escrita simultanea
    class leituraEscritaSync{
        public synchronized double lerValorCanal(int canal){
            try{
                return (6.25 * quanserClient.read(canal));
            }
            catch (Exception e){
                System.out.println("Erro na leitura do canal "+canal+": "+e);
            }
            return 0;
        }

        public synchronized void enviarValorCanal(int canal, double valor){
            try{
                quanserClient.write(canal, valor);
            } catch(Exception e){
                System.out.println("Erro na escrita do canal "+canal+": "+e);
            }
        }
    }
    
    public void addSetPointCurva(double setPoint){
        this.setPointCurva.add(this.tempoGlobal, setPoint);
    }
    
    public void addNivel1Obs(double nivel){
        this.nivelObs1Graf.add(this.tempoGlobal, nivel);
    }
    
    public void addNivel2Obs(double nivel){
        this.nivelObs2Graf.add(this.tempoGlobal, nivel);
    }
}
