/**
 * Created by jeremy
 */
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;

public class AppWindow extends JFrame {

        String filename;
        String date;
        String hour;
        String countTest;
        String countC;
        String countNC;

        String[] columnNames = {"Nom",
            "IP",
            "Recus",
            "Envoye",
            "Temps moyen"
        };

        private static Resultats resultats = null;

        JFrame frame = new JFrame("Logs Analyzer");
        JPanel container = new JPanel();

        JButton choose = new JButton("Choisir un fichier à analyser");
        AppListener mListener = new AppListener();
        JLabel lib_dateTest = new JLabel("Date du test : ");
        JTextField dateTest = new JTextField("");
        JLabel lib_heureTest = new JLabel("Heue du test : ");
        JTextField heureTest = new JTextField("");
        JLabel lib_nbTest = new JLabel("Nombre de tests : ");
        JTextField nbTest = new JTextField("");
        JLabel lib_pcC = new JLabel("Nombre de postes contactés : ");
        JTextField pcC = new JTextField("");
        JLabel lib_pcNC = new JLabel("Nombre de postes non contactés : ");
        JTextField pcNC = new JTextField("");
        JLabel wrongFile = new JLabel("FICHIER INCORRECT");

        JTextArea area = new JTextArea("");
        String salleS;
        String pcS;


        public AppWindow() {

            setTitle("Logs Analyzer 2");
            setSize(700, 125);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setLayout(null);
            add(choose); choose.setBounds(225,35,250,30);
            choose.addActionListener(mListener);

        }

