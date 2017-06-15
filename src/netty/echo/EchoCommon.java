package netty.echo;


import java.io.Serializable;

/**
 * Created by Youga on 2017/6/15.
 */

public class EchoCommon implements Serializable {

    private static final long serialVersionUID = 5835296181685869605L;
    public long sumCountPackage;
    public int countPackage;
    public byte[] bytes;
    public String sendUid;
    public String receiveUid;
    public long sendTime;
    public Target target;


    public enum Target {

        SYSTEM("系统"), CLIENT("客户端"), SERVER("服务端"), HEART_BEAT("心跳包");

        String mDescribe;

        Target(String describe) {
            mDescribe = describe;
        }

        public String getDescribe() {
            return mDescribe;
        }
    }
}
