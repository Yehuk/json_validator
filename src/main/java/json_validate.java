package main.java;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Class to implement server and validate json files
 */
public class json_validate {
        /**
         *
         */
        private static final int PORT = 80;     //We listen this port
        /**
         *
         */
        private static final int CODE = 200;    //Reply code

        /**
         * Try to validate json file
         * @param server is newly created server in 'main' function, we use this param to bind server and create context
         * @throws IOException this exception can happen when something wrong with input/output operations
         */

        public json_validate(HttpServer server) throws IOException {
                /**
                 * We create GsonBuilder and configure it to output Json that fits in a page for pretty printing
                 */
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                /**
                 * Binding server to PORT
                 */
                server.bind(new InetSocketAddress(PORT), 0);
                /**
                 * Start of the server's work
                 */
                server.createContext("/", httpExchange -> {
                        /**
                         * Counter of request-id
                         */
                        int id = 0;
                        /**
                         * We use BufferedReader to read text from a character-input stream, buffering characters
                         * so as to provide for the efficient reading of characters, arrays, and lines. And we are using InputStreamReader
                         * as a bridge between byte streams and character streams:  It reads bytes and decodes them into characters using a specified charset.
                         * Parameter of InputStreamReader is InputStream, which we get from method getRequestBody, which
                         * returns a InputStream for reading the request body.
                         */
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
                        /**
                         * Buffer line
                         */
                        String buildString = bufferedReader.readLine();
                        /**
                         * Here we read until EOF and building a line, containing our request
                         */
                        StringBuilder stringBuilder = new StringBuilder();
                        while (buildString != null) {
                                stringBuilder.append(buildString);
                                buildString = bufferedReader.readLine();
                        }
                        String request = stringBuilder.toString();
                        /**
                         * Shows the request
                         */
                        System.out.println("Request: " + request);
                        /**
                         * Response is empty at the start
                         */
                        String response = null;
                        /**
                         * Here we try to convert json to string and back and wait for exceptions
                         */
                        try {
                                /**
                                 * Json to string
                                 */
                                Object object = gson.fromJson(request, Object.class);
                                /**
                                 * String to json
                                 */
                                response = gson.toJson(object);
                                /**
                                 * If there is an exception
                                 */
                        } catch (JsonSyntaxException exception) {
                                /**
                                 * Creating the json foundation to the response
                                 */
                                JsonObject error = new JsonObject();
                                /**
                                 * "Extract" part of the error description before ": "
                                 */
                                String errorDescription = exception.getMessage().split(": ")[1];
                                /**
                                 * This part is Message
                                 */
                                String errorMessage = errorDescription.split(" at ")[0];
                                /**
                                 * And this one is Place
                                 */
                                String errorPlace = errorDescription.split(" at ")[1];
                                /**
                                 * Error's hash
                                 */
                                int errorCode = exception.hashCode();
                                /**
                                 * And now we form beautiful view for the response
                                 */
                                error.addProperty("errorCode", errorCode);
                                error.addProperty("errorMessage", errorMessage);
                                error.addProperty("errorPlace", errorPlace);
                                error.addProperty("resource", request);
                                error.addProperty("request-id", id);
                                response = gson.toJson(error);
                        }
                        id = id + 1;
                        /**
                         * Shows the response
                         */
                        System.out.println("Response: " + response);
                        /**
                         * Sending the response and closing
                         */
                        httpExchange.sendResponseHeaders(CODE, response.length());
                        httpExchange.getResponseBody().write(response.getBytes());
                        httpExchange.close();


                });

        }


        /**
         * Main is used to start server and handle recieved json files
         * @throws IOException this exception can happen when something wrong with input/output operations
         */
        public static void main(String[] args) throws IOException {
                /**
                 * We create server
                 */
                final HttpServer server = HttpServer.create();
                json_validate json = new json_validate(server);
                /**
                 * We launch server
                 */
                json.start(server);
        }

        /**
         * Bind and start listening
         * @param server we start server, that we get as a param
         */
        private static void start(HttpServer server){server.start();}

        /**
         * Stop the work
         * @param server we close server, that we get as a param
         */
         private static void stop(HttpServer server){server.stop(0);}

}