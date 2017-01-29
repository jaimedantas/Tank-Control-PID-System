/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

import Jama.Matrix;

/**
 *
 * @author Welisson
 */
public class Observador {
    private Matrix G;
    private Matrix H;
    private Matrix C;
    private Matrix L;
    private Matrix Wo;
    private Matrix InvWo;
    private Matrix qc;
    //private Matrix X;
    private double[][] EstadoEstimadoAtual;
    private double[][] ProxEstadoEstimado;
    private double[][] SaidaEstimada;
    private double[][] ErroEstimacao;
    private double[][]K;
    private int ordem;
    private int numPolos;
    private int rank_G;
    private boolean obs;
    private ImagNumber[] polos;
    
    public Observador (Double[][]G, Double[][]H, Double[][]C, int ordem)
            
    {
        this.G = new Matrix(Observador.DoubleArrayTodoubleArray(G, ordem, ordem));
        this.H = new Matrix(Observador.DoubleArrayTodoubleArray(H, ordem, 1));
        this.C = new Matrix(Observador.DoubleArrayTodoubleArray(C, 1, ordem));
        this.ordem = this.G.getColumnDimension();
        this.rank_G = this.G.rank();
        this.polos = null;
        this.numPolos = 0;
        this.K = null;
        this.ErroEstimacao = new double[2][1];
        this.EstadoEstimadoAtual = new double[2][1];
        this.ProxEstadoEstimado = new double[2][1];
        this.SaidaEstimada = new double[2][1];
    }
    
    public Observador(double[][]G, double[][]H, double[][]C)
    {
        this.G = new Matrix(G);
        this.H = new Matrix(H);
        this.C = new Matrix(C);
        this.ordem = this.G.getColumnDimension();
        this.rank_G = this.G.rank();
        this.polos = null;
        this.numPolos = 0;
        this.K = null;
        this.ErroEstimacao = new double[2][1];
        this.EstadoEstimadoAtual = new double[2][1];
        this.ProxEstadoEstimado = new double[2][1];
        this.SaidaEstimada = new double[2][1];
    }
    public Matrix getWo()
    {
        double[][] wTemp = new double[this.ordem][this.ordem];
        double[][] wAux = null;
        Matrix cgTemp;
        double[][]cTemp;
        double[][]gTemp;
        if(this.Wo == null)
        {
            wAux = this.C.getArray();
            System.arraycopy(wAux[0], 0, wTemp[0], 0, this.ordem);
            cTemp = this.C.getArray();
            gTemp = this.G.getArray();
            for(int rows = 1; rows < this.ordem; rows++ )
            {
                wAux = Observador.multMatrix(this.C, new Matrix(gTemp));
                System.arraycopy(wAux[0], 0, wTemp[rows], 0, this.ordem);
                cgTemp = new Matrix(gTemp);
                gTemp = Observador.multMatrix(cgTemp, this.G);
            }
            this.Wo = new Matrix(wTemp);
            if(this.Wo.det()!=0.0)
                this.InvWo = this.Wo.inverse();
            else
            {
                this.obs = false;
                System.out.println("O sistema Nao eh Observavel!");
            }
        }
        return this.Wo;
    }
    public void setPolos(ImagNumber[] polos)
    {
        this.numPolos = polos.length;
        if(this.numPolos != this.ordem)
            this.numPolos = this.ordem;
        this.polos = new ImagNumber[numPolos];
        System.arraycopy(polos, 0, this.polos, 0, this.numPolos);
    }
    public Matrix getQc()
    {
        double b;
        double c;
        double d;
        double[][] G3 = null;
        double[][] G2 = null;
        double[][] G1 = null;
        double[][] I = null;
        double[][] qcTemp = new double[this.ordem][this.ordem];
        if((this.numPolos > 1)&&(this.numPolos<=3))
        {
            if(this.numPolos == 2)
            {
                G2 = Observador.powMatrix(this.G.getArray(), this.numPolos);
                I = Observador.Identidade(this.ordem);
                if(((this.polos[0].R()==this.polos[1].R())&&(Math.abs(this.polos[0].I())==Math.abs(this.polos[1].I()))))
                {
                    b = -2 * this.polos[0].R();
                    c = this.polos[0].Mod() * this.polos[0].Mod();
                    d = 0.0;
                }
                else
                {
                    b = -(this.polos[0].R() + this.polos[1].R());
                    c = this.polos[0].R() * this.polos[1].R();
                    d = 0.0;
                }
                G1 = Observador.prodScalMatrix(this.G, b);
                I = Observador.prodScalMatrix(new Matrix(I), c);
                for (int i = 0; i < this.ordem; i++) {
                    for (int j = 0; j < this.ordem; j++) {
                        qcTemp[i][j] = G2[i][j] + G1[i][j] + I[i][j];
                    }
                }
                this.qc = new Matrix(qcTemp);
            }
            //Caso para três polos
            else
            {
                G3 = Observador.powMatrix(this.G.getArray(), this.numPolos);
                G2 = Observador.powMatrix(this.G.getArray(), this.numPolos - 1);
                I = Observador.Identidade(this.ordem);
                if(((this.polos[0].R()==this.polos[1].R())&&(Math.abs(this.polos[0].I())==Math.abs(this.polos[1].I())))||(
                        (this.polos[1].R()==this.polos[2].R())&&(Math.abs(this.polos[1].I())==Math.abs(this.polos[2].I()))))
                {
                    b = -(this.polos[this.numPolos-1].R()+2*this.polos[0].R());
                    c = (this.polos[0].Mod()*this.polos[0].Mod())+ 2*this.polos[0].R()*this.polos[this.numPolos-1].R();
                    d = -this.polos[0].Mod()*this.polos[0].Mod();
                }
                else
                {
                    b = -(this.polos[0].R()+this.polos[1].R()+this.polos[2].R());
                    c = this.polos[2].R()*(this.polos[0].R()+this.polos[1].R()) + (this.polos[0].R()*this.polos[1].R());
                    d = -(this.polos[0].R()*this.polos[1].R()*this.polos[2].R());
                }
                G2 = Observador.prodScalMatrix(new Matrix(G2), b);
                G1 = Observador.prodScalMatrix(new Matrix(G1), c);
                I = Observador.prodScalMatrix(new Matrix(I), d);
                for (int i = 0; i < this.ordem; i++) {
                    for (int j = 0; j < this.ordem; j++) {
                        qcTemp[i][j] = G3[i][j] + G2[i][j] + G1[i][j] + I[i][j];
                    }
                }
                this.qc = new Matrix(qcTemp);
            }
        }
        else
        {
            System.out.println("Quantidade de Polos Invalida!");
        }
        return this.qc;
    }
    
