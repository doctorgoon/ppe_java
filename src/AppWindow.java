/**
 * Created by Jeremy AVID
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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class AppWindow extends JFrame {


    /*
     * VARIABLES DE CLASSES
     */
    String filename;
    String date;
    String hour;
    String countTest; // nombre de tests
    String countC; // nombre de postes contactés
    String countNC; // nombre de postes non contactés

    String[] columnNames = {"Nom",
            "IP",
            "Recus",
            "Envoyes",
            "Temps moyen"
    };

    private static Resultats resultats = null; // objet réunissant tous les résultats

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

    JTextArea area = new JTextArea(""); // zone de texte contenant les résultats détaillés par salle
    String salleS;
    String pcS;


    /*
     * INITIALISATION DE LA FENETRE
     * no param
     */
    public AppWindow() {

        setTitle("Logs Analyzer 2");
        setSize(640, 125);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        add(choose); // bouton pour rechercher un fichier
        choose.setBounds(200, 35, 250, 30);
        choose.addActionListener(mListener);

    }

    /*
     * LISTENER POUR LE BOUTON CHOOSE
     */
    class AppListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == choose) {
                try {
                    remove(wrongFile);
                    program(); // si "choose" est activé la fonction "program" est lancée
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    /*
     * FONCTION PRINCIPALE / TRAITEMENT DU FICHIER
     * no param
     */
    public void program() throws IOException {

        JFileChooser fileChooser = new JFileChooser(); // ouvre boîte de dialogue pour choisir fichier
        FileFilter filter = new FileNameExtensionFilter("TEXT FILES", "txt", "text"); // ajout filtre fichier texte
        fileChooser.addChoosableFileFilter(filter);
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filename = selectedFile.getAbsolutePath(); // récupération nom fichier + chemin absolu
            if (filename != null) {
                log(filename);
                try {
                    resultats = new Resultats(); // création instance objet résultat
                    String file = new String(Files.readAllBytes(Paths.get(filename))); // conversion du fichier en string
                    String roomName;
                    String pcName;
                    String pcIP;
                    int envoyes;
                    int recus;
                    int temps_moyen;
                    List<String> rooms; // liste découpage de substring par salles
                    List<String> pcString; // liste découpage de substring par postes
                    String room;
                    int roomPos;
                    int nextPos;

                    date = getTestDate(file);
                    if (date == "null") {
                        add(wrongFile); // récupération date échouée -> mauvais fichier
                        wrongFile.setBounds(280, 70, 150, 20);
                    } else {
                        hour = getTestHours(file);
                        int count = counter("SALLE", file);
                        countTest = String.valueOf(counter("TEST N°", file));
                        countC = String.valueOf(counter("approximative", file) / counter("TEST N°", file));
                        countNC = String.valueOf(counter("Impossible", file) / counter("TEST N°", file));

                        // boucle du nombre de fois où une salle apparaît pour recupérer les subtring des salles
                        for (int i = 0; i < count; i = i + 1) {
                            roomPos = file.indexOf("SALLE");
                            if (roomPos != -1) {
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
                                if (nextPos == -1) { // si il n'existe plus de salle dans fichier on sort de la boucle
                                    i = count;
                                } else {
                                    file = file.substring(nextPos); // sinon le fichier est amputé de la salle traitée
                                }
                            } else {
                                i = count;
                            }
                        }

                        displayResults();
                        displayTables();
                        setSize(640, 1000);
                        area.setBounds(25, 150, 675, 875);
                        area.setBackground(Color.decode("#EEEEEE"));
                        area.setEditable(false);
                        add(area); // ajout de la zone de texte
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }


    /**
     * FONCTION GERANT L'AFFICHAGE DES RESULTATS DANS LA ZONE DE TEXTE
     * no param
     */
    private void displayTables() {

        if (resultats != null && resultats.getSalles().size() > 0) {

            // boucle à travers les différentes salles stockées dans resultats
            for (int i = 0; i < resultats.getSalles().size(); i++) {

                salleS = "SALLE " + resultats.getSalle(i).getName() + '\n' + '\n'; // récupération nom de la salle
                area.append("" + "\n" + "\n"); // saut de ligne
                area.append(salleS); // ajout de la string du nom de la salle dans la zone de texte

                // boucle permettant d'afficher les entêtes du tableu
                for (int j = 0; j < columnNames.length; j++) {
                    if (j == columnNames.length - 1) {
                        area.append(columnNames[j] + '\n');
                    } else {
                        area.append(columnNames[j] + "                       ");
                    }
                }
                area.append("____________________________________________________________________________________" + "\n");

                // boucle à travers les différents postes dans la salle donnée
                for (int j = 0; j < resultats.getSalle(i).getPostes().size(); j++) {

                    area.append(resultats.getSalle(i).getPoste(j).getName() + "            "); // affichage nom du poste
                    area.append(resultats.getSalle(i).getPoste(j).getIp() + "                  "); // affichage adresse IP
                    area.append(resultats.getSalle(i).getPoste(j).getEnvoyes() + "                                "); // affichage paquets envoyés
                    area.append(resultats.getSalle(i).getPoste(j).getRecus() + "                                     "); // affichage paquets recus
                    area.append(resultats.getSalle(i).getPoste(j).getTemps() + "                     " + '\n'); // affichage temps moyen
                }
            }
        }
    }

    /**
     * FONCTION GERANT L'AFFICHAGE DE RESULTATS DANS DES JFIELDS
     * no param
     */
    private void displayResults() {

        setSize(700, 500);

        // date du premier test
        add(lib_dateTest);
        lib_dateTest.setBounds(25, 100, 100, 15);
        add(dateTest);
        dateTest.setBounds(110, 100, 90, 15);
        dateTest.setText(date);
        dateTest.setBackground(Color.decode("#EEEEEE"));

        // heure du premier test
        add(lib_heureTest);
        lib_heureTest.setBounds(210, 100, 100, 15);
        add(heureTest);
        heureTest.setBounds(300, 100, 95, 15);
        heureTest.setText(hour);
        heureTest.setBackground(Color.decode("#EEEEEE"));

        // nombre de test en tout
        add(lib_nbTest);
        lib_nbTest.setBounds(405, 100, 200, 15);
        add(nbTest);
        nbTest.setBounds(520, 100, 40, 15);
        nbTest.setText(countTest);
        nbTest.setBackground(Color.decode("#EEEEEE"));

        // nombre de PC contactés
        add(lib_pcC);
        lib_pcC.setBounds(25, 125, 200, 15);
        add(pcC);
        pcC.setBounds(215, 125, 40, 15);
        pcC.setText(countC);
        pcC.setBackground(Color.decode("#EEEEEE"));

        // nombre de PC non contactés
        add(lib_pcNC);
        lib_pcNC.setBounds(300, 125, 250, 15);
        add(pcNC);
        pcNC.setBounds(520, 125, 40, 15);
        pcNC.setText(countNC);
        pcNC.setBackground(Color.decode("#EEEEEE"));
    }

    /**
     * FONCTION VERIFIANT SI LA SALLE EXISTE DEJA DANS RESULTATS
     * @param name -> string nom de la salle
     * @return
     */
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


    /**
     * FONCTION VERIFIANT SI LE POSTE EXISTE DEJA DANS LA SALLE
     * @param maSalle -> objet salle
     * @param name -> string nom du poste
     * @return
     */
    private static Postes lePosteExiste(Salles maSalle, String name) {
        if (maSalle.getPostes().size() > 0) {
            for (int i = 0; i < maSalle.getPostes().size(); i++) {
                if (maSalle.getPostes().get(i).name.equals(name)) {
                    ;
                    return maSalle.getPostes().get(i);
                }
            }
        }
        return null;
    }


    /**
     * FONCTION REMPLISSANT LES OBJETS POSTES -> SALLES -> RESULTATS
     * @param nomDeLaSalle
     * @param nomDuPoste
     * @param ip
     * @param envoyes
     * @param recus
     * @param temps
     * @return
     */
    private static int process(String nomDeLaSalle, String nomDuPoste, String ip, int envoyes, int recus, int temps) {

        // Etape 1, la salle existe-elle ?
        Salles maSalle = laSalleExiste(nomDeLaSalle);
        if (maSalle != null) {
            resultats.salles.remove(maSalle);
        } else {
            maSalle = new Salles();
            maSalle.setName(nomDeLaSalle);
        }

        // Etape 2 : Ce poste existe t-il ?
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

    /**
     * FONCTION PERMETTANT DE COMPTER LE NOMBRE D'APPARITION D'UNE SUBSTRING DANS UNE STRING
     * @param key
     * @param haystack
     * @return
     */
    private static int counter(String key, String haystack) {

        int count = 0;
        Pattern p = Pattern.compile(key);
        Matcher m = p.matcher(haystack);
        while (m.find()) {
            count++;
        }

        return count;
    }

    /**
     * FONCTION RECUPERANT L'HEURE DU PREMIER TEST
     * @param file
     * @return
     */
    private static String getTestHours(String file) {

        String testHour;
        int startPos;
        int endPos;

        startPos = file.indexOf(" à ") + 3;
        endPos = file.indexOf("SALLE", startPos);
        testHour = file.substring(startPos, endPos);

        return testHour;
    }

    /**
     * FONCTION RECUPERANT LA DATE DU PREMIER TEST
     * @param file
     * @return
     */
    private static String getTestDate(String file) {

        String testDate;
        int startPos;
        int endPos;

        startPos = file.indexOf("Le ") + 3;
        endPos = file.indexOf(" à ");
        if (startPos == -1 || endPos == -1) {
            testDate = "null";
        } else {
            testDate = file.substring(startPos, endPos);
        }


        return testDate;
    }

    /**
     * FONCTION RECUPERANT LE TEMPS MOYEN A PARTIR DE LA STRING D'UN PC
     * @param pcString
     * @return
     */
    private static String findAverageTime(String pcString) {

        String average_time;
        int startPos;
        int endPos;

        startPos = pcString.indexOf("Moyenne") + 9;
        endPos = pcString.indexOf("ms", startPos);

        if (endPos == -1) { // si le pc n'est pas contacté, il n'y pas de stats, donc le mot "moyenne" n'apparait pas
            average_time = "0";
        } else {
            average_time = pcString.substring(startPos, endPos);
        }

        return average_time.trim();
    }

    /**
     * FONCTION RECUPERANT LE NOMBRE DE PAQUET RECUS A PARTIR DE LA STRING D'UN PC
     * @param pcString
     * @return
     */
    private static String findReceived(String pcString) {

        String received;
        int startPos;
        int endPos;

        startPos = pcString.indexOf("recus") + 8;
        endPos = pcString.indexOf(", perdus");

        received = pcString.substring(startPos, endPos);

        return received;
    }

    /**
     * FONCTION RECUPERANT LE NOMBRE DE PAQUET ENVOYES A PARTIR DE LA STRING D'UN PC
     * @param pcString
     * @return
     */
    private static String findSent(String pcString) {

        String sent;
        int startPos;
        int endPos;

        startPos = pcString.indexOf("envoyes") + 10;
        endPos = pcString.indexOf(", recus");

        sent = pcString.substring(startPos, endPos);

        return sent;
    }

    /**
     * FONCTION RECUPERANT L'ADRESSE IP DU POSTE A PARTIR DE LA STRING D'UN PC
     * @param pcString
     * @return
     */
    private static String findPcIp(String pcString) {

        String pcIp;
        int startPos;
        int endPos;

        startPos = pcString.indexOf("[") + 1;
        endPos = pcString.indexOf("]");
        pcIp = pcString.substring(startPos, endPos);

        return pcIp;
    }

    /**
     * FONCTION RECUPERANT LE NOM DU POSTE A PARTIR DE LA STRING D'UN PC
     * @param pcString
     * @return
     */
    private static String findPCName(String pcString) {

        String pcName;
        int startPos;
        int endPos;

        startPos = pcString.indexOf("sur") + 4;
        endPos = pcString.indexOf("[") - 1;
        pcName = pcString.substring(startPos, endPos);

        return pcName;
    }

    /**
     * FONCTION RECUPPERANT LES SUBSTRING DE CHAQUE PC POUR UNE SALLE DONNEE
     * @param room
     * @return List de String comportant les substring de chaque PC
     */
    private static List<String> findPCString(String room) {

        List<String> pcStrings = new ArrayList<>();

        int count = 0;
        // boucle déterminant le nombre de fois où une adresse IP est citée, donc nombre de PC
        for (int i = 0; i < room.length(); i++) {
            if (room.charAt(i) == '[') {
                count++;
            }
        }

        String pcString;
        int startPos;
        int endPos;

        for (int i = 0; i < count; i = i + 1) {
            startPos = room.indexOf("Envoi"); // début de la string d'un PC
            endPos = room.indexOf("Envoi", startPos + 1); // fin de la string = début de celle du prochain PC
            if (endPos == -1) { // si pas de prochain PC
                pcString = room.substring(startPos); // la string du PC va jusqu'à la fin du fichier
            } else {
                pcString = room.substring(startPos, endPos);
                room = room.substring(endPos); // la string de la salle est amputée de la substring du PC
            }
            pcStrings.add(pcString); // ajout dans la liste de la substring du PC
        }

        return pcStrings;
    }

    /**
     * FONCTION RECUPPERANT LE NOM DE LA SALLE
     * @param room -> subtring d'une salle
     * @return
     */
    private static String findRoomName(String room) {

        String roomName;
        int startPos;
        int endPos;

        startPos = room.indexOf("SALLE") + 6;
        endPos = room.indexOf(":", startPos);

        roomName = room.substring(startPos, endPos);

        return roomName;
    }


    /**
     * FONCTION RECUPERRANT UNE SUBSTRING D'UNE SALLE A PARTIR DE LA STRING DU FICHIER DANS SA GLOBALITE
     * @param file
     * @return
     */
    private static String findRoom(String file) {

        int startPos;
        int endPos;
        String room;

        startPos = file.indexOf("SALLE");
        endPos = file.indexOf("SALLE", startPos + 1);
        if (endPos == -1) {
            endPos = file.indexOf("==");
        }

        room = file.substring(startPos, endPos);

        return room;
    }


    /**
     * FONCTION SE SUBSTITUANT AU FASTIDIEUX System.out.println
     * @param message
     */
    public static void log(String message) {
        System.out.println(message);
    }


    /**
     * CLASSE RESULTATS COMPORTANT UNE LISTE DE SALLES
     */
    public static class Resultats {

        private List<Salles> salles = new ArrayList<Salles>();

        public List<Salles> getSalles() {
            return salles;
        }

        public Salles getSalle(int index) {
            return salles.get(index);
        }

    }

    /**
     * CLASSE SALLES COMPORTANT UNE LISTE DE POSTES
     */
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


    /**
     * CLASSE POSTES COMPORTANT NOM, IP, ENVOYES, RECUS, TEMPS MOYEN
     */
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
