package es.cristichi.fnac.gui;

import javax.swing.*;

public abstract class ExitableJComponent extends JComponent {
    public ExitableJComponent(){
        super();
    }
    /**
     * This method should ensure that the onExitListener is executed when the component "exists" to the main menu.
     * For instance, when a Night finishes.
     * @param onExitListener Listener to run when the component exists to main menu.
     */
    public abstract void addOnExitListener(Runnable onExitListener);
}