        class AppListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == choose) {
                    try {
                        remove(wrongFile);
                        program();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }

        public void program() throws IOException {


                    JFileChooser fileChooser = new JFileChooser();
                    int returnValue = fileChooser.showOpenDialog(null);
                    if (returnValue == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        filename = selectedFile.getAbsolutePath();
                        if (filename != null ) {
                            log(filename);
                            try {
                                resultats = new Resultats();
                                String file = new String(Files.readAllBytes(Paths.get(filename)));
                                String roomName; String pcName; String pcIP; int envoyes; int recus; int temps_moyen; List<String> rooms;
                                List<String> pcString; String room; int roomPos; int nextPos;

                                date = getTestDate(file);
                                if (date == "null") {
                                    add(wrongFile); wrongFile.setBounds(280,70,150,20);
                                }
                                else {
                                    hour = getTestHours(file);
                                    int count = counter("SALLE", file);
                                    countTest = String.valueOf(counter("TEST N°", file));
                                    countC = String.valueOf( counter("approximative", file) / counter("TEST N°", file));
                                    countNC = String.valueOf(counter("Impossible", file) / counter("TEST N°", file));

                                    for (int i = 0; i < count; i = i + 1) {
                                        roomPos = file.indexOf("SALLE");
                                        if ( roomPos != -1 ) {
                                            room = findRoom(file);
                                            roomName = findRoomName(room);
                                            pcString = findPCString(room);
                                            for (String temp : pcString) {
                                                pcName = findPCName(temp);
                                                pcIP = findPcIp(temp);
                                                envoyes = Integer.parseInt(findSent(temp));
                                                recus = Integer.parseInt(findReceived(temp));
                                                temps_moyen = Integer.parseInt(findAverageTime(temp));
                                                process(roomName, pcName, pcIP, envoyes, recus, temps_moyen);
                                            }
                                            nextPos = file.indexOf("SALLE", roomPos + 1);
                                            if (nextPos == -1 ) {
                                                i = count;
                                            }
                                            else {
                                                file = file.substring(nextPos);
                                            }
                                        }
                                        else {
                                            i = count;
                                        }
                                    }

                                    displayResults();
                                    displayTables();
                                    setSize(700, 1000);
                                    area.setBounds(25,150, 675,875);
                                    area.setBackground(Color.decode("#EEEEEE"));
                                    area.setEditable(false);
                                    add(area);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }

            }

        }

    private void displayTables() {

        int x = 25; int y = 200; int w = 600; int h = 15;

        if (resultats != null && resultats.getSalles().size() > 0) {
            for (int i = 0; i < resultats.getSalles().size(); i++) {

                salleS = "SALLE " + resultats.getSalle(i).getName() + '\n'+ '\n';
                area.append("" + "\n");
                area.append(salleS);
                for (int j = 0; j < columnNames.length; j++) {
                    if(j == columnNames.length - 1) {
                        area.append(columnNames[j] + '\n');
                    }
                    else {
                        area.append(columnNames[j] + "                       ");
                    }
                }
                area.append("_______________________________________________________________________________________________________________________" + "\n");

                for (int j = 0; j < resultats.getSalle(i).getPostes().size(); j++) {

                    pcS = "                " + resultats.getSalle(i).getPoste(j).getIp() + " - " + resultats.getSalle(i).getPoste(j).getName() + '\n';
                    area.append(resultats.getSalle(i).getPoste(j).getName() + "            ");
                    area.append(resultats.getSalle(i).getPoste(j).getIp() + "                  ");
                    area.append(resultats.getSalle(i).getPoste(j).getEnvoyes() + "                                ");
                    area.append(resultats.getSalle(i).getPoste(j).getRecus() + "                                     ");
                    area.append(resultats.getSalle(i).getPoste(j).getTemps() + "                     " + '\n');
                }
            }
        }
    }

    private void displayResults() {
        setSize(700, 500);
        add(lib_dateTest); lib_dateTest.setBounds(25,100,100,15);
        add(dateTest); dateTest.setBounds(110,100,90,15);
        dateTest.setText(date);
        dateTest.setBackground(Color.decode("#EEEEEE"));

        add(lib_heureTest); lib_heureTest.setBounds(210,100,100,15);
        add(heureTest); heureTest.setBounds(300,100,95,15);
        heureTest.setText(hour);
        heureTest.setBackground(Color.decode("#EEEEEE"));

        add(lib_nbTest); lib_nbTest.setBounds(405,100,200,15);
        add(nbTest); nbTest.setBounds(520,100,40,15);
        nbTest.setText(countTest);
        nbTest.setBackground(Color.decode("#EEEEEE"));

        add(lib_pcC); lib_pcC.setBounds(25,125,200,15);
        add(pcC); pcC.setBounds(215,125,40,15);
        pcC.setText(countC);
        pcC.setBackground(Color.decode("#EEEEEE"));

        add(lib_pcNC); lib_pcNC.setBounds(300,125,250,15);
        add(pcNC); pcNC.setBounds(520,125,40,15);
        pcNC.setText(countNC);
        pcNC.setBackground(Color.decode("#EEEEEE"));

    }

    private static Salles laSalleExiste(String name) {
        if (resultats.salles.size() > 0) {
            for (int i = 0; i < resultats.salles.size(); i++) {
                if (resultats.salles.get(i).name.equals(name)) {
                    return resultats.salles.get(i);
                }
            }
        }
        return null;
    }

    private static Postes lePosteExiste(Salles maSalle, String name) {
        if (maSalle.getPostes().size() > 0) {
            for (int i = 0; i < maSalle.getPostes().size(); i++) {
                if (maSalle.getPostes().get(i).name.equals(name)) {;
                    return maSalle.getPostes().get(i);
                }
            }
        }
        return null;
    }

    private static int process(String nomDeLaSalle, String nomDuPoste, String ip, int envoyes, int recus, int temps) {

        // Etape 1, la salle existe-elle ?
        Salles maSalle = laSalleExiste(nomDeLaSalle);
        if (maSalle != null) {
            resultats.salles.remove(maSalle);
        } else {
            maSalle = new Salles();
            maSalle.setName(nomDeLaSalle);
        }

        // Etape 2 : Ce poste existe t-il
        Postes monPoste = lePosteExiste(maSalle, nomDuPoste);

        if (monPoste != null) {
            maSalle.getPostes().remove(monPoste);
        } else {
            monPoste = new Postes();
            monPoste.setName(nomDuPoste);
            monPoste.setIp(ip);
        }
        monPoste.setEnvoyes(envoyes);
        monPoste.setRecus(recus);
        monPoste.setTemps(temps);

        maSalle.getPostes().add(monPoste);
        resultats.salles.add(maSalle);

        return 0;
    }

    private static int counter(String key, String haystack) {

        int count = 0;
        Pattern p = Pattern.compile(key);
        Matcher m = p.matcher( haystack );
        while (m.find()) {
            count++;
        }

        return count;
    }

    private static String getTestHours(String file) {

        String testHour; int startPos; int endPos;

        startPos = file.indexOf(" à ") + 3;
        endPos = file.indexOf("SALLE", startPos);
        testHour = file.substring(startPos, endPos);

        return testHour;
    }

    private static String getTestDate(String file) {

        String testDate; int startPos; int endPos;

        startPos = file.indexOf("Le ") + 3;
        endPos = file.indexOf(" à ");
        if (startPos == -1 ||  endPos == -1 ) {
            testDate = "null";
        } else {
            testDate = file.substring(startPos, endPos);
        }


        return testDate;
    }

    private static String findAverageTime(String pcString) {

        String average_time; int startPos; int endPos;

        startPos = pcString.indexOf("Moyenne") + 9;
        endPos = pcString.indexOf("ms", startPos);

        if ( endPos == -1 ) {
            average_time = "0";
        }
        else {
            average_time = pcString.substring(startPos, endPos);
        }

        return average_time.trim();
    }

    private static String findReceived(String pcString) {

        String received; int startPos; int endPos;

        startPos = pcString.indexOf("recus") + 8;
        endPos = pcString.indexOf(", perdus");

        received = pcString.substring(startPos, endPos);

        return received;
    }

    private static String findSent(String pcString) {

        String sent; int startPos; int endPos;

        startPos = pcString.indexOf("envoyes") + 10;
        endPos = pcString.indexOf(", recus");

        sent = pcString.substring(startPos, endPos);

        return sent;
    }

    private static String findPcIp(String pcString) {

        String pcIp; int startPos; int endPos;

        startPos = pcString.indexOf("[") +1 ;
        endPos = pcString.indexOf("]");
        pcIp = pcString.substring(startPos, endPos);

        return pcIp;
    }

    private static String findPCName(String pcString) {

        String pcName; int startPos; int endPos;

        startPos = pcString.indexOf("sur") + 4;
        endPos = pcString.indexOf("[") - 1;
        pcName = pcString.substring(startPos, endPos);

        return pcName;
    }

    private static List<String> findPCString(String room) {

        List<String> pcStrings = new ArrayList<>();

        int count = 0;
        for( int i=0; i<room.length(); i++ ) {
            if( room.charAt(i) == '[' ) {
                count++;
            }
        }

        String pcString; int startPos; int endPos;

        for (int i = 0; i < count; i = i + 1 ) {
            startPos = room.indexOf("Envoi");
            endPos = room.indexOf("Envoi", startPos + 1);
            if ( endPos == -1 ) {
                pcString = room.substring(startPos);
            } else  {
                pcString = room.substring(startPos, endPos);
                room = room.substring(endPos);;
            }
            pcStrings.add(pcString);
        }

        return pcStrings;
    }

    private static String findRoomName(String room) {

        String roomName; int startPos; int endPos;

        startPos = room.indexOf("SALLE") + 6;
        endPos = room.indexOf(":", startPos);

        roomName = room.substring(startPos, endPos);

        return roomName;
    }

    private static String findRoom(String file) {

        int startPos; int endPos; String room;

        startPos = file.indexOf("SALLE");
        endPos = file.indexOf("SALLE", startPos + 1);
        if (endPos == -1) {
            endPos = file.indexOf("==");
        }

        room = file.substring(startPos, endPos);

        return room;
    }

    public static void log(String message)
    {
        System.out.println(message);
    }

    public static class Resultats {

        private List<Salles> salles = new ArrayList<Salles>();

        public List<Salles> getSalles() {
            return salles;
        }

        public Salles getSalle(int index)
        {
            return salles.get(index);
        }

    }

    public static class Salles {

        private String name;
        private List<Postes> postes = new ArrayList<Postes>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<Postes> getPostes() {
            return postes;
        }

        public Postes getPoste(int index) {
            return postes.get(index);
        }
    }

    public static class Postes {

        private String name;
        private String ip;
        private int envoyes = 0;
        private int recus = 0;
        private int temps = 0;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getEnvoyes() {
            return envoyes;
        }

        public void setEnvoyes(int envoyes) {
            this.envoyes += envoyes;
        }

        public int getRecus() {
            return recus;
        }

        public void setRecus(int recus) {
            this.recus += recus;
        }

        public int getTemps() {
            return temps;
        }

        public void setTemps(int temps) {
            this.temps += temps;
        }



    }

}
