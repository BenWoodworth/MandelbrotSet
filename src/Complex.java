
public class Complex {
    double a;
    double b;
    
    public Complex(double real, double imag) {
        this.a = real;
        this.b = imag;
    }
    
    public Complex() {
        this(0, 0);
    }
    
    public Complex(Complex n) {
        a = n.a;
        b = n.b;
    }
    
    public Complex add(Complex n) {
        a += n.a;
        b += n.b;
        return this;
    }
    
    public Complex subtract(Complex n) {
        a -= n.a;
        b -= n.b;
        return this;
    }
    
    public Complex multiply(Complex n) {
        double a0 = a;
        a = a * n.a - b * n.b;
        b = a0 * n.b + b * n.a;
        return this;
    }
    
    public Complex divide(Complex n) {
        double denom = n.a * n.a + n.b * n.b;
        double a0 = a;
        a = (a * n.a + b * n.b) / denom;
        b = (n.a * b - a0 * n.b) / denom;
        return this;
    }
    
    public Complex square() {
        double a0 = a;
        a = a * a - b * b;
        b = 2 * a0 * b;
        return this;
    }
    
    public double getAbsSquared() {
        return a * a + b * b;
    }
    
    public String toString() {
        return Double.valueOf(a) + " + " + Double.valueOf(b) + "i";
    }
}