    public Matrix getL()
    {
        double[][] Aux = null;
        double[][] lTemp = null;
        double[][] zz =  {{0},{1}};
        if((this.InvWo != null)&&(this.qc!=null))
        {
           Aux = Observador.multMatrix(this.qc, this.InvWo);
           lTemp = new double[this.ordem][1];
           //lTemp = Observador.multMatrix(new Matrix(Aux),new Matrix(zz));
           for(int i = 0; i < this.ordem; i++)
               lTemp[i][0] = Aux[i][this.ordem-1];
           this.L = new Matrix(lTemp);
        }
        else
        {
            System.out.println("Falta Calcular Matrizes!");
        }
        return this.L;
    }
    public double[][] getK()
    {
        double[][] Aux = null;
        double[][] kTemp = null;
        if((this.InvWo != null)&&(this.qc!=null))
        {
           Aux = Observador.multMatrix(this.InvWo, this.qc); 
           kTemp = new double[1][this.ordem];
           for(int i = 0; i < this.ordem; i++)
               kTemp[0][i] = -Aux[this.ordem-1][i];
           this.K = kTemp;
        }
        return this.K;
    }
    
    public ImagNumber[] calcPolos(double[][] L)
    {
        
        //ImagNumber[] polos = new ImagNumber[this.ordem];
        this.polos = this.baskhara();
        return polos;
    }
    
    public static void PrintMatrix(Matrix A)
    {
        for(int i = 0; i<A.getRowDimension(); i++)
        {
            for(int j = 0; j< A.getColumnDimension(); j++)
            {
                System.out.print(A.get(i, j)+" ");
            }
            System.out.println("");
        }
    }
    
    public static void PrintMatrix(Number[][] A, int numLin, int numCols)
    {
        int size = A.length;
        int rows = numLin;
        int cols = numCols;
        for(int i = 0; i<rows; i++)
        {
            for(int j = 0; j<cols; j++)
            {
                System.out.print(A[i][j]+" ");
            }
            System.out.println("");
        }
    }
    
