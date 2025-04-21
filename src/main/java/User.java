import java.util.Scanner;

public abstract class User {
    protected String username;

    public User(String username) {
        this.username = username;
    }

    public abstract void showMenu(Scanner scanner);
}