import java.util.*;
import java.io.*;
import java.net.*;

public class ExManager {
    private String path;
    private int num_of_nodes;
    // your code here

    private Node[] nodes;

    public ExManager(String path)
    {
        this.path = path;
        // your code here
    }

    public Node get_node(int id)
    {
        // your code here
        return this.nodes[id - 1];
    }

    public int getNum_of_nodes()
    {
        return this.num_of_nodes;
    }

    public void update_edge(int id1, int id2, double weight)
    {
        //your code here
        this.nodes[id1 - 1].update_edge(id2, weight);
        this.nodes[id2 - 1].update_edge(id1, weight);
    }

    public void read_txt() throws FileNotFoundException {
        // your code here
        Scanner scanner = new Scanner(new File(path));

        String line = scanner.nextLine();
        this.num_of_nodes = Integer.parseInt(line);
        this.nodes = new Node[this.num_of_nodes];

        for (int i = 0; i < this.num_of_nodes; i++)
        {
            line = scanner.nextLine();
            String[] arr = line.split(" ");
            int num_neighbors = arr.length / 4;

            int[] neighbors_id = new int[num_neighbors];
            double[] edges = new double[num_neighbors];
            int[] neighbors_input_port = new int[num_neighbors];
            int[] neighbors_output_port = new int[num_neighbors];
            int id = i + 1;

            for (int j = 1; j < arr.length; j += 4)
            {
                neighbors_id[j / 4] = Integer.parseInt(arr[j]);
                edges[j / 4] = Double.parseDouble(arr[j+1]);
                neighbors_input_port[j / 4] = Integer.parseInt(arr[j+2]);
                neighbors_output_port[j / 4] = Integer.parseInt(arr[j+3]);
            }

            this.nodes[i] = new Node(id, neighbors_id, edges,
                    neighbors_input_port, neighbors_output_port, num_of_nodes);
        }
    }

    public void start()
    {
        // your code here
        for (int i = 0; i < this.num_of_nodes; i++)
        {
            this.nodes[i] = new Node(this.nodes[i].getNodeId(), this.nodes[i].getNeighbors_id(),
                    this.nodes[i].getEdges(), this.nodes[i].getNeighbors_input_port(),
                    this.nodes[i].getNeighbors_output_port(), this.num_of_nodes);
        }
        try
        {
            for (int i = 0; i < this.num_of_nodes; i++)
            {
                this.nodes[i].start();
            }
            /*while (true)
            {
                int sum = 0;
                for (int i = 0; i < this.num_of_nodes; i++)
                {
                    sum += this.nodes[i].getNum_visited();
                }
                System.out.println(sum / this.num_of_nodes);
                if(sum == this.num_of_nodes * this.num_of_nodes)
                    break;
            }*/
            for (int i = 0; i < this.num_of_nodes; i++)
            {
                this.nodes[i].join();
            }
            for (int i = 0; i < this.num_of_nodes; i++)
            {
                this.nodes[i].terminate();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void terminate()
    {
        // your code here
        for (int i = 0; i < this.num_of_nodes; i++)
        {
            this.nodes[i].terminate();
        }
    }
}
