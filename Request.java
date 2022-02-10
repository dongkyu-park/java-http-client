public class Request {
    private String header;
    private String body;

    Request() {
        this.header = "";
        this.body = "";
    }

    public void setHeader(String header) {
        this.header += header + "\r\n";
    }

    public String getRequestMessage() {
        return header + body;
    }
}
