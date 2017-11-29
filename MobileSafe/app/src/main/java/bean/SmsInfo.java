package bean;


public class SmsInfo {
    private String address;//发送和接收的地址
    private int date ;//?
    private int read ;//?
    private int type ;//1 2 一个接收一个发送
    private String body ;//短信内容

    public SmsInfo(String address, int date, int read, int type, String body) {
        this.address = address;
        this.date = date;
        this.read = read;
        this.type = type;
        this.body = body;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public int getRead() {
        return read;
    }

    public void setRead(int read) {
        this.read = read;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
