/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

import static java.lang.Math.sqrt;

/**
 *
 * @author alexandre
 */
public class ObservadorDeEstados {
    
    private float G[][] = new float[2][2];
    private float H[] = new float[2];
    private float C[] = new float[2];
    private float Winv[][] = new float[2][2];
    private float I[][] = new float[2][2];
    private float q_l[][] = new float[2][2];
    private float L[] = new float[2];
    private float niveisObservados[] = new float[2];
    private float niveisObservados_prev[] = new float[2];
    
    //Polos
    private float p1 = 0, p2 = 0;
    //parte imaginaria polos
    private float img1 = 0;
    
    public ObservadorDeEstados(){
        //Iniciando G
        this.G[0][0] = (float) 0.9935;
        this.G[0][1] = 0;
        this.G[1][0] = (float) 0.006518;
        this.G[1][1] = (float) 0.9935;
        
        //Iniciando H
        this.H[0] = (float) 0.02955;
        this.H[1] = (float) 0.00009682;
        
        //Iniciando C
        this.C[0] = 0;
        this.C[1] = 1;
        
        //Iniciando I
        this.I[0][0] = 1;
        this.I[0][1] = 0;
        this.I[1][0] = 0;
        this.I[1][1] = 1;
        
        this.Winv[0][0] = (float) -152.4127;
        this.Winv[0][1] = (float) 153.416;
        this.Winv[1][0] = 1;
        this.Winv[1][1] = 0;
        
        this.niveisObservados_prev[0] = 0;
        this.niveisObservados_prev[1] = 0;
        
        this.niveisObservados[0] = 0;
        this.niveisObservados[1] = 0;
        
    }

    public void setP1(float p1) {
        this.p1 = p1;
    }

    public void setP2(float p2) {
        this.p2 = p2;
    }
    
    public void setImg1(float img1){
        this.img1 = img1;
    }
    
    public float getNivelObservado1(){
        return this.niveisObservados[0];
    }
    
    public float getNivelObservado2(){
        return this.niveisObservados[1];
    }
    
    public float getL1(){
        return this.L[0];
    }
    
    public float getL2(){
        return this.L[1];
    }
    
    public void setLs(float _l0, float _l1){
        L[0] = _l0;
        L[1] = _l1;
    }
    
    public void setQL(){
        q_l[0][0] = (G[0][0]-p1) * (G[0][0]-p2);
        q_l[0][1] = 0;
        q_l[1][0] = G[1][0]*(G[0][0]-p2) + G[1][0]*(G[1][1]-p1);
        q_l[1][1] = (G[1][1]-p1)*(G[1][1]-p2);
        
    }
    
    public void setL(){
        L[0] = Winv[0][1]*(G[0][0]*(G[0][0]-p1-p2) + (G[0][1]*G[1][0]) + (p1*p2) + (img1*img1));
        L[1] = Winv[0][1]*G[1][0]*(G[0][0] + G[1][1] - p1 - p2);
//        L[0] = q_l[0][0] * Winv[0][1] + q_l[0][1]*Winv[1][1];
//        L[1] = q_l[1][0] * Winv[0][1] + q_l[1][1]*Winv[1][1];
    }
    
    public void setNiveisObservados(double tensao, double nivel2_real){
        
        this.niveisObservados[0] = (float) (this.G[0][0]*this.niveisObservados_prev[0] + this.G[0][1]*this.niveisObservados_prev[1] + this.L[0]*(nivel2_real - this.niveisObservados_prev[1]) + this.H[0] * tensao);
        this.niveisObservados[1] = (float) (this.G[1][0]*this.niveisObservados_prev[0] + this.G[1][1]*this.niveisObservados_prev[1] + this.L[1]*(nivel2_real - this.niveisObservados_prev[1]) + this.H[1] * tensao);
        
        this.niveisObservados_prev[0] = this.niveisObservados[0];
        this.niveisObservados_prev[1] = this.niveisObservados[1];
    }
    
    public float[] calcularPolosObservador()
    {
        
        //Variáveis equação de segundo grau: a = 1.0
        float b = this.L[0]*this.C[0] + this.L[1]*this.C[1] - this.G[0][0] - this.G[1][1];
        float c = ((this.G[0][0] - this.L[0]*this.C[0]) * (this.G[1][1] - this.L[1]*this.C[1])) - ((this.G[0][1] - this.L[0]*this.C[1]) * (this.G[1][0] - this.L[1]*this.C[0]));
        
        double delta = b*b - 4.0*c;
        float polos[] = new float[4];
        
        if(delta >= 0)
        {
            polos[0] = (float) ((-b + sqrt(delta)) / 2.0);
            polos[1] = 0;
            polos[2] = (float) ((-b - sqrt(delta)) / 2.0);
            polos[3] = 0;
        }
        else
        {
            polos[0] = (float) (-b/2.0);
            polos[1] = (float) sqrt(-delta);
            polos[2] = (float) (-b / 2.0);
            polos[3] = (float) (-sqrt(-delta));
        }
        
        return polos;
    }
}
