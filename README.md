```java
public class Main {
    public static void main(String[] args) throws IOException {
        URL url = new URL("http://www.naver.com");
        Request request = initRequestToURL(url);

        Socket socket = new Socket(url.getHost(), 80);
        System.out.println("success?: " + socket.isConnected());

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
 }
```

- 응답 결과

![image](https://user-images.githubusercontent.com/81552729/153464654-75ae64e3-5337-4543-80cc-bbea47921be1.png)

위의 코드에서 의문이 시작됐다.
302 Response는 요청한 URL이 변경 되었으므로 Response header에 포함 된 Location의 URL로 다시 요청을 하라는 권고이다.
브라우저가 이 응답을 받았다면.. 스마트한 처리로 리다이렉트가 이루어 지겠지만, 이 Response status를 200으로 바꿔보고 싶었다.

```java
URL url = new URL("https://www.naver.com");
```

그래서 위와 같이 url 주소를 변경해서 요청 했으나, 같은 Response가 반환 되었다.

왜지?? 의문을 갖고 처음으로 생각한 것은,
실제 연결에 사용하는 것은 Socket 객체이고, Socket을 생성할 땐 url.getHost()로 가져 오므로 `www.naver.com`의 값은 고정이기에 URL을 아무리 바꿔봐야 의미가 없고
Socket을 생성할 때 `https://www.naver.com` 와 같은 형태로 프로토콜을 변경하여 소켓 연결을 해야 된다고 생각했다. 또 한참을 삽질했다.

하지만 Socket생성자에 HTTPS 프로토콜을 넣는 방법따윈 존재하지 않았다.
또, Socket 패키지는 기본적으로 TCP/IP 프로토콜이 default값으로 설정되어 있는데, 이 프로토콜을 변경하는 방법을 찾지 못했다.

HTTPS 와 HTTP의 차이가 무엇인지 정확히 알아야 겠다는 결론에 도달했고,
HTTPS가 HTTP 프로토콜에 SSL(Secure Sockets Layer) 기술을 추가하여 보안을 강화한 프로토콜을 뜻함을 알게됐다.

조금 더 찾아보니 Socket패키지를 확장하여 SSL을 적용한 SSLSocket 패키지가 존재 했다.

```java
public class Main {
    public static void main(String[] args) throws IOException {
        URL url = new URL("http://www.naver.com");
        Request request = initRequestToURL(url);

        SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), 443);
//        Socket socket = new Socket(url.getHost(), 80);

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
 }
```

- 응답 결과

![image](https://user-images.githubusercontent.com/81552729/153468343-25201c48-8269-4ba0-b45c-f3dd1f93752a.png)

![image](https://user-images.githubusercontent.com/81552729/153468407-bd611eb6-aa8b-4745-aa8b-9eb3507079eb.png)

리다이렉트 권고 없이 제대로 호출이 이루어짐을 확인할 수 있었다.
단순히 프로토콜 변경만 딸깍 하면 끝나버릴 것이라 생각하고 한참 방법을 찾았지만,

HTTPS 프로토콜은 HTTP 기반의 프로토콜에 SSL 기술이 추가된 것이고
이는 CA에서 인증서를 건네받고, 데이터를 암호화 하고 하는 등의 복잡한 작업들이 추가 되어야 하기에,
Socket클래스에 구현하지 않고 따로 분리해서 구현하지 않았을까 추정해본다.
