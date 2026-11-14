import java.awt.FlowLayout;
import java.util.Scanner;
import javax.swing.*;

/**
 * Base class for every state. Holds the shared context and console input, the
 * console input helpers used by the text states, and the frame helpers used by
 * the GUI states.
 *
 * <p>All GUI states share one window. A GUI state's {@code run()} calls
 * {@link #prepareFrame()}, adds its buttons, and calls {@link #showFrame(JFrame)};
 * the window's contents are swapped rather than a new window being opened for
 * every state.</p>
 */
public abstract class WarehouseState
{
    protected static final WarehouseContext context = WarehouseContext.getInstance();

    protected static final Scanner in = new Scanner(System.in);

    /** The single window shared by all GUI states; created on first use. */
    private static JFrame frame;

    public abstract void run();

    protected int getCommand(String prompt)
    {
        while (true)
        {
            System.out.print(prompt);
            String line = in.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.println("Please enter a number."); }
        }
    }

    protected String getToken(String prompt)
    {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    protected int getQuantity(String prompt)
    {
        while (true)
        {
            int n = getCommand(prompt);
            if (n >= 0) return n;
            System.out.println("Quantity cannot be negative.");
        }
    }

    protected boolean yesOrNo(String prompt)
    {
        System.out.print(prompt + " (y/n): ");
        return in.nextLine().trim().equalsIgnoreCase("y");
    }

    /**
     * Returns the shared window with its contents cleared, ready for a state to
     * add its buttons. Creates the window the first time it is needed.
     *
     * @return the shared window, empty
     */
    protected JFrame prepareFrame()
    {
        if (frame == null)
        {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setLayout(new FlowLayout());
        }
        frame.getContentPane().removeAll();
        return frame;
    }

    /**
     * Lays out and shows the window after a state has added its buttons.
     *
     * @param f the window returned by {@link #prepareFrame()}
     */
    protected void showFrame(JFrame f)
    {
        f.setSize(650, 160);
        f.setLocationRelativeTo(null);
        f.revalidate();
        f.repaint();
        f.setVisible(true);
    }

    /**
     * Hides the shared window, for when the program moves from a GUI state to
     * a text-based state.
     */
    protected void hideFrame()
    {
        if (frame != null) frame.setVisible(false);
    }
}