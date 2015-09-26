

public class MandelbrotSet {
    
    public static int test(Complex n, int loops) {
        Complex val = new Complex();
        int i = 0;
        for (; i < loops && !diverges(val); i++)
            val.square().add(n);
        return i == loops ? -1 : i;
    }
    
    private static boolean diverges(Complex n) {
        return n.getAbsSquared() > 4;
    }
}
