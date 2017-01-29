/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

/**
 *
 * @author jeanmarioml
 */
public class Simulacao {
    
    private double nivelTanque1;
    private double nivelTanque2;
    
    private double nivelTanque1_prev;
    private double nivelTanque2_prev;
    
    private double tensao_prev = 0;
    
    public Simulacao(){
        this.nivelTanque1 = 0;
        this.nivelTanque2 = 0;
        this.nivelTanque1_prev = 0;
        this.nivelTanque2_prev = 0;
    }

    public double getNivelTanque1() {
        return nivelTanque1;
    }

    public double getNivelTanque2() {
        return nivelTanque2;
    }
    
    
    
    public void niveltank1Discreto(double v_prev){//, double l_prev){
        this.nivelTanque1_prev = this.nivelTanque1;
        this.nivelTanque1 = (0.99346*this.nivelTanque1_prev + 0.029545*this.tensao_prev);
        this.tensao_prev = v_prev;
       // return this.nivelTanque1;
    }
    
    public void niveltank2Discreto(){//double l2_prev, double l1_prev){
        this.nivelTanque2_prev = this.nivelTanque2;
        this.nivelTanque2 = 0.99346*this.nivelTanque2_prev + 0.0065397*this.nivelTanque1_prev;
       // return this.nivelTanque2;
    }
}
