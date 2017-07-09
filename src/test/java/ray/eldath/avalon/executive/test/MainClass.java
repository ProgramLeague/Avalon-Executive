package ray.eldath.avalon.executive.test;

import org.eclipse.jetty.util.UrlEncoded;

public class MainClass {
    public static void main(String[] args) {
        String encode = UrlEncoded.encodeString("print(1+1)");
        System.out.println(encode);
        System.out.println(UrlEncoded.decodeString(encode));
    }
}
