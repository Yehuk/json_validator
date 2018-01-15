package main.java;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;

/**
 * Class to implement server and validate json files
 */
public class json_validate {
        private static final int PORT = 80;     //We listen this port
        private static final int CODE = 200;    //Reply code

        /**
         * Try to validate json file
         * @param server is newly created server in 'main' function, we use this param to bind server and create context
         * @throws IOException
         */

        public json_validate(HttpServer server) throws IOException {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();        //We create GsonBuilder and
                // configure it to output Json that fits in a page for pretty printing
                server.bind(new InetSocketAddress(PORT), 0);    //Binding server to PORT
                server.createContext("/", httpExchange -> {     //Start of the server's work
                        int id = 0;     //Counter of request-id
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpExchange.getRequestBody()));
                        //We use BufferedReader to read text from a character-input stream, buffering characters
                        //so as to provide for the efficient reading of characters, arrays, and lines. And we are using InputStreamReader
                        //as a bridge between byte streams and character streams:  It reads bytes and decodes them into characters using a specified charset.
                        //Parameter of InputStreamReader is InputStream, which we get from method getRequestBody, which
                        //returns a InputStream for reading the request body.
                        String buildString = bufferedReader.readLine();         //Buffer line
                        StringBuilder stringBuilder = new StringBuilder();      //Here we read until EOF and building
                        // a line, containing our request
                        while (buildString != null) {
                                stringBuilder.append(buildString);
                                buildString = bufferedReader.readLine();
                        }
                        String request = stringBuilder.toString();

                        System.out.println("Request: " + request);      //Shows the request
                        String response = null;                         //Response is empty at the start

                        try {   //Here we try to convert json to string and back and wait for exceptions
                                Object object = gson.fromJson(request, Object.class);   //Json to string
                                response = gson.toJson(object);                         //String to json
                        } catch (JsonSyntaxException exception) {                       //If there is an exception
                                JsonObject error = new JsonObject();                                    //Creating
                                //the json foundation to the response
                                String errorDescription = exception.getMessage().split(": ")[1];      //"Extract"
                                //part of the error description before ": "
                                String errorMessage = errorDescription.split(" at ")[0];        //This part is Message
                                String errorPlace = errorDescription.split(" at ")[1];          //And this one is Place
                                int errorCode = exception.hashCode();   //Error's hash
                                //And now we form beautiful view for the response
                                error.addProperty("errorCode", errorCode);
                                error.addProperty("errorMessage", errorMessage);
                                error.addProperty("errorPlace", errorPlace);
                                error.addProperty("resource", request);
                                error.addProperty("request-id", id);
                                response = gson.toJson(error);
                        }
                        id = id + 1;
                        System.out.println("Response: " + response);     //Shows the response

                        //Sending the response and closing
                        httpExchange.sendResponseHeaders(CODE, response.length());
                        httpExchange.getResponseBody().write(response.getBytes());
                        httpExchange.close();


                });

        }


        /**
         * Main is used to start server and handle recieved json files
         * @throws IOException
         */
        public static void main(String[] args) throws IOException {
                final HttpServer server = HttpServer.create();  //We create server
                json_validate json = new json_validate(server);
                json.start(server);                             //We launch server
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