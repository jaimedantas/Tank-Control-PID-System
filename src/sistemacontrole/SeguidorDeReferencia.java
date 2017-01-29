/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sistemacontrole;

import Jama.Matrix;
import java.text.NumberFormat;

/**
 *
 * @author Wigow
 */
public class SeguidorDeReferencia {
    Matrix G_orig;
    Matrix G_aum;
    Matrix H_orig;
    Matrix H_aum;
    Matrix C;
    Matrix Wc;
    Matrix InvWc;
    Matrix Qc;
    Matrix V;
    Matrix InvV;
    Matrix K_aum;
    Matrix k2;
    double erro; //erro acumulado
    Double k1;
    int ordem;//ordem de G original
    int numPolos;
    double valorAtual;
    private ImagNumber[] polos;
    Matrix EstadosEstimados;
    
    public SeguidorDeReferencia(Matrix G_orig, Matrix H_orig, Matrix C) {
        this.G_orig = G_orig;
        this.H_orig = H_orig;
        this.ordem = this.G_orig.getRowDimension();
        this.H_aum = new Matrix(this.ordem+1, 1,0.0);
        this.H_aum.set(this.ordem, 0, 1.0);
        this.C = C;
        this.erro = 0.0;
        this.valorAtual = 0.0;
    }
    static public void Logger(String error){
        System.err.flush();
        System.err.println(error);
    }
    public void AumentaG() {
        if (G_orig==null || H_orig==null)
            Logger("Defina G e H válidos!");
        else {
            int ordG = 0;
            ordG = G_orig.getColumnDimension();
            this.G_aum = new Matrix(ordG+1, ordG+1, 0.0);
            for (int i = 0; i < ordG; i++){
                for (int j = 0; j < ordG; j++){
                    G_aum.set(i, j, G_orig.get(i, j));
                }
            }
            for (int i = 0; i < ordG; i++){
                for (int j = 0; j < 1; j++){
                    G_aum.set(i, ordG , H_orig.get(i, j));
                }
            }
        }    
    }
    public void CalculaWc()
    {
        double[][] wTemp = new double[this.ordem+1][this.ordem+1];
        double[][] wAux = new double[this.ordem+1][this.ordem+1];
        double[][] cTemp;
        double[][]gTemp;
        Matrix Aux1 = null;
        Matrix Aux2 = null;
        gTemp = this.G_aum.getArray();
        for(int i =0; i<= this.ordem; i++)
            wAux[i][0] = this.H_aum.get(i,0);
        System.arraycopy(wAux[0], 0, wTemp[0], 0, this.ordem+1);
        
        for(int i = 1; i<= this.ordem; i++)
        {
            Aux1 = new Matrix(Observador.powMatrix(gTemp, i));
            Aux2 = Aux1.times(this.H_aum);
            cTemp = Aux2.getArray();
            for(int j = 0 ; j <= this.ordem; j++)
                wAux[j][i] = cTemp[j][0];
        }
        this.Wc = new Matrix(wAux);
        this.InvWc =this.Wc.inverse();
    }
    
    public void CalcV()
    {
        double[][] a11 = new double[this.ordem][this.ordem ];
        double[][] a21 = new double[this.ordem][this.ordem];
        double[][] a22 = null;
        double[][] v = new double[this.ordem+1][this.ordem+1];
        Matrix op = null;
        Matrix eye;
        eye = new Matrix(Observador.Identidade(this.ordem));
        eye.timesEquals(-1.0);
        op = this.G_orig.plus(eye);
        a11 = op.getArray(); 
        op = this.C.times(this.G_orig);
        a21 = op.getArray();
        op = this.C.times(this.H_orig);
        a22 = op.getArray();
        for(int i = 0; i<this.ordem; i++)
        {
            for(int j = 0; j<this.ordem; j++)
            {
                v[i][j] = a11[i][j];
            }
            v[i][this.ordem] = this.H_orig.get(i,0);
            v[this.ordem][i] = a21[0][i];
        }
        v[this.ordem][this.ordem] = a22[0][0];
        this.V = new Matrix(v);
        this.InvV = this.V.inverse();
    }
    
