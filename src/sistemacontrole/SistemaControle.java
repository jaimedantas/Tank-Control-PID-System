/*
Sistema de Controle LAB 1
Alexandre Luz, Jaime Dantas, Anderson e Higo Bessa
 */
package sistemacontrole;

import Jama.Matrix;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.jfree.data.xy.XYSeries;


/**
 *
 * @author jaime
 */
public class SistemaControle {
    private static LoginWindow JanelaConectar;
    private static MainWindow JanelaPrincipal;
    private static FuncoesWindow JanelaFuncao;
    private static LogWindow logWindow;
    private static LeituraEscritaCanais leituraEscritaCanais;
    private static SinalSaida sinalSaida;
    private static PID pid, pid2;
    private static ObservadorDeEstados obsEstados;
    private static SeguidorDeReferencia seg;
    
    //cria conexao
    QuanserClient quanserClient;
    
    //variaveis
    double offset, periodo, amplitude, amplitudeMaxima, amplitudeMinima, periodoMaximo,
            periodoMinimo, tempo = 0;
    
    double[] tempoEntrada = new double[7], leitura = new double[7];
    
    double setPoint;
    
    //threads de saidas
    Thread saida0 = null;
    Thread saida1 = null;
    Thread[] entrada = new Thread[7];

    //variavel de malha fechada
    boolean malhaFechadaAtivada;
    
    //variaveis de parada das threads de saida
    boolean saida0isRunning = false, saida1isRunning = false;
    
    boolean[] entradaIsRunning = new boolean[7];
    
    static final XYSeries sinal_gerado = new XYSeries("Saida 0");
    static final XYSeries[] sinal_entrada = new XYSeries[7];

