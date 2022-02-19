import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Server implements Runnable {

    public final static int PORT = 8008;
    private final static int BUFFER = 1024;

    private DatagramSocket socket;
    private ArrayList<User> client_list;
    private ArrayList<Integer> client_ports;
    private ArrayList<String> existing_clients;

    public Server() throws IOException {
        socket = new DatagramSocket(PORT);
        System.out.println("Server is running at port: " + PORT);
        client_list = new ArrayList();
        client_ports = new ArrayList();
        existing_clients = new ArrayList();
    }

    public void run() {
        byte[] buffer = new byte[BUFFER];
        while (true) {
            try {
                Arrays.fill(buffer, (byte) 0);
                DatagramPacket com_packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(com_packet);
                String recPacket = new String(buffer, 0, buffer.length).trim();
                JsonObject packetJson = new JsonParser().parse(recPacket).getAsJsonObject();
                InetAddress clientAddress = com_packet.getAddress();
                int client_port = com_packet.getPort();

                String command = packetJson.get("command").getAsString();

                if (command.equals("register")) {
                    String username = packetJson.get("username").getAsString();

                    User user = new User(clientAddress, username);
                    if (!existing_clients.contains(username)) {
                        existing_clients.add(username);
                        client_ports.add(client_port);
                        client_list.add(user);

                        JsonObject retJSON = new JsonObject();
                        retJSON.addProperty("command","ret_code");
                        retJSON.addProperty("ret_code", 501);
                        byte[] bufferJSON = retJSON.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(bufferJSON, bufferJSON.length, com_packet.getAddress(), com_packet.getPort());
                        socket.send(packet);

                        System.out.print("Users in message board: ");
                        for (int i = 0; i < existing_clients.size(); i++) {
                            if (i > 0) {
                                System.out.print(", ['" + existing_clients.get(i) + "']");
                            } else {
                                System.out.print("['" + existing_clients.get(i) + "']");
                            }
                        }
                        System.out.println(" ");

                        retJSON = new JsonObject();
                        retJSON.addProperty("command","ret_code");
                        retJSON.addProperty("ret_code", 401);
                        bufferJSON = retJSON.toString().getBytes();
                        packet = new DatagramPacket(bufferJSON, bufferJSON.length, com_packet.getAddress(), com_packet.getPort());
                        socket.send(packet);
                    } else {
                        JsonObject retJSON = new JsonObject();
                        retJSON.addProperty("command","ret_code");
                        retJSON.addProperty("ret_code", 502);
                        byte[] bufferJSON = retJSON.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(bufferJSON, bufferJSON.length, com_packet.getAddress(), com_packet.getPort());
                        socket.send(packet);
                    }
                }

                if (command.equals("deregister")) {
                    String username = packetJson.get("username").getAsString();

                    User user = new User(clientAddress, username);
                    if (existing_clients.contains(username)) {
                        existing_clients.remove(username);
                        client_list.remove(user);

                        System.out.println("User " + username + " exiting...");

                        System.out.println(" ");
                        System.out.print("Users in message board: ['");
                        for (int i = 0; i < existing_clients.size(); i++) {
                            if (i > 0) {
                                System.out.print(", ['" + existing_clients.get(i) + "']");
                            } else {
                                System.out.print("['" + existing_clients.get(i) + "']");
                            }
                        }
                        System.out.println("']");
                    } else {
                        JsonObject retJSON = new JsonObject();
                        retJSON.addProperty("command","ret_code");
                        retJSON.addProperty("ret_code", 501);
                        byte[] bufferJSON = retJSON.toString().getBytes();
                        DatagramPacket packet = new DatagramPacket(bufferJSON, bufferJSON.length, com_packet.getAddress(), com_packet.getPort());
                        socket.send(packet);
                    }
                }

                if (command.equals("msg")) {
                    String username = packetJson.get("username").getAsString();
                    String msg = packetJson.get("message").getAsString();
                    System.out.println(username + ": " + msg);

//                    String fullMsg = "Message sent successfully. \n" + username + ": " + msg;
//                    byte[] data = fullMsg.getBytes();
//
//                    for (int i = 0; i < client_list.size(); i++) {
//                        InetAddress cl_address = client_list.get(i).getAddress();
//                        int cl_port = client_ports.get(i);
//                        DatagramPacket packet = new DatagramPacket(data, data.length, cl_address, cl_port);
//                        socket.send(packet);
//                    }

                    JsonObject retJSON = new JsonObject();
                    retJSON.addProperty("command","ret_code");
                    retJSON.addProperty("ret_code", 401);
                    byte[] bufferJSON = retJSON.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(bufferJSON, bufferJSON.length, com_packet.getAddress(), com_packet.getPort());
                    socket.send(packet);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.run();
    }
}
