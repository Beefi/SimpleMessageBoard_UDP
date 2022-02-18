import java.net.InetAddress;

public class User {
    private InetAddress address;
    private String username;

    public User(InetAddress address, String username) {
        this.address = address;
        this.username = username;
    }

    public void setAddress(InetAddress ia) {
        this.address = ia;
    }

    public void setUsername(String s) {
        this.username = s;
    }

    public InetAddress getAddress() {
        return address;
    }

    public String getUsername() {
        return username;
    }
}
