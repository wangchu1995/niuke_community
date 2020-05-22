import java.io.IOException;


public class WkTests {
    public static String cmd1="F:\\webSoftware\\wkhtmltopdf\\bin\\wkhtmltoimage https://www.nowcoder.com F:\\webSoftware\\wkhtmltopdf\\data\\images\\";
    public static String cmd2="F:\\webSoftware\\wkhtmltopdf\\bin\\wkhtmltopdf https://www.nowcoder.com F:\\webSoftware\\wkhtmltopdf\\data\\pdfs\\";

    public static void main(String[] args) {
        try {
            Runtime.getRuntime().exec(cmd1+"2.png");
            System.out.println("ok");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
