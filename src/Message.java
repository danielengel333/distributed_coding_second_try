import java.io.Serializable;

class Message implements Serializable
{
    private int id;
    private Pair<Pair<Integer, Integer>, Double>[] link_arr;

    public Message(int id, Pair<Pair<Integer, Integer>, Double>[] link_arr)
    {
        this.id = id;
        this.link_arr = link_arr;
    }

    public int getId()
    {
        return this.id;
    }

    public Pair<Pair<Integer, Integer>, Double>[] getLink_arr()
    {
        return this.link_arr;
    }
}
