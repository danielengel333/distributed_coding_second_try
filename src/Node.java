import java.lang.ref.Cleaner;
import java.net.*;
import java.util.*;
import java.io.*;
import java.util.concurrent.Semaphore;

public class Node extends Thread
{
    private int id;
    private int[] neighbors_id;
    private double[] edges;
    private int[] neighbors_input_port;
    private int[] neighbors_output_port;
    private int num_of_nodes;
    private int num_of_neighbors;
    private double[][] weight_matrix;
    private int num_visited;
    private HashMap<Integer,Server> servers;
    private HashMap<Integer,Client> clients;
    private HashMap<Integer,Boolean> visited;
    private Message linked_state;

    public Node(int id, int[] neighbors_id, double[] edges, int[] neighbors_input_port, int[] neighbors_output_port, int num_of_nodes)
    {
        this.id = id;
        this.neighbors_id = neighbors_id;
        this.edges = edges;
        this.neighbors_input_port = neighbors_input_port;
        this.neighbors_output_port = neighbors_output_port;
        this.num_of_nodes = num_of_nodes;
        this.num_of_neighbors = this.edges.length;

        this.visited = new HashMap<>();
        for (int i = 0; i < this.num_of_nodes; i++)
        {
            this.visited.put(i + 1, false);
        }
        this.visited.put(this.id, true);

        this.num_visited = 1;


        servers = new HashMap<>();
        clients = new HashMap<>();
        for (int i = 0; i < this.num_of_neighbors; i++)
        {
            servers.put(neighbors_id[i], new Server(this.neighbors_input_port[i], this));
            clients.put(neighbors_id[i], new Client(this.neighbors_output_port[i], this));
        }

        this.weight_matrix = new double[num_of_nodes][num_of_nodes];
        for (int i = 0; i < this.num_of_nodes; i++)
        {
            for (int j = 0; j < this.num_of_nodes; j++)
            {
                weight_matrix[i][j] = -1;
            }
        }
        for (int i = 0; i < this.edges.length; i++)
        {
            weight_matrix[this.id - 1][this.neighbors_id[i] - 1] = this.edges[i];
            weight_matrix[this.neighbors_id[i] - 1][this.id - 1] = this.edges[i];
        }

        this.linked_state = this.create_linked_state();
    }

    public void update_edge(int id2, double weight)
    {
        for (int i = 0; i < this.edges.length; i++)
        {
            if (neighbors_id[i] == id2)
            {
                this.edges[i] = weight;
                this.weight_matrix[this.id - 1][id2 - 1] = weight;
                this.weight_matrix[id2 - 1][this.id - 1] = weight;
                this.linked_state = this.create_linked_state();
                break;
            }
        }
    }

    public void print_graph()
    {
        for (int i = 0; i < this.num_of_nodes; i++)
        {
            for (int j = 0; j < this.num_of_nodes - 1; j++)
            {
                System.out.print(this.weight_matrix[i][j] + ", ");
            }
            System.out.println(this.weight_matrix[i][this.num_of_nodes - 1]);
        }
    }

    public void printId()
    {
        System.out.println(this.id);
    }


    @Override
    public void run()
    {

        // establish connections
        for (int id: this.neighbors_id)
        {
            servers.get(id).start();
        }
        for (int id: this.neighbors_id)
        {
            clients.get(id).start();
        }
        for (int id: this.neighbors_id)
        {
            try
            {
                clients.get(id).join();
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }


        // send linked state to neighbors
        try
        {
            Message message = this.linked_state;

            for (int id: this.neighbors_id)
            {
                clients.get(id).send_message(message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // receive and process linked states from other nodes
        while(this.num_visited < this.num_of_nodes)
        {
            for (int id1: this.neighbors_id)
            {
                try
                {
                    Message message = servers.get(id1).read_input();
                    if (message == null)
                    {
                        continue;
                    }

                    int source_id = message.getId();
                    if (visited.get(source_id))
                    {
                        continue;
                    }

                    for (int id2: this.neighbors_id)
                    {
                        clients.get(id2).send_message(message);
                    }

                    this.process_message(message, id1);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        //this.terminate();
    }

    public void process_message(Message message, int port)
    {
        try {
            int source_id = message.getId();
            Pair<Pair<Integer, Integer>, Double>[] link_arr = message.getLink_arr();
            for (Pair<Pair<Integer, Integer>, Double> link : link_arr) {
                int id1 = link.getKey().getKey();
                int id2 = link.getKey().getValue();
                double weight = link.getValue();

                this.weight_matrix[id1 - 1][id2 - 1] = weight;
                this.weight_matrix[id2 - 1][id1 - 1] = weight;
            }

            if (!this.visited.get(source_id))
            {
                this.visited.put(source_id, true);
                this.num_visited++;
            }
        }
        catch (Exception e)
        {
            //System.out.println("error caught in process_message");
            e.printStackTrace();
        }
    }

    public Message create_linked_state()
    {
        Pair<Pair<Integer,Integer>, Double>[] link_arr = new Pair[this.num_of_neighbors];
        for (int i = 0; i < this.num_of_neighbors; i++)
        {
            link_arr[i] = new Pair<>(new Pair<>(this.id, this.neighbors_id[i]),
                    this.weight_matrix[this.id - 1][this.neighbors_id[i] - 1]);
        }
        return new Message(this.id, link_arr);
    }

    public void terminate()
    {
        for (int id: this.neighbors_id)
        {
            servers.get(id).terminate();
            clients.get(id).terminate();
        }
    }

    public Message getLinked_state()
    {
        return this.linked_state;
    }
    public int getNum_of_nodes()
    {
        return this.num_of_nodes;
    }
    public int getNum_visited()
    {
        return this.num_visited;
    }
    public int[] getNeighbors_output_port()
    {
        return this.neighbors_output_port;
    }
    public void setNum_visited(int val)
    {
        this.num_visited = val;
    }
    public double[][] getWeight_matrix()
    {
        return this.weight_matrix;
    }
    public int getNodeId()
    {
        return this.id;
    }
    public int[] getNeighbors_id() {
        return neighbors_id;
    }
    public int getNum_of_neighbors()
    {
        return num_of_neighbors;
    }
    public double[] getEdges() {
        return edges;
    }
    public int[] getNeighbors_input_port() {
        return neighbors_input_port;
    }
}