    public void Qc()
    {
        double b;
        double c;
        double d;
        double[][] G3 = null;
        double[][] G2 = null;
        double[][] G1 = null;
        double[][] I = null;
        double[][] qcTemp = new double[this.ordem+1][this.ordem+1];
        if ((this.numPolos > 1) && (this.numPolos <= 3)) {
            if (this.numPolos == 2) {
                G2 = Observador.powMatrix(this.G_aum.getArray(), this.numPolos);
                I = Observador.Identidade(this.ordem+1);
                if (((this.polos[0].R() == this.polos[1].R()) && (Math.abs(this.polos[0].I()) == Math.abs(this.polos[1].I())))) {
                    b = -2 * this.polos[0].R();
                    c = this.polos[0].Mod() * this.polos[0].Mod();
                    d = 0.0;
                } else {
                    b = -(this.polos[0].R() + this.polos[1].R());
                    c = this.polos[0].R() * this.polos[1].R();
                    d = 0.0;
                }
                G1 = Observador.prodScalMatrix(this.G_aum, b);
                I = Observador.prodScalMatrix(new Matrix(I), c);
                for (int i = 0; i <= this.ordem; i++) {
                    for (int j = 0; j <= this.ordem; j++) {
                        qcTemp[i][j] = G2[i][j] + G1[i][j] + I[i][j];
                    }
                }
                this.Qc = new Matrix(qcTemp);
            } //Caso para três polos
            else {
                System.out.println("entrou");
                G3 = Observador.powMatrix(this.G_aum.getArray(), this.numPolos);
                G2 = Observador.powMatrix(this.G_aum.getArray(), this.numPolos - 1);
                I = Observador.Identidade(this.ordem+1);
                if (((this.polos[0].R() == this.polos[1].R()) && (Math.abs(this.polos[0].I()) == Math.abs(this.polos[1].I()))) || ((this.polos[1].R() == this.polos[2].R()) && (Math.abs(this.polos[1].I()) == Math.abs(this.polos[2].I())))) {
                    b = -(this.polos[this.numPolos - 1].R() + 2 * this.polos[0].R());
                    c = (this.polos[0].Mod() * this.polos[0].Mod()) + 2 * this.polos[0].R() * this.polos[this.numPolos - 1].R();
                    d = -this.polos[0].Mod() * this.polos[0].Mod();
                } else {
                    b = -(this.polos[0].R() + this.polos[1].R() + this.polos[2].R());
                    c = this.polos[2].R() * (this.polos[0].R() + this.polos[1].R()) + (this.polos[0].R() * this.polos[1].R());
                    d = -(this.polos[0].R() * this.polos[1].R() * this.polos[2].R());
                }
                G2 = Observador.prodScalMatrix(new Matrix(G2), b);
                G1 = Observador.prodScalMatrix(this.G_aum, c);
                I = Observador.prodScalMatrix(new Matrix(I), d);
                for (int i = 0; i <= this.ordem; i++) {
                    for (int j = 0; j <= this.ordem; j++) {
                        qcTemp[i][j] = G3[i][j] + G2[i][j] + G1[i][j] + I[i][j];
                    }
                }
                this.Qc = new Matrix(qcTemp);
            }
        } else {
            System.out.println("Quantidade de Polos Invalida!");
        }
    }
    
    public void CalcK()
    {
        Matrix linha = new Matrix(1, this.ordem+1, 0.0);
        Matrix aux = null;
        linha.set(0, this.ordem, 1.0);
        aux = linha.times(this.InvWc);
        this.K_aum = aux.times(this.Qc);
    }
    
    public void Ganhos()
    {
        Matrix linha = null;
        Matrix Aux = null;
        double[][] k2Aux = new double[1][this.H_orig.getRowDimension()];
        linha = this.K_aum;
        linha.set(0, this.ordem, this.K_aum.get(0, this.ordem)+1.0);
        Aux = linha.times(this.InvV);
        for(int i = 0; i < this.H_orig.getRowDimension(); i++)
        {
            k2Aux[0][i] = Aux.get(0, i);
        }
        this.k2 = new Matrix(k2Aux);
        this.k1 = Aux.get(0, this.ordem); 
    }
    
    public void Seguir(double L1, double L2, double r, double periodo)
    {
        Matrix EstadosAtuais = new Matrix(this.H_orig.getRowDimension(),1,0.0);
        Matrix aux = null;
        EstadosAtuais.set(0, 0, L1);
        EstadosAtuais.set(1, 0, (L2-1.0));
        if((this.k1 != null)&&(this.k2!=null))
        {
            //this.valorAtual = this.k1*(this.erro + (r - L2));
            aux = this.k2.times(EstadosAtuais);
            //this.valorAtual -= aux.get(0,0);
            this.erro += (r - L2);
            this.valorAtual = this.k1*(this.erro + (r -L2)) - aux.get(0,0);   
        }
        else
        {
            Logger("Valores dos Ganhos Nao Inicializados!");
        }
    }
    
