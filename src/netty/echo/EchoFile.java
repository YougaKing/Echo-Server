package netty.echo;

/**
 * Created by Youga on 2017/6/15.
 */

public class EchoFile extends EchoCommon {
    private static final long serialVersionUID = -7073394178771948754L;
    public String fileName;
    public long fileSize;

    public static EchoFile buildFile(long sumCountPackage, Target target) {
        EchoFile echo = new EchoFile();
        echo.sumCountPackage = sumCountPackage;
        echo.countPackage = 1;
        echo.sendTime = System.currentTimeMillis();
        echo.target = target;
        return echo;
    }
}
