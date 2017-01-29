/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author alexandre
 */
public class PID {
    
    private final FuncoesWindow funcoesWindow;
    private final LeituraEscritaCanais leituraEscrita;
    private final MainWindow mainWindow;
    
    //variáveis de controle
    private double kp, ki, kd, ti, td;
    
    //diz se foi setado kd ou td, e ki ou ti
    private boolean isKi, isKd;
    
    //variáveis de erro usadas no programa
    private double erro, erroSoma, erroDif, ultimoErro, erroDifD2;
    
    //ultimo valor registrado do sensor para ser usado no controlador PI-D
    private double ultimoValorSensor;
    
    //valores de cada parâmetro do alg de controle
    private double P, I, D, D2;
    
    //setPoint do controlador
    private double setPoint;
    private double setPointAnterior;
    
    //variável de analise
    private boolean runAnalise = false;
    
    //resultado final do sinal de controle
    private double sinal_calculado;
    
    //diz que tipo de controle foi selecionado
    private int tipoControle;
    
    //periodo de amostragem
    private double sampleTime;
    
    //anti windup variaveis
    private double saturacao;
    
    //PV do controlador
    private int PV;
    
    //variavel se e Mestre/Escravo
    private boolean isEscravo;
            
    PID(FuncoesWindow funcoesWindow, LeituraEscritaCanais leituraEscrita, MainWindow mainWindow){
        this.funcoesWindow = funcoesWindow;
        this.leituraEscrita = leituraEscrita;
        this.mainWindow = mainWindow;
        
        this.setPoint = 0;
        this.setPointAnterior = 0;
        
        this.erroSoma = 0;
        this.ultimoErro = 0;
        this.ultimoValorSensor = 0;
        
        this.sampleTime = 0.1;
        
        this.P = 0;
        this.I = 0;
        this.D = 0;
        this.D2 = 0;
        
    }
    
    public void calcularP(){
        this.P = this.kp * this.erro;        
    }
    
    public void calcularI(){
        if(this.isKi){
            //this.I = this.ki * this.erroSoma * this.sampleTime;
            /*
                para evitar sinais bruscos com mudança no valor de ki
                calcula-se o valor de I atraves de ki*erroAtual + valorAntigoDeI
            */
            this.I += this.ki * this.erro * this.sampleTime; 
        }
        else{
            if(this.ti == 0){
                this.I = 0;
            }
            else{
                //this.I = (this.kp/this.ti)*this.erroSoma * this.sampleTime;
                this.I += (this.kp/this.ti)*this.erro * this.sampleTime;
            }
        }
    }
    
    public void calcularD(){
        if(this.isKd){
            this.D = (this.kd * this.erroDif)/this.sampleTime;
        }
        else{
            this.D = (this.kp*this.td)*this.erroDif/this.sampleTime;
        }
    }
    
    public void calcularD2(){
        if(this.isKd){
            this.D = this.kd * this.erroDifD2/this.sampleTime;
        }
        else{
            this.D = (this.kp*this.td)*this.erroDifD2/this.sampleTime;
        }
    }
    
    public void calcularErros(){
        double leituraCanal = this.leituraEscrita.getCanalLeitura(this.PV);
        this.erro = this.setPoint - leituraCanal;
        //System.out.println(this.erro + " "+leituraCanal);
        this.erroSoma += this.erro;
        this.erroDif = this.erro - this.ultimoErro;
        this.erroDifD2 = leituraCanal - this.ultimoValorSensor;
        
        this.ultimoValorSensor = leituraCanal;
        this.ultimoErro = this.erro;
    }
    
    public void calcularPID(){
        calcularP();
        calcularI();
        calcularD();
        calcularD2();
    }
    
    public double getValorCalculado(){
        calcularErros();
        calcularPID();
        
        //aplicar filtros
        //se o windup estiver selecionado
        if(this.funcoesWindow.isAntiWindUpActive()){
            runAntiWindUp();
        }
        //se o filtro derivativo estiver selecionado
        if(this.funcoesWindow.isDerivativoAtivado()){
            runFiltroDerivativo();
        }
        
        //aplicar controle
        switch(this.tipoControle){
            case 0://controle tipo P
                this.sinal_calculado = this.P;
                break;
            case 1://controle tipo PI
                this.sinal_calculado = this.P + this.I;
                break;
            case 2://controle tipo PD
                this.sinal_calculado = this.P + this.D;
                break;
            case 3://controle tipo PID
                this.sinal_calculado = this.P + this.I + this.D;
                break;
            case 4://controle tipo PI-D
                this.sinal_calculado = this.P + this.I + this.D2;
                break;
            default:
                break;
        }
        return this.sinal_calculado;
    }

    //setar o tipo de controle utilizado
    public void setTipoControle(int tipoControle){
        this.tipoControle = tipoControle;
        
    }
    
    //setar os valores de kp, ki (ou ti) e kd (ou td)
    public void setPIDParametros(double kp, double i, boolean isKi, double d, boolean isKd){
        this.kp = kp;
        this.isKi = isKi;
        this.isKd = isKd;
        
        if(isKi){
            this.ki = i;
        }
        else{
            this.ti = i;
        }
        
        if(isKd){
            this.kd = d;
        }
        else{
            this.td = d;
        }
    }
    
