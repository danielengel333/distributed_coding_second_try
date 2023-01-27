import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client extends Thread
{
    private int port;
    private Socket s;
    private ObjectOutputStream output_stream;
    private Node node;
    public Client(int port, Node node)
    {
        this.port = port;
        this.node = node;
    }

    public void send_message(Message message)
    {
        try
        {
            output_stream.writeObject(message);
            output_stream.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void run()
    {
        try
        {
            // let clients wait enough time for all servers to fire up
            synchronized (this)
            {
                wait(10);
            }
            s = new Socket("localhost", this.port);
            output_stream = new ObjectOutputStream(s.getOutputStream());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void terminate()
    {
        try
        {
            this.output_stream.close();
            this.s.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
