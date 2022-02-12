import sun.net.InetAddressCachePolicy;
import sun.net.www.http.HttpClient;
import sun.net.www.protocol.https.DelegateHttpsURLConnection;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;

public class Main {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://www.naver.com");
        Request request = initRequestToURL(url);

//        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
//        SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
        Socket socket = new Socket(url.getHost(), 443);
        System.out.println(socket.isConnected());

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintStream out = new PrintStream(socket.getOutputStream());
        out.println(request.getRequestMessage());

        while (true) {
            String line = in.readLine();

            if (line == null) {
                break;
            }
            System.out.println(line);
        }

        in.close();
        out.close();
        socket.close();
    }

//    public static void main(String[] args) {
//        URL url = new URL("https://www.naver.com");
//        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//
//        connection.setRequestMethod("GET");
//        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
//
//        int responseCode = connection.getResponseCode();
//
//        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//        StringBuffer stringBuffer = new StringBuffer();
//        String inputLine;
//
//        while ((inputLine = bufferedReader.readLine()) != null)  {
//            stringBuffer.append(inputLine);
//        }
//        bufferedReader.close();
//
//        String response = stringBuffer.toString();
//        System.out.println(response);
//    }

    public static Request initRequestToURL(URL url) {
        Request request = new Request();

        request.setHeader("GET / HTTP/1.1");
        request.setHeader("Host: " + url.getHost());
        request.setHeader("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.99 Safari/537.36");

        return request;
    }
}
