import java.util.ArrayList;
import java.util.List;

/**
 * The client menu state. Shows the logged-in client's details, the product
 * list, and transactions, and places orders. Wishlist editing has moved to
 * {@link WishlistState}, reached through the "Wishlist Operations" command.
 */
public class ClientState extends WarehouseState
{
    private static ClientState instance;
    private final Warehouse warehouse = Warehouse.getInstance();

    private static final int LOGOUT = 0;
    private static final int CLIENT_DETAILS = 1;
    private static final int SHOW_PRODUCT_LIST = 2;
    private static final int SHOW_CLIENT_TRANSACTIONS = 3;
    private static final int WISHLIST_OPS = 4;
    private static final int PLACE_ORDER = 5;
    private static final int HELP = 6;

    private ClientState() {
        super();
    }

    public static ClientState getInstance() {
        if (instance == null) {
            instance = new ClientState();
        }
        return instance;
    }

    public void help() {
        System.out.println("Client State Help:");
        System.out.println("1. View Client Details");
        System.out.println("2. Show Product List");
        System.out.println("3. Show Client Transactions");
        System.out.println("4. Wishlist Operations (view, add, remove, change quantity)");
        System.out.println("5. Place Order");
        System.out.println("0. Logout");
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
        List<ProductView> productViews = warehouse.getProductListView(WarehouseContext.CLIENT);
        if (productViews.isEmpty()) { System.out.println("No products available."); return; }
        for (ProductView pv : productViews) {
            System.out.println(pv.clientString());
        }
    }

    public void showClientTransactions() {
        List<Transaction> transactions = warehouse.getClientTransactions(context.getUser());
        if (transactions == null) {
            System.out.println("Client not found.");
        } else if (transactions.isEmpty()) {
            System.out.println("No transactions found for this client.");
        } else {
            for (Transaction t : transactions) {
                System.out.println(t);
            }
        }
    }

    /**
     * Moves to the wishlist-operations state. Uses {@code become} so that
     * leaving that state returns here.
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
            if (!yesOrNo("Order this item?")) continue;
            int qty = getQuantity("Enter quantity: ");
            lines.add(new OrderLine(w.getProductID(), qty));
        }
        String summary = warehouse.createOrder(userID, lines);
        System.out.println(summary);
    }

    public void exit()
    {
        System.out.println("Exiting Client State.");
        context.logout();
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            int choice = getCommand("\nClient Menu (client " + context.getUser() + "):\n" +
                    "1. View Client Details\n" +
                    "2. Show Product List\n" +
                    "3. Show Client Transactions\n" +
                    "4. Wishlist Operations\n" +
                    "5. Place Order\n" +
                    "6. Help\n" +
                    "0. Logout\n" +
                    "Enter command: ");

            switch (choice) {
                case CLIENT_DETAILS -> getClientDetails();
                case SHOW_PRODUCT_LIST -> showProductList();
                case SHOW_CLIENT_TRANSACTIONS -> showClientTransactions();
                case WISHLIST_OPS -> { wishlistOperations(); running = false; }
                case PLACE_ORDER -> placeOrder();
                case HELP -> help();
                case LOGOUT -> { exit(); running = false; }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }
}
