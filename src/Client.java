import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

class MessageSender implements Runnable {

    public final static int PORT = 8008;
    private DatagramSocket socket;
    private String hostName;
    private String clientName;

    MessageSender(DatagramSocket sock, String host) {
        socket = sock;
        hostName = host;
    }

    private void sendMessage(String s) throws Exception {
        byte buffer[] = s.getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        socket.send(packet);
    }

    private void sendRegisterCommand(JsonObject jsonObject) throws Exception {
        byte com_buffer[] = jsonObject.get("command").toString().getBytes();
        byte username_buffer[] = jsonObject.get("username").toString().getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(com_buffer, com_buffer.length, address, PORT);
        DatagramPacket userPacket = new DatagramPacket(username_buffer, username_buffer.length, address, PORT);
        socket.send(packet);
        socket.send(userPacket);
    }

    private void sendMessageCommand(JsonObject jsonObject) throws Exception {
        byte com_buffer[] = jsonObject.get("command").toString().getBytes();
        byte user_buffer[] = jsonObject.get("username").toString().getBytes();
        byte msg_buffer[] = jsonObject.get("message").toString().getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(com_buffer, com_buffer.length, address, PORT);
        DatagramPacket userPacket = new DatagramPacket(user_buffer, user_buffer.length, address, PORT);
        DatagramPacket msgPacket = new DatagramPacket(msg_buffer, msg_buffer.length, address, PORT);
        socket.send(packet);
        socket.send(userPacket);
        socket.send(msgPacket);
    }

    private void sendDeregisterCommand(JsonObject jsonObject) throws Exception {
        byte com_buffer[] = jsonObject.get("command").toString().getBytes();
        byte user_buffer[] = jsonObject.get("username").toString().getBytes();
        InetAddress address = InetAddress.getByName(hostName);
        DatagramPacket packet = new DatagramPacket(com_buffer, com_buffer.length, address, PORT);
        DatagramPacket userPacket = new DatagramPacket(user_buffer, user_buffer.length, address, PORT);
        socket.send(packet);
        socket.send(userPacket);

        System.exit(0);
    }

    private void sendCommand(JsonObject jsonObject) throws Exception {
        InetAddress address = InetAddress.getByName(hostName);
        byte[] buffer = jsonObject.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
        socket.send(packet);
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);
        boolean connected = false;
        boolean registered = false;
        do {
            try {
                System.out.println("Client connected to server");
                connected = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } while (!connected);

        while (true) {
            try {
                if (!registered) {
                    JsonObject registerJSON = new JsonObject();
                    System.out.print("Enter preferred username: ");
                    String username = scanner.nextLine();
                    registerJSON.addProperty("command", "register");
                    registerJSON.addProperty("username", username);

                    sendCommand(registerJSON);
                    //sendRegisterCommand(registerJSON);

                    clientName = username;

                    registered = true;
                }
                else {
                    JsonObject msgJSON = new JsonObject();
                    String client_msg = scanner.nextLine();
                    System.out.print("\r");

                    if (!client_msg.equals("bye")) {
                        msgJSON.addProperty("command", "msg");
                        msgJSON.addProperty("username", clientName);
                        msgJSON.addProperty("message", client_msg);

                        sendCommand(msgJSON);
                        //sendMessageCommand(msgJSON);
                    }
                    else {
                        msgJSON.addProperty("command", "deregister");
                        msgJSON.addProperty("username", clientName);

                        sendCommand(msgJSON);

                        System.exit(0);
                        //sendDeregisterCommand(msgJSON);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}

class MessageReceiver implements Runnable {

    DatagramSocket socket;
    byte buffer[];

    MessageReceiver(DatagramSocket sock) {
        socket = sock;
        buffer = new byte[1024];
    }

    public void run() {
        while (true) {
            try {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength()).trim();

                if (received.equals("502")) {
                    System.out.println("Unsuccessful registration, exiting...");
                    System.exit(502);
                }
                if (received.equals("501")) {
                    System.out.println("User not registered, exiting...");
                    System.exit(501);
                }
                if (received.equals("301")) {
                    System.out.println("Command unknown, exiting...");
                    System.exit(301);
                }
                if (received.equals("201")) {
                    System.out.println("Command parameters incomplete, exiting...");
                    System.exit(201);
                }
                else {
                    System.out.println(received);
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
}

public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter IP address of message board server: ");
        String host = scanner.nextLine();
        System.out.println("Connecting to " + host);

        DatagramSocket socket = new DatagramSocket();
        MessageReceiver receiver = new MessageReceiver(socket);
        MessageSender sender = new MessageSender(socket, host);

        Thread receiverThread = new Thread(receiver);
        Thread senderThread = new Thread(sender);

        receiverThread.start();
        senderThread.start();


    }
}