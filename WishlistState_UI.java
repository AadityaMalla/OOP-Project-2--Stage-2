import java.awt.Font;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;

/**
 * GUI version of the wishlist-operations state, entered from
 * {@link ClientState_UI} with the "Wishlist Operations" button.
 *
 * <p>Holds every command that shows or edits the logged-in client's wishlist:
 * display, add, remove, and change quantity. Placing an order stays in the
 * client state. Each command is its own button class that listens to itself,
 * so no code checks which button was pressed. The client being served is the
 * context's current user, so this state never asks for a client id. The Back
 * button returns to the client screen through the context's return stack.</p>
 */
public class WishlistState_UI extends WarehouseState
{
    private static WishlistState_UI instance;
    private final Warehouse warehouse = Warehouse.getInstance();

    private WishlistState_UI()
    {
        super();
    }

    /**
     * Returns the single instance of this state.
     *
     * @return the wishlist state
     */
    public static WishlistState_UI getInstance()
    {
        if (instance == null)
        {
            instance = new WishlistState_UI();
        }
        return instance;
    }

    /**
     * Shows the wishlist screen with one button per command.
     */
    @Override
    public void run()
    {
        JFrame frame = prepareFrame();
        frame.setTitle("Warehouse - Wishlist (client " + context.getUser() + ")");
        frame.getContentPane().add(new DisplayWishlistButton());
        frame.getContentPane().add(new AddToWishlistButton());
        frame.getContentPane().add(new RemoveFromWishlistButton());
        frame.getContentPane().add(new ChangeQuantityButton());
        frame.getContentPane().add(new BackButton());
        showFrame(frame);
    }

    /**
     * Shows each wishlist entry with the product's name, price, and stock.
     */
    public void displayWishlist()
    {
        List<WishlistView> wishlist = warehouse.getClientWishlistView(context.getUser());
        if (wishlist == null) { showMessage("Client not found."); return; }
        if (wishlist.isEmpty()) { showMessage("Wishlist is empty."); return; }

        StringBuilder text = new StringBuilder();
        for (WishlistView v : wishlist)
            text.append(String.format("Product=%s, Name=%s, Price=$%.2f, WishQty=%d, Stock=%d%n",
                    v.getProductID(), v.getProductName(), v.getPrice(),
                    v.getWishlistQuantity(), v.getStock()));

        JTextArea area = new JTextArea(text.toString());
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        JOptionPane.showMessageDialog(null, area,
                "Wishlist for " + context.getUser(), JOptionPane.PLAIN_MESSAGE);
    }

    /**
     * Adds a quantity of a product to the wishlist. If the product is already
     * there, the quantity is added to the existing entry.
     */
    public void addToWishlist()
    {
        String productID = askText("Enter product ID:");
        if (productID == null) return;
        Integer qty = askPositiveNumber("Enter quantity:");
        if (qty == null) return;
        boolean ok = warehouse.addToWishlist(context.getUser(), productID, qty);
        showMessage(ok ? "Product added to wishlist." : "Product not found.");
    }

    /**
     * Removes a product from the wishlist.
     */
    public void removeFromWishlist()
    {
        String productID = askText("Enter product ID to remove:");
        if (productID == null) return;
        boolean ok = warehouse.removeFromWishlist(context.getUser(), productID);
        showMessage(ok ? "Product removed from wishlist." : "That product is not on the wishlist.");
    }

    /**
     * Replaces the quantity of a product already on the wishlist.
     */
    public void changeQuantity()
    {
        String productID = askText("Enter product ID:");
        if (productID == null) return;
        Integer qty = askPositiveNumber("Enter new quantity (use Remove to delete an entry):");
        if (qty == null) return;
        boolean ok = warehouse.updateWishlistQuantity(context.getUser(), productID, qty);
        showMessage(ok ? "Wishlist quantity updated." : "That product is not on the wishlist.");
    }

    /**
     * Returns to the client screen by popping the context's return stack.
     */
    public void back()
    {
        context.logout();
    }

    // ---- dialog helpers ----

    /** Asks for a line of text; returns null if the user cancels or enters nothing. */
    private String askText(String prompt)
    {
        String s = JOptionPane.showInputDialog(null, prompt);
        if (s == null || s.trim().isEmpty()) return null;
        return s.trim();
    }

    /** Asks for a positive whole number; returns null on cancel or bad input. */
    private Integer askPositiveNumber(String prompt)
    {
        String s = askText(prompt);
        if (s == null) return null;
        try
        {
            int n = Integer.parseInt(s);
            if (n > 0) return n;
            showMessage("Quantity must be positive.");
        }
        catch (NumberFormatException e)
        {
            showMessage("Not a number.");
        }
        return null;
    }

    /** Shows a short message in a dialog. */
    private void showMessage(String message)
    {
        JOptionPane.showMessageDialog(null, message);
    }

    // ---- buttons: each listens to itself, so no conditionals on the event source ----

    private class DisplayWishlistButton extends JButton implements ActionListener
    {
        DisplayWishlistButton() { super("Display Wishlist"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { displayWishlist(); }
    }

    private class AddToWishlistButton extends JButton implements ActionListener
    {
        AddToWishlistButton() { super("Add to Wishlist"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { addToWishlist(); }
    }

    private class RemoveFromWishlistButton extends JButton implements ActionListener
    {
        RemoveFromWishlistButton() { super("Remove from Wishlist"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { removeFromWishlist(); }
    }

    private class ChangeQuantityButton extends JButton implements ActionListener
    {
        ChangeQuantityButton() { super("Change Wishlist Quantity"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { changeQuantity(); }
    }

    private class BackButton extends JButton implements ActionListener
    {
        BackButton() { super("Back to Client Menu"); addActionListener(this); }
        public void actionPerformed(ActionEvent e) { back(); }
    }
}
