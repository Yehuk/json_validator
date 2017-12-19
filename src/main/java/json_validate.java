import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.stream.Collectors;

public class GetJSONFormat implements Server {

    @NotNull
    private final HttpServer myServer;
    @NotNull
    private final Gson Jbuilder;

    private static final int PORT = 80;
    private static final int CODE_OK = 200;
    private static final String ROOT = "/";

    /**
     * Checking Json file
     *
     * @throws IOException because method create() of HttpServer can throw IOException
     */
    public GetJSONFormat() throws IOException {
        this.Jbuilder= new GsonBuilder().setPrettyPrinting().create();
        this.myServer= HttpServer.create(new InetSocketAddress(PORT), 0);
        this.myServer.createContext(ROOT, http -> {
            InputStreamReader isr = new InputStreamReader(http.getRequestBody());
            final String jsonRequest = new BufferedReader(isr).lines().collect(Collectors.joining());
            System.out.println("request:" + jsonRequest);
            String jsonResponse;
            try {
                Object object = Jbuilder.fromJson(jsonRequest, Object.class);
                jsonResponse = Jbuilder.toJson(object);
            } catch (JsonSyntaxException e) {
                JsonObject jsonError = new JsonObject();
                jsonError.addProperty("message", e.getMessage());
                jsonResponse = Jbuilder.toJson(jsonError);
            }
            System.out.println("response:" + jsonResponse);
            http.sendResponseHeaders(CODE_OK, jsonResponse.length());
            http.getResponseBody().write(jsonResponse.getBytes());
            http.close();
        });
    }

    /**
     * Starting server and waiting for a Json files
     *
     * @param args - does not matter
     * @throws IOException - because constructor of Formatter can throw IOException
     */
    public static void main(String[] args) throws IOException {
        GetJSONFormat jsonFormat = new GetJSONFormat();
        jsonFormat.start();
        Runtime.getRuntime().addShutdownHook(new Thread(jsonFormat::stop));
    }

    /**
     * Implements method of bind server to HTTP port and start listening.
     */
    @Override
    public void start() {
        this.myServer.start();
    }

    /**
     * Implements method of stop listening and free all the resources.
     */
    @Override
    public void stop() {
        this.myServer.stop(0);
    }
}
