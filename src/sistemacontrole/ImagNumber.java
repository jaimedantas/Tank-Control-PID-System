/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sistemacontrole;

/**
 *
 * @author Alexandre
 */
public class ImagNumber {
    private double real;
    private double imaginary;
    private double modulo;
    private double fase;
    
    public ImagNumber()
    {
        this(0.0, 0.0);
    }
    public ImagNumber(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
        this.modulo = Math.sqrt(real*real + imaginary*imaginary);
        this.fase = Math.atan2(imaginary, real);
    }
    public void setNumber(double real, double imaginary)
    {
        this.real = real;
        this.imaginary = imaginary;
        this.modulo = Math.sqrt(real*real + imaginary*imaginary);
        this.fase = Math.atan2(imaginary, real);
    }
    
    public double R()
    {
        return this.real;
    }
    public double I()
    {
        return this.imaginary;
    }
    public double Mod()
    {
        return this.modulo;
    }
    public double Fas()
    {
        return this.fase;
    }
    public void Print()
    {
        if(this.imaginary != 0.0)
            System.out.println(this.real+" j"+this.imaginary);
        else
            System.out.println(this.real);
    }
}