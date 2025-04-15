import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Authentication authentication = new Authentication();
        String sender = authentication.authenticate();
        String role = Users.getRole(sender);

        if (!role.equals("administrator")) {
            Messages.displayMessagesBySprint();
        }

        switch (role) {
            case "administrator":
                AdministratorMenu.start(scanner, sender);
                break;
            case "developer":
            case "product_owner":
            case "scrum_master":
                UserMenu.start(scanner, sender, role);
                break;
            default:
                System.out.println("Je hebt geen rol.");
        }
    }
}