    public Matrix getG_orig() {
        return G_orig;
    }

    public void setG_orig(Matrix G_orig) {
        this.G_orig = G_orig;
    }

    public Matrix getG_aum() {
        return G_aum;
    }

    public void setG_aum(Matrix G_aum) {
        this.G_aum = G_aum;
    }

    public Matrix getH_orig() {
        return H_orig;
    }

    public void setH_orig(Matrix H_orig) {
        this.H_orig = H_orig;
    }

    public Matrix getH_aum() {
        return H_aum;
    }

    public void setH_aum(Matrix H_aum) {
        this.H_aum = H_aum;
    }

    public Matrix getC() {
        return C;
    }

    public void setC(Matrix C) {
        this.C = C;
    }

    public Matrix getWc() {
        return Wc;
    }

    public void setWc(Matrix Wc) {
        this.Wc = Wc;
    }

    public Matrix getQc() {
        return Qc;
    }

    public void setQc(Matrix Qc) {
        this.Qc = Qc;
    }

    public Matrix getV() {
        return V;
    }

    public void setV(Matrix V) {
        this.V = V;
    }

    public Matrix getInvV() {
        return InvV;
    }

    public void setInvV(Matrix InvV) {
        this.InvV = InvV;
    }

    public Matrix getK_aum() {
        return K_aum;
    }
    
    public double getvalorAtual()
    {
        return this.valorAtual;
    }
    
    public void setK_aum(Matrix K_aum) {
        this.K_aum = K_aum;
    }

    public Matrix getK2() {
        return k2;
    }

    public void setK2(Matrix k2) {
        this.k2 = k2;
    }

    public double getErro() {
        return erro;
    }

    public Double getK1() {
        return k1;
    }

    public void setK1(Double k1) {
        this.k1 = k1;
        
    }
    
    public void setPolos(ImagNumber[] polos)
    {
        this.numPolos = polos.length;
        if(this.numPolos != (this.ordem+1))
            this.numPolos = this.ordem;
        this.polos = new ImagNumber[numPolos];
        System.arraycopy(polos, 0, this.polos, 0, this.numPolos);
    }
    
    public static void PrintMatrix(Matrix A)
    {
        int rows = A.getRowDimension();
        int cols = A.getColumnDimension();
        for(int i = 0; i< rows; i++)
        {
            for(int j = 0; j< cols; j++)
                System.out.print(" " + A.get(i, j));
            System.out.println();
        }
    }
    
    public double[] getGanhos(){
        double[] ganhos = new double[3]; 
        ganhos[0] = k1;
        ganhos[1] = k2.get(0,0);
        ganhos[2] = k2.get(0,1);
        return ganhos;
    }
            
public static void main(String args[]){
    double[][] GG = { {0.9993, 0}, {0.000657, 0.9993} };
    double[][] HH = {{0.0296}, {0.0001}};
    double[][] CC = {{0,1}};
    SeguidorDeReferencia seg = new SeguidorDeReferencia(new Matrix(GG), new Matrix(HH), new Matrix(CC)); 
    seg.AumentaG();
    //Seguidor.PrintMatrix(seg.G_aum);
    seg.CalculaWc();
    //Seguidor.PrintMatrix(seg.Wc);
    seg.CalcV();
    //Seguidor.PrintMatrix(seg.V);
    //Seguidor.PrintMatrix(seg.InvV);
    ImagNumber polo1 = new ImagNumber(0.9048, 0.0);
    ImagNumber polo2 = new ImagNumber(0.9920, 0.0);
    ImagNumber polo3 = new ImagNumber(0.9980, 0.0);
    ImagNumber[] polos = new ImagNumber[3];
    polos[0] = polo1;
    polos[1] = polo2;
    polos[2] = polo3;
    seg.setPolos(polos);
    seg.Qc();
    SeguidorDeReferencia.PrintMatrix(seg.Qc);
    //Seguidor.PrintMatrix(seg.InvV);
    seg.CalcK();
    System.out.println();
    SeguidorDeReferencia.PrintMatrix(seg.K_aum);
    seg.Ganhos();
    System.out.println();
    System.out.print("k2: ");
    SeguidorDeReferencia.PrintMatrix(seg.k2);
    System.out.println();
    System.out.println("k1: "+seg.k1);
}
    
    
}
