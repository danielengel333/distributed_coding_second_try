import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread
{
    private int port;
    private ServerSocket ss;
    private Socket s;
    private Node node;
    private ObjectInputStream input_stream;
    public Server(int port, Node node)
    {
        this.port = port;
        this.node = node;
    }

    public Message read_input() throws IOException, ClassNotFoundException
    {
        try
        {
            if (this.input_stream == null)
            {
                return null;
            }
            Message input = (Message)input_stream.readObject();;
            return input;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void run()
    {
        try
        {
            ss = new ServerSocket(this.port);
            this.s = ss.accept();
            this.input_stream = new ObjectInputStream(s.getInputStream());
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
            this.input_stream.close();
            this.s.close();
            this.ss.close();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }
}
