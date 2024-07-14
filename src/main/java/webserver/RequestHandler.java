package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Map;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
            String firstHeader = bufferedReader.readLine();
            System.out.println(firstHeader);

            String bodyValue = "";
            while (true) {
                String s = bufferedReader.readLine();
                System.out.println(s);
                String a = new String(s);
                int index = a.indexOf(":");
                if(index != -1) {
                    String headerName = a.substring(0, index);
                    if (headerName.equals("Content-Length")) {
                        bodyValue = a.substring(index + 1);
                    }
                }
                if ("".equals(s) || s == null) break;
            }

            String[] split = firstHeader.split(" ");
            String method = split[0];
            String url = split[1];
            int index = url.indexOf("?");
            String path = "";
            String queryString = "";
            if (index != -1) {
                path = url.substring(0, index);
                queryString = url.substring(index + 1);
            } else {
                path = url;
            }

            String ContentType = "";

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = new byte[0];
            if (method.equals("GET")) {
                if (path.equals("/index.html")) {
                    body = Files.readAllBytes(new File("./webapp/index.html").toPath());
                    ContentType = "text/html;charset=utf-8";
                } else if (path.equals("/user/form.html")) {
                    body = Files.readAllBytes(new File("./webapp/user/form.html").toPath());
                    ContentType = "text/html;charset=utf-8";
                } else if (path.equals("/user/create")) {
                    Map<String, String> parseQueryString = HttpRequestUtils.parseQueryString(queryString);
                    User user = new User(
                            parseQueryString.get("userId"),
                            parseQueryString.get("password"),
                            parseQueryString.get("name"),
                            parseQueryString.get("email")
                    );
                    System.out.println(user);
                    ContentType = "text/html;charset=utf-8";
                } else if (path.equals("/css/styles.css")) {
                    body = Files.readAllBytes(new File("./webapp/css/styles.css").toPath());
                    ContentType = "text/css;";
                } else if (path.equals("/css/bootstrap.min.css")) {
                    body = Files.readAllBytes(new File("./webapp/css/bootstrap.min.css").toPath());
                    ContentType = "text/css;";
                } else if (path.equals("/js/bootstrap.min.js")) {
                    body = Files.readAllBytes(new File("./webapp/js/bootstrap.min.js").toPath());
                    ContentType = "text/javascript;";
                } else if (path.equals("/js/jquery-2.2.0.min.js")) {
                    body = Files.readAllBytes(new File("./webapp/js/jquery-2.2.0.min.js").toPath());
                    ContentType = "text/javascript;";
                } else if (path.equals("/js/scripts.js")) {
                    body = Files.readAllBytes(new File("./webapp/js/scripts.js").toPath());
                    ContentType = "text/javascript;";
                } else if (path.equals("/favicon.ico")) {
                    body = Files.readAllBytes(new File("./webapp/favicon.ico").toPath());
                    ContentType = "image/x-icon;";
                } else {
                    body = "Hello World".getBytes();
                    ContentType = "text/html;charset=utf-8";
                }
            } else {
                String bodyContent = IOUtils.readData(bufferedReader, Integer.parseInt(bodyValue.trim()));
                Map<String, String> parseQueryString = HttpRequestUtils.parseQueryString(bodyContent);
                User user = new User(
                        parseQueryString.get("userId"),
                        parseQueryString.get("password"),
                        parseQueryString.get("name"),
                        parseQueryString.get("email")
                );
                System.out.println(user);
                ContentType = "text/html;charset=utf-8";
            }
            response200Header(dos, body.length, ContentType);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String ContentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + ContentType + "\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