    public void setEscravo(boolean isEscravo){
        this.isEscravo = isEscravo;
    }
    
    public void setPV(int PV){
        this.PV = PV;
    }
    
    //aplica o setpoint do controle
    public void setSetPoint(double setPoint, boolean newRun){
        if(setPoint != this.setPoint){
            runAnalise = false;
            this.setPointAnterior = this.setPoint;
            this.setPoint = setPoint;
            //calcular analise do sistema para novo valor de set point
            if(newRun && !this.isEscravo){
                runAnalise = true;
                mainWindow.addNewRow();
                //adicionar ao gráfico os valores de Kp, Ki, Kd, Ti e Td do controlador atual
                mainWindow.setValueTable("Kp", String.valueOf(this.kp));
                switch(this.tipoControle){
                    case 1://controle tipo PI
                        //if(this.isKi){
                            mainWindow.setValueTable("Control", funcoesWindow.tipoControleComboBox.getSelectedItem().toString());
                            mainWindow.setValueTable("Ki", funcoesWindow.kiTextField.getText());
                            //mainWindow.setValoresTanque1("Ki", this.ki);
//                        }
//                        else{
                            mainWindow.setValueTable("Ti", funcoesWindow.tiTextField.getText());
                            
//                        }
                        break;
                    case 2://controle tipo PD
//                        if(this.isKd){
                            mainWindow.setValueTable("Control", funcoesWindow.tipoControleComboBox.getSelectedItem().toString());

                            mainWindow.setValueTable("Kd", funcoesWindow.kdTextField.getText());
//                        }
//                        else{
                            mainWindow.setValueTable("Td", funcoesWindow.tdTextField.getText());
//                        }
                        break;
                    case 3://controle tipo PID
//                        if(this.isKi){
                            mainWindow.setValueTable("Control", funcoesWindow.tipoControleComboBox.getSelectedItem().toString());
                            mainWindow.setValueTable("Ki", funcoesWindow.kiTextField.getText());
//                        }
//                        else{
                            mainWindow.setValueTable("Ti", funcoesWindow.tiTextField.getText());
//                        }
//                        if(this.isKd){
                            mainWindow.setValueTable("Kd", funcoesWindow.kdTextField.getText());
//                        }
//                        else{
                            mainWindow.setValueTable("Td", funcoesWindow.tdTextField.getText());
//                        }
                        break;
                    case 4://controle tipo PI-D
//                        if(this.isKi){
                            mainWindow.setValueTable("Control", funcoesWindow.tipoControleComboBox.getSelectedItem().toString());
                            mainWindow.setValueTable("Ki", funcoesWindow.kiTextField.getText());
//                        }
//                        else{
                            mainWindow.setValueTable("Ti", funcoesWindow.tiTextField.getText());
//                        }
//                        if(this.isKd){
                            mainWindow.setValueTable("Kd", funcoesWindow.kdTextField.getText());
//                        }
//                        else{
                            mainWindow.setValueTable("Td", funcoesWindow.tdTextField.getText());
//                        }
                        break;
                    default:
                        break;
                }
                runAnaliseSistema();
            }
        }
    }
    
    //retorna o sample time do controle
    public void setSampleTime(double sampleTime){
        this.sampleTime = sampleTime;
    }
    
    //aplicar filtro wind up ao sinal de controle
    public void runAntiWindUp(){
        
        if(this.I > 4){
            this.I = 4;
        }
        else if(this.I < -4){
            this.I = -4;
        }
    }
    
    //aplicar filtro derivativo
    public void runFiltroDerivativo(){
        double gama = 0.1;
        switch(this.tipoControle){
            case 3:
                this.D = this.D/(1 + gama*(this.D/(this.kp*this.erro)));
                break;
            default:
                this.D = this.D/(1 + (gama*this.D));
                break;
        }
    }
    
    //retorna o valor do sensor atualmente selecionado
    public double getSensorValor(){
        return this.leituraEscrita.getCanalLeitura(this.funcoesWindow.getPV());
    }
    