    public static void PrintMatrix(double[][] A, int numLin, int numCols)
    {
        int rows = numLin;
        int cols = numCols;
        for(int i = 0; i<rows; i++)
        {
            for(int j = 0; j<cols; j++)
            {
                System.out.print(A[i][j]+" ");
            }
            System.out.println("");
        }
    }
    
    public static double[][] multMatrix(Matrix A, Matrix B)
    {
        double[][] multResult = null;
        double[][] aTemp = null;
        double[][] bTemp = null;
        int rows = 0;
        int cols = 0;
        if(A.getColumnDimension()==B.getRowDimension())
        {
            rows = A.getRowDimension();
            cols = B.getColumnDimension();
            multResult = new double[rows][cols];
            aTemp = A.getArray();
            bTemp = B.getArray();
            for(int k = 0; k<cols; k++ )
            {
                for(int i = 0; i< rows; i++)
                {
                    for(int j = 0; j< cols; j++)
                    {
                        multResult[i][k] += aTemp[i][j]*bTemp[j][k];
                    }
                }
            }
        }
        else{
            System.out.println("Dimensoes das Matrizes Incompativeis!");
        }
        return multResult;
    }
    
    public static double[][] prodScalMatrix(Matrix A, double Scalar)
    {
        int rows = A.getRowDimension();
        int cols = A.getColumnDimension();
        double[][] result = new double[rows][cols];
        for(int i = 0; i< rows; i++)
        {
            for(int j = 0; j< cols; j++)
            {
                result[i][j] = A.get(i, j)*Scalar;
            }
        }
        return result;
    }
    
    public static double[][] powMatrix(double[][] A, int pot)
    {
        double[][] result = null;
        Matrix powA = new Matrix(A);
        Matrix Ao = new Matrix(A);
        if(pot == 1)
            return A;
        for(int i = 1; i< pot; i++)
        {
            result = multMatrix(powA, Ao);
            powA = null;
            powA = new Matrix(result);
        }
        return result;
    }
    
    public static double[][] Identidade(int ordem)
    {
        double[][] result = new double[ordem][ordem];
        for(int i = 0; i< ordem;i++)
            result[i][i] = 1;
        return result;
    }
    
    public static double[][] DoubleArrayTodoubleArray(Double[][] array, int rows, int cols)
    {
        double[][] arrayDouble = new double[rows][cols];
        for(int i = 0; i< rows; i++)
        {
            for(int j = 0; j< cols; j++)
                arrayDouble[i][j] = array[i][j].doubleValue();
        }
        return arrayDouble;
    }
    
    public static void InicializaArray(Double[][]array, int rows, int cols)
    {
        for(int i = 0; i< rows; i++)
        {
            for(int j = 0; j< cols; j++)
            {
                if(array[i][j] == null)
                    array[i][j] = 0.0;
            }
        }
    }
    
    public ImagNumber[] baskhara()
    {
        ImagNumber polo1 = new ImagNumber(0.0,0.0);
        ImagNumber polo2 = new ImagNumber(0.0,0.0);
        ImagNumber[] polos = new ImagNumber[this.ordem];
        double a = 1.0;
        double b = 0.0;
        double c = 0.0;
        double L1 = this.L.get(0,0);
        double L2 = this.L.get(1,0);
        double C1 = this.C.get(0,0);
        double C2 = this.C.get(0,1);
        double G11 = this.G.get(0,0);
        double G12 = this.G.get(0,1);
        double G21 = this.G.get(1,0);
        double G22 = this.G.get(1,1);
        
        double Z1 = 0.0;
        double Z2 = 0.0;
        double Img1 = 0.0;
        double Img2 = 0.0;
        b = L2*C2 + L1*C1 - (G22+G11);
        c = (G11*G22 + (L1*C1*L2*C2) +(G21*L1*C2) + (G12*L2*C1)) - ((G11*L2*C2) + (G22*L1*C1) + (G21*G12) + (L1*C2*L2*C1));
        
        double delta = b*b - (4*a*c);
        if(delta > 0)
        {
            Z1 = (-b + Math.sqrt(delta))/2.0;
            Z2 = (-b - Math.sqrt(delta))/2.0;
        }
        else if(delta == 0.0)
        {
            Z1 = (-b + Math.sqrt(delta))/2.0;
            Z2 = Z1;
        }
        else
        {
            delta = -delta;
            Img1= (Math.sqrt(delta))/2.0;
            Img2 = -Img1;
            Z1 = -(b/2.0);
            Z2 = Z1;
        }
        polo1.setNumber(Z1,Img1);
        polo2.setNumber(Z2,Img2);
        polos[0] = polo1;
        polos[1] = polo2;
        return polos;
    }
    
