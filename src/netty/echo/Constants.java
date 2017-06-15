package netty.echo;

import java.nio.charset.Charset;

/**
 * Created by Youga on 2017/6/15.
 */

public class Constants {
    public static final String ENCODING = "UTF-8";

    public Constants() {
    }

    public static Charset getCharset() {
        return Charset.forName("UTF-8");
    }
}
