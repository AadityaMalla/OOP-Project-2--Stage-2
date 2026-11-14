import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

/**
 * Screen for the logged-in client's wishlist operations.
 * <p>Reached from the client screen's "Wishlist operations" button. Holds every
 * command that shows or edits the wishlist: show, add, remove, and change
 * quantity. Placing an order stays on the client screen. The client being
 * served is the context's current user, so this state never asks for a client
 * id. Each button listens to itself, so no code checks which button was
 * pressed. "Back to client menu" pops the context's return stack, which returns
 * to the client screen as the same client.</p>
 * @author Aaditya Malla
 * @version 1.0
 */
public class WishlistState extends WarehouseState
{
    private static WishlistState instance;
    private final Warehouse warehouse = Warehouse.getInstance();

    /**
     * Private constructor to enforce singleton pattern.
     */
    private WishlistState() { super(); }

    /**
     * Returns the singleton instance of WishlistState.
     * @return the singleton instance of WishlistState
     */
    public static WishlistState getInstance()
    {
        if (instance == null) instance = new WishlistState();
        return instance;
    }

    /**
     * Displays the wishlist screen: the wishlist buttons on top and the output
     * area below.
     */
    @Override
    public void run()
    {
        JFrame frame = prepareFrame();
        frame.setTitle("Warehouse - Wishlist for Client " + context.getUser());
        frame.getContentPane().setLayout(new BorderLayout());   // buttons on top, output below

        JPanel buttons = new JPanel(new FlowLayout());
        buttons.add(new ShowWishlistButton());
        buttons.add(new AddWishlistButton());
        buttons.add(new RemoveWishlistButton());
        buttons.add(new ChangeQuantityButton());
        buttons.add(new BackButton());

        output.setEditable(false);
        frame.getContentPane().add(buttons, BorderLayout.NORTH);
        frame.getContentPane().add(new JScrollPane(output), BorderLayout.CENTER);
        showFrame(frame);
    }

    /**
     * Displays each wishlist entry with the product's name, price, and stock.
     */
    private void showWishlist()
    {
        List<WishlistView> wishlist = warehouse.getClientWishlistView(context.getUser());
        if (wishlist == null) { display("Client not found."); return; }
        if (wishlist.isEmpty()) { display("Wishlist is empty."); return; }
        StringBuilder sb = new StringBuilder("Wishlist:\n");
        for (WishlistView v : wishlist)
            sb.append(String.format("Product=%s, Name=%s, Price=$%.2f, WishQty=%d, Stock=%d%n",
                    v.getProductID(), v.getProductName(), v.getPrice(),
                    v.getWishlistQuantity(), v.getStock()));
        display(sb.toString());
    }

    /**
     * Adds a quantity of a product to the wishlist. If the product is already
     * on the wishlist, the quantity is added to the existing entry.
     */
    private void addItemToWishlist()
    {
        String productID = promptRequired("Enter product ID:");
        if (productID == null) return;                  // cancelled
        Integer qty = promptInt("Enter quantity:", 1);
        if (qty == null) return;                        // cancelled

        boolean ok = warehouse.addToWishlist(context.getUser(), productID, qty);
        display(ok ? "Product added to wishlist. Product ID: " + productID : "Error: product not found.");
    }

    /**
     * Removes a product from the wishlist.
     */
    private void removeFromWishlist()
    {
        String productID = promptRequired("Enter product ID to remove:");
        if (productID == null) return;                  // cancelled

        boolean ok = warehouse.removeFromWishlist(context.getUser(), productID);
        display(ok ? "Product removed from wishlist. Product ID: " + productID
                   : "Error: that product is not on the wishlist.");
    }

    /**
     * Replaces the quantity of a product already on the wishlist.
     */
    private void changeQuantity()
    {
        String productID = promptRequired("Enter product ID:");
        if (productID == null) return;                  // cancelled
        Integer qty = promptInt("Enter new quantity:", 1);
        if (qty == null) return;                        // cancelled

        boolean ok = warehouse.updateWishlistQuantity(context.getUser(), productID, qty);
        display(ok ? "Wishlist quantity updated. Product ID: " + productID + ", new quantity: " + qty
                   : "Error: that product is not on the wishlist.");
    }

    /**
     * Button to display the wishlist.
     */
    private class ShowWishlistButton extends JButton implements ActionListener {
        ShowWishlistButton() { super("Show wishlist"); addActionListener(this); }
        @Override public void actionPerformed(ActionEvent e) { showWishlist(); }
    }
    /**
     * Button to add an item to the wishlist.
     */
    private class AddWishlistButton extends JButton implements ActionListener {
        AddWishlistButton() { super("Add to wishlist"); addActionListener(this); }
        @Override public void actionPerformed(ActionEvent e) { addItemToWishlist(); }
    }
    /**
     * Button to remove an item from the wishlist.
     */
    private class RemoveWishlistButton extends JButton implements ActionListener {
        RemoveWishlistButton() { super("Remove from wishlist"); addActionListener(this); }
        @Override public void actionPerformed(ActionEvent e) { removeFromWishlist(); }
    }
    /**
     * Button to change the quantity of a wishlist item.
     */
    private class ChangeQuantityButton extends JButton implements ActionListener {
        ChangeQuantityButton() { super("Change quantity"); addActionListener(this); }
        @Override public void actionPerformed(ActionEvent e) { changeQuantity(); }
    }
    /**
     * Button to return to the client screen.
     */
    private class BackButton extends JButton implements ActionListener {
        BackButton() { super("Back to client menu"); addActionListener(this); }
        @Override public void actionPerformed(ActionEvent e) { context.logout(); }
    }
}
