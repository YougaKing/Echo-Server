package netty.echo;

/**
 * Created by Youga on 2017/6/15.
 */

public class EchoMessage extends EchoCommon {

    private static final long serialVersionUID = -6057744079088403732L;

    public static EchoMessage buildMessage(String message, Target target) {
        EchoMessage echo = new EchoMessage();
        byte[] bytes = message.getBytes(Constants.getCharset());
        echo.bytes = bytes;
        echo.sumCountPackage = bytes.length;
        echo.countPackage = 1;
        echo.sendTime = System.currentTimeMillis();
        echo.target = target;
        return echo;
    }

    public static EchoMessage buildMessage(byte[] bytes, Target target) {
        EchoMessage echo = new EchoMessage();
        echo.bytes = bytes;
        echo.sumCountPackage = bytes.length;
        echo.countPackage = 1;
        echo.sendTime = System.currentTimeMillis();
        echo.target = target;
        return echo;
    }

    public String getMessage() {
        return new String(bytes, Constants.getCharset());
    }
}