    //Construtor
    SistemaControle() throws IOException{
        //criar tela de login
        JanelaConectar = new LoginWindow();
        
        //criar tela de funcoes
        JanelaFuncao = new FuncoesWindow();
        
        //criar tela principal
        JanelaPrincipal = new MainWindow();
        
        //criar janela de logs
        logWindow = new LogWindow();
        
        JanelaConectar.addConectarListener(new ConnectListener());
        JanelaConectar.pack();
        JanelaConectar.setLocationRelativeTo(null);
        JanelaConectar.setTitle("Conectar");
        JanelaConectar.setVisible(true);
        
        JanelaPrincipal.addLerListener(new LerCanais());
        JanelaPrincipal.addTipoFuncaoListener(new EscolherTipoDeFuncao());
        JanelaPrincipal.addPararSinalListener(new PararSinal());
        JanelaPrincipal.setPreferredSize(new Dimension(900, 773));
        JanelaPrincipal.pack();
        JanelaPrincipal.setLocationRelativeTo(null);
        JanelaPrincipal.setTitle("Sistema de Controle de Planta Quanser");
        JanelaPrincipal.addLogWindowOpenListener(new ShowLogWindow());


        JanelaFuncao.addGerarFuncaoListener(new GerarFuncao());
        JanelaFuncao.pack();
        JanelaFuncao.setLocationRelativeTo(null);
        JanelaFuncao.setTitle("Escolha o tipo de função");
        JanelaFuncao.setDefaultCloseOperation(JanelaFuncao.DISPOSE_ON_CLOSE);

        logWindow.pack();
        logWindow.setLocationRelativeTo(null);
        logWindow.setTitle("Logs");
    }
    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String[] args) throws IOException {
        //chama classe principal
        SistemaControle Sistema = new SistemaControle();
    }
    
    class EscolherTipoDeFuncao implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            JanelaFuncao.setVisible(true);
        }
    }
    
    class LerCanais implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            getLeituraEscrita().gerarGraficosEntrada();
        }
    }
    
    class ConnectListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e){
            //iniciar leitura dos canais
            leituraEscritaCanais = new LeituraEscritaCanais(JanelaConectar, JanelaPrincipal);
            
            JanelaConectar.setVisible(false);
            JanelaPrincipal.setVisible(true);
            leituraEscritaCanais.iniciarThreads();
            
            //inicializar PIDs
            SistemaControle.pid = new PID(SistemaControle.JanelaFuncao, SistemaControle.leituraEscritaCanais, SistemaControle.JanelaPrincipal);
            SistemaControle.pid2 = new PID(SistemaControle.JanelaFuncao, SistemaControle.leituraEscritaCanais, SistemaControle.JanelaPrincipal);
            
            //inicializar observador de estados
            SistemaControle.obsEstados = new ObservadorDeEstados();
            
            //inicializar seguidor de referência
            double[][] GG = { {0.9993, 0}, {0.000657, 0.9993} };
            double[][] HH = {{0.0296}, {0.0001}};
            double[][] CC = {{0,1}};
            SistemaControle.seg = new SeguidorDeReferencia(new Matrix(GG), new Matrix(HH), new Matrix(CC));
            seg.AumentaG();
            seg.CalculaWc();
            seg.CalcV();
        }
    }
    
    class PararSinal implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                sinalSaida.stopSinal(false);
            } catch (Exception ex){
                JOptionPane.showMessageDialog(null, "Nenhuma saída atualmente ativa.", "Aviso!", JOptionPane.WARNING_MESSAGE);
            }
        }
        
    }
    
    class ShowLogWindow implements ActionListener{
        
        @Override
        public void actionPerformed(ActionEvent e){
            JOptionPane.showMessageDialog(null, "Nenhuma saída atualmente ativa.", "Aviso!", JOptionPane.WARNING_MESSAGE);
            logWindow.setVisible(true);
        }
    }
    
    class GerarFuncao implements ActionListener{
        
        @Override
        public void actionPerformed(ActionEvent e) {
            try{
                sinalSaida.stopSinal(true);
                
            } catch (Exception ex){
//                System.out.println("Nenhuma thread rodando.");
            }
            if(SistemaControle.JanelaFuncao.isMalhaFechada()){
                JanelaFuncao.erro = "";
                int tipoControle = SistemaControle.JanelaFuncao.getSelectedControle();
                double paramPID[] = SistemaControle.JanelaFuncao.getPIDValores();
                boolean isKi = SistemaControle.JanelaFuncao.isKi();
                boolean isKd = SistemaControle.JanelaFuncao.isKd();
                
                SistemaControle.pid.setTipoControle(tipoControle);
                SistemaControle.pid.setEscravo(false);
                SistemaControle.pid.setPV(0);
                SistemaControle.pid.setPIDParametros(paramPID[0], paramPID[1], isKi, paramPID[2], isKd);
                
                //controle cascata
                if(SistemaControle.JanelaFuncao.getPV() == 1 && SistemaControle.JanelaFuncao.isCascata()){
                    if(JanelaFuncao.radioButtonCascata.isSelected()){
                        JanelaPrincipal.setValoresTanque1("Controle", JanelaFuncao.tipoControleComboBox1.getSelectedItem().toString());
                        JanelaPrincipal.setValoresTanque1("Kd", JanelaFuncao.kdTextField1.getText());
                        JanelaPrincipal.setValoresTanque1("Ki", JanelaFuncao.kiTextField1.getText());
                        JanelaPrincipal.setValoresTanque1("Kp", JanelaFuncao.kpTextField1.getText());
                        JanelaPrincipal.setValoresTanque1("ti", JanelaFuncao.tiTextField1.getText());
                        JanelaPrincipal.setValoresTanque1("td", JanelaFuncao.tdTextField1.getText());    
                    }
                    SistemaControle.pid.setPV(1);
                    tipoControle = SistemaControle.JanelaFuncao.getSelectedControle1();
                    paramPID = SistemaControle.JanelaFuncao.getPID2Valores();
                    isKi = SistemaControle.JanelaFuncao.isKi1();
                    isKd = SistemaControle.JanelaFuncao.isKd1();
                    SistemaControle.pid2.setTipoControle(tipoControle);
                    SistemaControle.pid2.setPV(0);
                    SistemaControle.pid2.setEscravo(true);
                    SistemaControle.pid2.setPIDParametros(paramPID[0], paramPID[1], isKi, paramPID[2], isKd);
                }
                //seguidor de referências
                else if(SistemaControle.JanelaFuncao.getPV() == 1 && SistemaControle.JanelaFuncao.seguidorRefRadio.isSelected()){
                    if(JanelaFuncao.radioPolos.isSelected())
                    {
                        //pegar os polos da interface
                        double pReal = Double.parseDouble(JanelaFuncao.inputPolo1.getText());
                        double pImg = Double.parseDouble(JanelaFuncao.polo1Img.getText());
                        ImagNumber polo1 = new ImagNumber(pReal, pImg);

                        pReal = Double.parseDouble(JanelaFuncao.inputPolo2.getText());
                        pImg = Double.parseDouble(JanelaFuncao.polo2Img.getText());
                        ImagNumber polo2 = new ImagNumber(pReal, pImg);

                        pReal = Double.parseDouble(JanelaFuncao.polo3input.getText());
                        pImg = Double.parseDouble(JanelaFuncao.polo3Img.getText());
                        ImagNumber polo3 = new ImagNumber(pReal, pImg);
                        
                        ImagNumber[] polos = new ImagNumber[3];
                        polos[0] = polo1;
                        polos[1] = polo2;
                        polos[2] = polo3;
                        
                        seg.setPolos(polos);
                        seg.AumentaG();
                        seg.CalculaWc();
                        seg.CalcV();
                        seg.setPolos(polos);
                        seg.Qc();
                        seg.CalcK();
                        seg.Ganhos();
                    }
                }
            }
            else{
                sinalSaida = new SinalSaida(SistemaControle.leituraEscritaCanais, 
                                            SistemaControle.JanelaFuncao, 
                                            SistemaControle.pid, 
                                            SistemaControle.pid2, 
                                            SistemaControle.obsEstados,
                                            SistemaControle.seg);
                JanelaFuncao.setVisible(false);

            }
            
            if(SistemaControle.JanelaFuncao.isMalhaFechada() && !SistemaControle.JanelaFuncao.theresError){
                JanelaFuncao.setVisible(false);
                sinalSaida = new SinalSaida(SistemaControle.leituraEscritaCanais, 
                                            SistemaControle.JanelaFuncao, 
                                            SistemaControle.pid, 
                                            SistemaControle.pid2, 
                                            SistemaControle.obsEstados,
                                            SistemaControle.seg);
            }
            else if(SistemaControle.JanelaFuncao.isMalhaFechada() && SistemaControle.JanelaFuncao.theresError){
                JanelaFuncao.mostrarErro();
            }
            
        }
    }
    
    public LeituraEscritaCanais getLeituraEscrita(){
        return SistemaControle.leituraEscritaCanais;
    }

}
