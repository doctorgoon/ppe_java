import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.lang.Object;

/**
 * Created by jeremy on 18/02/2017.
 */

public class fileToStats {


    private String roomName;
    private String pcName;
    private String pcIP;
    private int[] stats = new int[3];

    class Results
    {
        List<Rooms> RoomsList = new ArrayList<Rooms>();
    }

    class Rooms
    {
        public void setRoomName(String name)
        {
            roomName = name;
        }

        public String getRoomName()
        {
            return roomName;
        }

        List<PCs> PCsList = new ArrayList<PCs>();
    }

    class PCs {

        public void setpcName(String name)
        {
            pcName = name;
        }

        public String getpcName()
        {
            return pcName;
        }

        public void setpcIP(String ip)
        {
            pcIP = ip;
        }

        public String getpcIP()
        {
            return pcIP;
        }

        public void setStats(int s, int r, int a)
        {
            stats[0] = s;
            stats[1] = r;
            stats[2] = a;
        }

        public int[] getStats()
        {
            return stats;
        }
    }


    public void loadResults(String file)
    {
        Results results = new Results();

        int roomNamePos = file.indexOf(":");

        String roomName = file.substring(roomNamePos, roomNamePos - 7);

        Rooms currentRoom = roomExists(roomName);

        if ( currentRoom != null ) {

            int pcNamePos = file.indexOf("sur") + 4;

            int ipPosS = file.indexOf("[");

            int ipPosE = file.indexOf("]");

            String pcName = file.substring(pcNamePos, ipPosS - 1);

            String pcIP = file.substring(ipPosS, ipPosE);

            PCs currentPC = pcExists(currentRoom, pcName);

            if ( currentPC != null ) {

                int posEnv0 = file.indexOf("envoyes") + 10;
                int posEnv1 = file.indexOf(", recus");
                String envoyes = file.substring(posEnv0, posEnv1);
                int posRec0 = file.indexOf("recus") + 8;
                int posRec1 = file.indexOf(", perdus");
                String recus = file.substring(posRec0, posRec1);
                int posTem0 = file.indexOf("Moyenne");
                int posTem1 = file.indexOf("ms", posTem0);
                int posNext = file.indexOf("Envoi", 10);
                if (posNext == -1) {
                    String temps_moyen = file.substring(posTem0 + 10, posTem1);
                }
                else {
                    String temps_moyen = file.substring(posTem0 + 10, posNext);


            }
        }
    }

    static String readFile(String path, Charset encoding)
            throws IOException
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public Rooms roomExists(String name)
    {
        for (int i = 0; i < Results.Rooms.size(); i++)
        {
            if ( Results.Rooms.get(i).name == name)
            {
                return Results.Rooms.get(i);
            }
        }

        return null;
    }

    public PCs pcExists(Rooms room, String name)
    {
        for (int i = 0; i < room.PCs.size(); i++)
        {
            if ( room.PCs.get(i).name == name)
            {
                return room.PCs.get(i);
            }
        }

        return null;
    }


    public static void main(String args[]){

        fileToStats fileToStats = new fileToStats();
        fileToStats.setStats("chemin.txt");
        System.out.println(fileToStats.getStats());
    }
}