    //calcular valores de sobressinal, tempo de subida(90%, 95% e 100%) 
    //tempo de pico, tempo de acomodação(2%, 5% e 10%)
    public void runAnaliseSistema(){
        Thread t = new Thread(new AnaliseSistema());
        while(t.isAlive()){
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                System.out.println("erro funcao runAnaliseSistema");
            }
        }
        t.start();
    }
    
    //classe que mede os tempos do sistema
    public class AnaliseSistema extends Thread{
        @Override
        public void run(){
            String _setPoint = setPointAnterior + "cm - " + setPoint + "cm";
            mainWindow.setValueTable("SetPoint", _setPoint);
            long startTime = System.currentTimeMillis();//inicio de medição
            long Tpico = 0, Ts2 = 0, Ts5 = 0, Ts10 = 0, Tr100 = 0, Tr95 = 0, Tr90 = 0,
                 startTr95 = 0, startTr90 = 0;
            double Mpcm = 0, Mp_porcentagem = 0;
            boolean endTpico=false, endTs2=false, endTs5=false, endTs10=false, 
                    endTr100=false, endTr95=false, endTr90=false, endMp=false,
                    startedTimeTr95=false, startedTimeTr90=false;
            double valorSensor;
            double set = (setPoint - setPointAnterior);
            
            while(runAnalise){
                try {
                    valorSensor = getSensorValor();
                    
                    //calculo de 2% de acomodação
                    if(valorSensor <= setPoint*1.02 && valorSensor >= setPoint*0.98){
                        if(!endTs2){
                            Ts2 = System.currentTimeMillis() - startTime;
                            String value = String.format("%.2fs",(double)Ts2/1000);
                            mainWindow.setValueTable("Ts2", value);
                            endTs2 = true;
                        }
                    }
                    else{
                        mainWindow.setValueTable("Ts2", "");
                        endTs2 = false;
                    }

                    //calculo de 5% de acomodação
                    if(valorSensor <= setPoint*1.05 && valorSensor >= setPoint*0.95){
                        if(!endTs5){
                            Ts5 = System.currentTimeMillis() - startTime;
                            String value = String.format("%.2fs",(double)Ts5/1000);
                            mainWindow.setValueTable("Ts5", value);
                            endTs5 = true;
                        }
                    }
                    else{
                        mainWindow.setValueTable("Ts5", "");
                        endTs5 = false;
                    }

                    //calculo de 10% de acomodação
                    if(valorSensor <= setPoint*1.1 && valorSensor >= setPoint*0.9){
                        if(!endTs10){
                            Ts10 = System.currentTimeMillis() - startTime;
                            String value = String.format("%.2fs",(double)Ts10/1000);
                            mainWindow.setValueTable("Ts10", value);
                            endTs10 = true;
                        }
                    }
                    else{
                        mainWindow.setValueTable("Ts10", "");
                        endTs10 = false;
                    }
                    
                    //inicio de contagem para 10-90%
                    if(((valorSensor >= ((set)*0.1)+setPointAnterior && set > 0) ||
                        (valorSensor <= setPointAnterior-(Math.abs(set)*0.1) && set < 0)) && 
                        !startedTimeTr90){

                        startTr90 = System.currentTimeMillis();
                        startedTimeTr90 = true;
                    }
                    
                    //inicio de contagem para 5-95%
                    if(((valorSensor >= ((set)*0.05)+setPointAnterior && set > 0) ||
                        (valorSensor <= setPointAnterior-(Math.abs(set)*0.05) && set < 0)) &&
                        !startedTimeTr95){

                        startTr95 = System.currentTimeMillis();
                        startedTimeTr95 = true;
                    }
                    
                    //final de contagem para 5-95%
                    if(((valorSensor >= ((set)*0.95)+setPointAnterior && set > 0) || 
                        (valorSensor <= setPointAnterior-(Math.abs(set)*0.95) && set < 0)) && 
                        !endTr95){

                        Tr95 = System.currentTimeMillis() - startTr95;
                        String value = String.format("%.2fs",(double)Tr95/1000);
                        mainWindow.setValueTable("Tr95", value);
                        endTr95 = true;
                    }
                    
                    //final de contagem para 10-90%
                    if(((valorSensor >= ((set)*0.90)+setPointAnterior && set > 0) ||
                        (valorSensor <= setPointAnterior-(Math.abs(set)*0.9) && set < 0)) &&
                        !endTr90){

                        Tr90 = System.currentTimeMillis() - startTr90;
                        String value = String.format("%.2fs",(double)Tr90/1000);
                        mainWindow.setValueTable("Tr90", value);
                        endTr90 = true;
                    }
                    
                    //condição caso o nível passe do valor do setPoint para baixo (undershoot) ou para cima (overshoot)
                    if((valorSensor >= setPoint && set > 0) || (valorSensor <= setPoint && set < 0)){//overshoot
                        
                        //final de contagem para 0-100%
                        if(!endTr100){
                            Tr100 = System.currentTimeMillis() - startTime;//tempo de subida 0-100%
                            String value = String.format("%.2fs",(double)Tr100/1000);
                            mainWindow.setValueTable("Tr100", value);
                            endTr100 = true;
                        }
                        
                        //calculo de sobressinal e de tempo de pico
                        if((valorSensor - setPoint >= Mpcm && set > 0) ||
                            (setPoint - valorSensor >= Mpcm && set < 0) ){
                            
                            Tpico = System.currentTimeMillis() - startTime;//tempo do pico
                            String value = String.format("%.2fs",(double)Tpico/1000);
                            mainWindow.setValueTable("Tpico", value);

                            Mpcm = Math.abs(valorSensor - setPoint);
                            Mp_porcentagem = (Mpcm/setPoint)*100;
                            value = String.format("%.2f%% - %.2fcm", Mp_porcentagem, Mpcm);
                            mainWindow.setValueTable("Mp", value);
                        }
                    }
                    Thread.sleep(75);
                } catch (Exception ex) {
                    System.out.println("Erro durante analise de sistema: "+ex);
                }
            }
        }
    }
}
