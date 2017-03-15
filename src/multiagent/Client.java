package multiagent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Nichlas on 14-03-2017.
 */
public class Client {
    private BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );

    //private List< Agent > agents = new ArrayList< Agent >();
    public Node initialState;
    public static char[][] goals; // Taget fra node
    public static boolean[][] walls; // Taget fra node
    public static int MAX_ROW;
    public static int MAX_COL;
    static Map< Character, String > colors = new HashMap< Character, String >();

    public Client()throws IOException {
        LoadMap();

        for(int j = 0; j < initialState.agents.length; j++){
            for (int i = 0; i< initialState.agents[j].length; i++){
                if(initialState.agents[j][i] !=  '\u0000')
                System.err.println("x: " + j + "y: " + i + "agent: " + initialState.agents[j][i] + "color: " + colors.get(initialState.agents[j][i]));
            }

        }
    }



    public void LoadMap()throws IOException{// Read lines specifying colors
        String line, color;

        while ( ( line = in.readLine() ).matches( "^[a-z]+:\\s*[0-9A-Z](,\\s*[0-9A-Z])*\\s*$" ) ) {
            line = line.replaceAll( "\\s", "" );
            color = line.split( ":" )[0];

            for ( String id : line.split( ":" )[1].split( "," ) )
                colors.put( id.charAt( 0 ), color );
        }

        int max_row = 0;
        boolean agentFound = false;


        int max_col = 0;

        ArrayList<String> store_contents = new ArrayList<String>();

        while (!line.equals("")) {

            store_contents.add(line);

            if(line.length() - 1 > max_col)
                max_col = line.length() - 1;

            line = in.readLine();
            max_row++;
        }

        MAX_COL = max_col+1;
        MAX_ROW = max_row+1;

        goals = new char[MAX_ROW][MAX_COL];
        walls = new boolean[MAX_ROW][MAX_COL];


        initialState = new Node(null);

        for(int row = 0; row < store_contents.size(); row++){
            line = store_contents.get(row);
            for (int col = 0; col < line.length(); col++) {
                char chr = line.charAt(col);

                if (chr == '+') { // Wall.
                    walls[row][col] = true;
                } else if ('0' <= chr && chr <= '9') { // Agent.
                    initialState.agents[row][col] = chr;
                } else if ('A' <= chr && chr <= 'Z') { // Box.
                    initialState.boxes[row][col] = chr;
                } else if ('a' <= chr && chr <= 'z') { // Goal.
                    goals[row][col] = chr;
                } else if (chr == ' ') {
                    // Free space.
                } else {
                    System.err.println("Error, read invalid level character: " + (int) chr);
                    System.exit(1);
                }
            }
        }}

    public static void main( String[] args ){
        System.err.println( "Hello from Client. I am sending this using the error outputstream" );

        try{
            Client cc = new Client();
        }catch (Exception e){

        }
    }
}
