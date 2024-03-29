package it.polimi.ingsw.Model;

/**
 * This class represent every cell that the <em>Board</em> is composed of.
 */
public class Tile {

    private final Position position;
    private boolean occupied;
    private int constructionLevel;
    private boolean dome;
    private Constructor actualConstructor;

    /**
     * Creates a <em>Tile</em>, using a default value for every attribute, except for position, which is set to the value
     * we use as a parameter. The attribute position is Final type because once created, it can not be changed, as
     * it is not allowed to change position of any cell of the <em>Board</em>.
     *
     * @param pos the actual position of the <em>Tile</em> in the <em>Board</em>;
     */
    public Tile(Position pos)   {
        this.position = pos;
        this.occupied = false;
        this.constructionLevel = 0;
        this.dome = false;
        this.actualConstructor = null;
    }

    public Position getPos()    {
        return this.position;
    }

    public void setOccupied(boolean b)    {
        this.occupied = b;
    }

    public boolean getOccupied()    {
        return this.occupied;
    }

    /**
     * Increase the constructionLevel value by 1. If the current value of constructionLevel is 3 and the
     * attribute dome is false, the method set dome to true. On the other hand, if constructionLevel is 3, but
     * dome is true, the method throws an exception, because the <em>Tile</em> is already complete.
     *
     * @exception IllegalArgumentException if the construction on the <em>Tile</em> is already complete.
     */
    public void increaseConstructionLevel()  throws IllegalArgumentException {
        if(getDome())   {
            throw new IllegalArgumentException("Error, max construction's capacity reached");
        }
        else    {
            if(getConstructionLevel() < 3)  {
                this.constructionLevel = this.constructionLevel + 1;
            }
            else if(getConstructionLevel() == 3 && !getDome())   {
                setDome(true);
            }

        }
    }

    public int getConstructionLevel()   {
        return this.constructionLevel;
    }

    public void setDome(boolean b)  {
        this.dome = b;
    }

    public boolean getDome()    {
        return this.dome;
    }

    public Constructor getActualConstructor() {
        return actualConstructor;
    }

    public void setActualConstructor(Constructor c) {
        this.actualConstructor = c;
        this.setOccupied(true);
    }
}


