import java.util.Random;

public class RandomTest {

    public static void main(String[] args) {
        Random random = new Random();
//        double[] arr = {20.5000,-12.4486,0.6030,13.1739,-15.5657,26.8099,-7.5264,-27.5342};
//
//        for(double d:arr){
//            d = random.nextDouble()*5+d;
//            System.out.println(d);
//        }

        for (int i = 0; i < 6; i++) {
            double d = random.nextDouble()*13;
            System.out.println(d);
        }

    }
}
