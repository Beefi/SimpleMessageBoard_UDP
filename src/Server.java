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

                InetAddress clientAddress = com_packet.getAddress();
                int client_port = com_packet.getPort();

                if (recPacket.equals("register")) {
                    Arrays.fill(buffer, (byte) 0);
                    DatagramPacket user_packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(user_packet);

                    String username = new String(buffer, 0, buffer.length).trim();

                    User user = new User(clientAddress, username);
                    if (!existing_clients.contains(username)) {
                        existing_clients.add(username);
                        client_ports.add(client_port);
                        client_list.add(user);

                        System.out.println(" ");
                        System.out.print("Users in message board: ");
                        for (int i = 0; i < existing_clients.size(); i++) {
                            System.out.print("['" + existing_clients.get(i) + "'], ");
                        }
                        System.out.println(" ");
                    } else {
                        String retCode = "502";
                        byte[] data = retCode.getBytes();
                        DatagramPacket packet = new DatagramPacket(data, data.length, user_packet.getAddress(), user_packet.getPort());
                        socket.send(packet);
                    }
                }

                if (recPacket.equals("deregister")) {
                    Arrays.fill(buffer, (byte) 0);
                    DatagramPacket user_packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(user_packet);

                    String username = new String(buffer, 0, buffer.length).trim();

                    User user = new User(clientAddress, username);
                    if (existing_clients.contains(username)) {
                        existing_clients.remove(username);
                        client_list.remove(user);

                        System.out.println("User " + username + " exiting...");

                        System.out.println(" ");
                        System.out.print("User in message board: ['");
                        for (int i = 0; i < existing_clients.size(); i++) {
                            System.out.print(existing_clients.get(i));
                        }
                        System.out.println("']");
                    }
                }

                if (recPacket.equals("msg")) {
                    Arrays.fill(buffer, (byte) 0);
                    DatagramPacket user_packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(user_packet);

                    String username = new String(buffer, 0, buffer.length).trim();

                    Arrays.fill(buffer, (byte) 0);
                    DatagramPacket msg_packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(msg_packet);

                    String msg = new String(buffer, 0, buffer.length).trim();

                    System.out.println(username + ": "+ msg);
                    byte[] data = (username + ": " + msg).getBytes();

                    for (int i = 0; i < client_list.size(); i++) {
                        InetAddress cl_address = client_list.get(i).getAddress();
                        int cl_port = client_ports.get(i);
                        DatagramPacket packet = new DatagramPacket(data, data.length, cl_address, cl_port);
                        socket.send(packet);
                    }
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
