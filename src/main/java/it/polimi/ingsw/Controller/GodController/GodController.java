package it.polimi.ingsw.Controller.GodController;

import it.polimi.ingsw.Controller.Controller;
import it.polimi.ingsw.Message.PosMessage;
import it.polimi.ingsw.Model.Model;

import java.io.Serializable;

/**Interface used to implement a Strategy Pattern regarding the special effect of the God card
 */
public abstract class GodController implements Serializable {
    private static final long serialVersionUID = 1L;
    /**Used to handle all the correct calls to the model for a special choose constructor
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     * @param posMessage Message containing the selected move position
     */
    public void handleSpecialChooseConstructor(Model model, Controller controller, PosMessage posMessage){}

    /**Used to handle all the correct calls to the model for a special move
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     * @param posMessage Message containing the selected move position
     */
    public void handleSpecialMove(Model model, Controller controller, PosMessage posMessage){};

    /**Used to handle all the correct calls to the model for a special move
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     * @param posMessage Message containing the selected move position
     */
    public void handleSpecialBuild(Model model, Controller controller, PosMessage posMessage){};

    /**Used to handle all the correct calls to the model to prepare a special choose constructor phase
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     */
    public void prepareSpecialChooseConstructor(Model model, Controller controller){};

    /**Used to handle all the correct calls to the model to prepare a special move phase
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     */
    public void prepareSpecialMove(Model model, Controller controller){};

    /**Used to handle all the correct calls to the model to prepare a special build phase
     *
     * @param model Model of the game
     * @param controller The Controller of the Application
     */
    public void prepareSpecialBuild(Model model, Controller controller){};
}
