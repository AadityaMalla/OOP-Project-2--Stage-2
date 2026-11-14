import java.util.List;

/**
 * The wishlist-operations state, entered from {@link ClientState}.
 *
 * <p>Holds every command that edits or shows the logged-in client's wishlist:
 * display, add, delete, and change quantity. Placing an order stays in
 * {@link ClientState}. The client being served is the context's current user,
 * so this state never asks for a client id. Leaving the state returns to the
 * client menu through the context's return stack.</p>
 */
public class WishlistState extends WarehouseState
{
    private static WishlistState instance;
    private final Warehouse warehouse = Warehouse.getInstance();

    private static final int BACK = 0;
    private static final int DISPLAY_WISHLIST = 1;
    private static final int ADD_TO_WISHLIST = 2;
    private static final int REMOVE_FROM_WISHLIST = 3;
    private static final int MODIFY_QUANTITY = 4;
    private static final int HELP = 5;

    private WishlistState()
    {
        super();
    }

    /**
     * Returns the single instance of this state.
     *
     * @return the wishlist state
     */
    public static WishlistState getInstance()
    {
        if (instance == null)
        {
            instance = new WishlistState();
        }
        return instance;
    }

    /**
     * Shows the wishlist menu and runs commands until the user goes back to
     * the client menu.
     */
    @Override
    public void run()
    {
        boolean active = true;
        while (active)
        {
            int command = getCommand("\nWishlist Menu (client " + context.getUser() + "):\n" +
                    "1. Display Wishlist\n" +
                    "2. Add to Wishlist\n" +
                    "3. Remove from Wishlist\n" +
                    "4. Change Wishlist Quantity\n" +
                    "5. Help\n" +
                    "0. Back to Client Menu\n" +
                    "Enter command: ");

            switch (command)
            {
                case DISPLAY_WISHLIST -> displayWishlist();
                case ADD_TO_WISHLIST -> addToWishlist();
                case REMOVE_FROM_WISHLIST -> removeFromWishlist();
                case MODIFY_QUANTITY -> modifyQuantity();
                case HELP -> help();
                case BACK -> { back(); active = false; }
                default -> System.out.println("Invalid command. Please try again.");
            }
        }
    }

    /**
     * Prints each wishlist entry with the product's name, price, and stock.
     */
    public void displayWishlist()
    {
        List<WishlistView> wishlist = warehouse.getClientWishlistView(context.getUser());
        if (wishlist == null) { System.out.println("Client not found."); return; }
        if (wishlist.isEmpty()) { System.out.println("Wishlist is empty."); return; }
        for (WishlistView v : wishlist)
            System.out.printf("Product=%s, Name=%s, Price=$%.2f, WishQty=%d, Stock=%d%n",
                    v.getProductID(), v.getProductName(), v.getPrice(),
                    v.getWishlistQuantity(), v.getStock());
    }

    /**
     * Adds a quantity of a product to the wishlist. If the product is already
     * there, the quantity is added to the existing entry.
     */
    public void addToWishlist()
    {
        String productID = getToken("Enter product ID: ");
        int qty = getQuantity("Enter quantity: ");
        if (qty == 0) { System.out.println("Quantity must be positive."); return; }
        boolean ok = warehouse.addToWishlist(context.getUser(), productID, qty);
        System.out.println(ok ? "Product added to wishlist." : "Product not found.");
    }

    /**
     * Removes a product from the wishlist.
     */
    public void removeFromWishlist()
    {
        String productID = getToken("Enter product ID to remove: ");
        boolean ok = warehouse.removeFromWishlist(context.getUser(), productID);
        System.out.println(ok ? "Product removed from wishlist." : "That product is not on the wishlist.");
    }

    /**
     * Replaces the quantity of a product already on the wishlist.
     */
    public void modifyQuantity()
    {
        String productID = getToken("Enter product ID: ");
        int qty = getQuantity("Enter new quantity: ");
        if (qty == 0) { System.out.println("Quantity must be positive. Use Remove to delete an entry."); return; }
        boolean ok = warehouse.updateWishlistQuantity(context.getUser(), productID, qty);
        System.out.println(ok ? "Wishlist quantity updated." : "That product is not on the wishlist.");
    }

    /**
     * Lists the commands available in this state.
     */
    public void help()
    {
        System.out.println("Wishlist State Help:");
        System.out.println("1. Display Wishlist");
        System.out.println("2. Add to Wishlist");
        System.out.println("3. Remove from Wishlist");
        System.out.println("4. Change Wishlist Quantity");
        System.out.println("0. Back to Client Menu");
    }

    /**
     * Returns to the client menu by popping the context's return stack.
     */
    public void back()
    {
        System.out.println("Returning to Client Menu.");
        context.logout();
    }
}