    public void Estimar(double SinalEntrada, double[][] EstadosReais)
    {
        this.EstadoEstimadoAtual = this.ProxEstadoEstimado;
        Matrix MErroEstimacao;
        Matrix MestadoReal = new Matrix(EstadosReais);
        Matrix X = new Matrix(this.EstadoEstimadoAtual);
        MErroEstimacao =  MestadoReal.minus(X);
        Matrix MatrixXProx;
        Matrix LYy;
        Matrix Hu;
        Matrix aux;
        Matrix Saida;
        //MatrixXProx = new Matrix(Observador.multMatrix(this.G, X));
        MatrixXProx = this.G.times(X);
        //LYy = new Matrix(Observador.prodScalMatrix(this.L, (SaidaAtual - this.SaidaEstimada)));
        LYy = this.L.times(MErroEstimacao.getArray()[1][0]);
        Hu =this.H.times(SinalEntrada*5.0);
        aux = MatrixXProx.plus(LYy);
        MatrixXProx = aux.plus(Hu);
        Saida = this.C.times(X);
        //this.SaidaEstimada = ;
        this.ProxEstadoEstimado = MatrixXProx.getArray();
        this.ErroEstimacao = MErroEstimacao.getArray();
    }
    public double[][] getEstadoEstimado()
    {
        return this.ProxEstadoEstimado;
    }
    public double[][] getErroEstimacao()
    {
        return this.ErroEstimacao;
    }
    public Matrix getInvWo()
    {
        return this.InvWo;
    }
    public void setL(double[][]L)
    {
        this.L = new Matrix(L);
    }
     public void setL(Matrix L)
    {
        this.L = new Matrix(L.getArray());
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        double[][] a = {{0.9993,0},{0.000657,0.9993}};
        double[][] b = {{0.0296},{0.0001}};
        double[][]c = {{0,1}};
        //double[][] teste = {{1,2},{3,4}};
        ImagNumber[] polosPorL = new ImagNumber[2];
        Matrix A = new Matrix(a);
        Matrix B = new Matrix(b);
        Matrix C = new Matrix(c);
        Matrix Wo = null;
        Matrix Qc = null;
        Matrix Li = null;
        Matrix Ki = null;
        //Matrix Teste = new Matrix(teste);
        Matrix Result;
        //ImagNumber p1 = new ImagNumber(0.3, 0.0);
        //ImagNumber p2 = new ImagNumber(0.9, 0.0);
        ImagNumber[] polos;
        //polos[0] = p1;
        //polos[1] = p2;
        Observador obs = new Observador(a,b,c);
        //Wo = obs.getWo();
        //obs.setPolos(polos);
        //Qc = obs.getQc();
        //Observador.PrintMatrix(Wo);
        //Result = new Matrix(Observador.prodScalMatrix(Teste, 2.0));
        //Observador.PrintMatrix(Result);
  //      System.out.println("Matriz qc: ");
//        Observador.PrintMatrix(Qc);
    //    System.out.println("Wo");
      //  Observador.PrintMatrix(Wo);
        //System.out.println("InvWo");
        //Observador.PrintMatrix(obs.getInvWo());
        //----------------------------------------------
        double[][] L = new double[2][1];
        L[0][0] = 2000.0;
        L[1][0] = 0.5;
        obs.setL(L);
        //polos = obs.baskhara();
        Li = obs.getL();
        //polos[0].Print();
        //polos[1].Print();
        //----------------------------------------------
        System.out.println("Matriz L: ");
        Observador.PrintMatrix(Li);
//        Ki = new Matrix(obs.getK());
        //System.out.println("Matriz K: ");
//        Observador.PrintMatrix(Ki);
        polosPorL = obs.baskhara();
        for(int i =0; i< 2; i++)
        {
            System.out.print("polo"+(i+1)+": ");
            polosPorL[i].Print();
        }
    }
    
}