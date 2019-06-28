package oa;

/**
 * @author liyonghua@vv.cn
 * @version 1.0
 * @date 2019/6/28 18:47
 */
public class Fingerprint {
    /**
     * 指纹索引，编号0-9
     */
    private int index;
    /**
     * base64 编码的指纹数据
     */
    private String fingerprint;

    /**
     * 标示指纹模板是否有效或者是否为胁迫指纹;
     * 其具体表示为：0 表示指纹模板无效，1 表示指纹模板有效，3 表示胁迫指纹。
     */
    private int flag;

    public Fingerprint(int index, String fingerprint,int flag) {
        this.index = index;
        this.fingerprint = fingerprint;
        this.flag = flag;
    }

    public Fingerprint() {
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    @Override
    public String toString() {
        return "Fingerprint{" +
                "index=" + index +
                ", fingerprint='" + fingerprint + '\'' +
                '}';
    }
}
