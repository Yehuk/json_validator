package main.java;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

public class json_validate {
        private static final int PORT = 80;     //We listen this port
        private static final int CODE = 200;    //Reply code



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
                        String request = bufferedReader.readLine();     //And now we convert it to string
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
                                error.addProperty("request-id", id);
                                response = gson.toJson(error);
                        } finally {id++;}

                        System.out.println("Response:" + response);     //Shows the response

                        //Sending the response and closing
                        httpExchange.sendResponseHeaders(CODE, response.length());
                        httpExchange.getResponseBody().write(response.getBytes());
                        httpExchange.close();


                });

        }



        public static void main(String[] args) throws IOException {
                final HttpServer server = HttpServer.create();  //We create server
                json_validate json = new json_validate(server);
                json.start(server);                             //We launch server
        }

        private static void start(HttpServer server){server.start();}   //Bind and start listening
        private static void stop(HttpServer server){server.stop(0);}    //Stop the work

}