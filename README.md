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

---

# 잘못 된 내용 바로 잡기


Socket 패키지는 TCP/IP 프로토콜이 default 설정으로 잡혀 있습니다.

민지노가 보내주신 주요 포트번호를 보면 80포트에 HTTP, 443포트에 SSL 서비스가 연동되어 있습니다.
`https://klkl0.tistory.com/63`

제가 Socket의 프로토콜을 HTTPS로 변경할 방법을 찾는데 한참 삽질을 했다는 이야기를 했는데, 정리하며 다시 생각해보니 Socket을 생성할 때 port번호를 지정함으로 이미 해당 port번호가 선점하고 있는 프로토콜로 요청을 보내겠다는 `설정`으로 Socket이 연결된 것이라 생각합니다.

처음 Socket 패키지를 사용할 때, 포트를 80으로 지정해서 `www.naver.com`으로 GET 요청을 날렸으니 HTTP 프로토콜로 요청이 간 것이고, 네이버 서버는 이 요청을 받고 옛날에 사용했던(보안에 취약한) 접근을 하려하니, 302 response를 날리고 `https://www.naver.com` HTTPS 프로토콜의 해당 호스트로 요청을 다시하라는 Location 메세지를 보내준 것 이구요.

HTTPS 프로토콜은 HTTP 프로토콜이 보안에 취약하다는 단점을 개선하고자 HTTP 프로토콜 기반에 SSL(Secure Socket Layer)라는 기술을 추가해서 만든 새로운 프로토콜 입니다. SSL 기술은 CA에 비밀키가 담긴 인증서를 요청하고, 데이터를 암호화 하는 등의 작업이 필요합니다.
이 일련의 과정을 SSL HandShake라 하는데 HTTPS 프로토콜은 서버에서 클라이언트의 요청을 받으면, SSL 핸드셰이킹 과정을 먼저 수행합니다.

> 밀러가 보내주신 `https://stackoverflow.com/questions/16972759/plain-socket-vs-ssl-socket` 참고해서 내용 수정했습니다. 감사합니다!

따라서 기본 Socket 패키지는 HTTPS 프로토콜을 사용 할 수 없는 것이 아닙니다. 자바에서 기본 Socket 객체를 만들면, 입력한 호스트와 port번호로 연결이 되는 것이고, 입력한 port번호에 기반한 프로토콜로 요청을 하게끔 `설정`이 되는 것 입니다.
즉, 포트 번호를 443으로 지정해서 Socket을 생성하면 요청을 보낼 때 HTTPS 프로토콜을 사용하겠다는 설정으로 `www.naver.com` 호스트에 Socket연결은 성공한 상태 입니다.

이 후 GET 요청을 보내면 response status가 400으로 돌아오게 됩니다.
GET 요청을 핸드셰이킹 과정의 처리 없이 요청을 보낸 것이죠. 따라서 잘못 된 요청 이라는 응답의 400 상태 코드가 돌아옵니다.

이 핸드셰이킹 과정 처리를 구현한 것이 Socket을 확장한 SSLSocket인 것 같습니다.
