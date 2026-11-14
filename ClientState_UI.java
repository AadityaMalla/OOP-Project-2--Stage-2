import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class ClientState_UI extends WarehouseState 
{
    private static ClientState_UI    instance;
    private final Warehouse warehouse = Warehouse.getInstance();

    private ClientState_UI() {
        super();
    }

    public static ClientState_UI getInstance() {
        if (instance == null) {
            instance = new ClientState_UI();
        }
        return instance;
    }

    public void getClientDetails()
    {
        String userID = context.getUser();
        Client client = warehouse.getClient(userID);
        if (client != null) {
            System.out.println("Client ID: " + client.getID());
            System.out.println("Name: " + client.getName());
            System.out.println("Address: " + client.getAddress());
        } else {
            System.out.println("Client not found.");
        }
    }

    public void showProductList() {
        List<ProductView> productViews = warehouse.getProductListView(context.CLIENT);
        for (ProductView pv : productViews) {
            System.out.println(pv.clientString());
        }
    }

    public void showClientTransactions() {
        String userID = context.getUser();
        Client client = warehouse.getClient(userID);
        if (client != null) {
            List<Transaction> transactions = client.getTransactions();
            if (transactions.isEmpty()) {
                System.out.println("No transactions found for this client.");
            } else {
                for (Transaction t : transactions) {
                    System.out.println(t);
                }
            }
        } else {
            System.out.println("Client not found.");
        }
    }

    /**
     * Moves to the wishlist-operations state. Uses {@code become} so that the
     * Back button there returns to this screen.
     */
    public void wishlistOperations() {
        context.become(WarehouseContext.WISHLIST, context.getUser());
    }

    public void placeOrder() {
        String userID = context.getUser();
        List<WishlistItem> wishlist = warehouse.getClientWishlist(userID);
        if (wishlist == null) 
        {
            System.out.println("Client not found.");
            return;
        }
        if (wishlist.isEmpty()) 
        {
            System.out.println("Wishlist is empty.");
            return;
        }

        List<OrderLine> lines = new ArrayList<>();
        for (WishlistItem w : wishlist)
        {
            System.out.println(w);
            System.out.print("Order this item? (y/n): ");
            boolean order = in.nextLine().trim().equalsIgnoreCase("y");
            if (!order) continue;

            System.out.print("Enter quantity: ");
            int qty;
            try
            {
                qty = Integer.parseInt(in.nextLine().trim());
            }
            catch (NumberFormatException e)
            {
                System.out.println("  Not a number — line skipped.");
                continue;
            }
            lines.add(new OrderLine(w.getProductID(), qty));
        }
        String summary = warehouse.createOrder(userID, lines);
        System.out.println(summary);
    }

    public void exit() 
    {
        System.out.println("Exiting Client State.");
        hideFrame(); // the next state (clerk or login) is text-based
        context.logout();
    }

    @Override
    public void run() {
        JFrame frame = prepareFrame();
        frame.setTitle("Warehouse - Client");
        frame.getContentPane().add(new ClientDetailsButton());
        frame.getContentPane().add(new ShowProductListButton());
        frame.getContentPane().add(new ShowClientTransactionsButton());
        frame.getContentPane().add(new WishlistOperationsButton());
        frame.getContentPane().add(new PlaceOrderButton());
        frame.getContentPane().add(new LogoutButton());
        showFrame(frame);
        }

    private class ClientDetailsButton extends JButton implements ActionListener
    {
        ClientDetailsButton() { super("Get Client Details"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { getClientDetails(); }
    }

    private class ShowProductListButton extends JButton implements ActionListener
    {
        ShowProductListButton() { super("Show Product List"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { showProductList(); }
    }

    private class ShowClientTransactionsButton extends JButton implements ActionListener
    {
        ShowClientTransactionsButton() { super("Show Client Transactions"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { showClientTransactions(); }
    }

    private class WishlistOperationsButton extends JButton implements ActionListener
    {
        WishlistOperationsButton() { super("Wishlist Operations"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { wishlistOperations(); }
    }

    private class PlaceOrderButton extends JButton implements ActionListener
    {
        PlaceOrderButton() { super("Place Order"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { placeOrder(); }
    }

    private class LogoutButton extends JButton implements ActionListener
    {
        LogoutButton() { super("Logout"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { exit(); }
    }
}