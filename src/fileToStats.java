import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.org.omg.CORBA.StructMemberHelper;
import com.sun.xml.internal.ws.util.StringUtils;

import java.io.*;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package fileToStats;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jeremy AVID
 */
public class fileToStats {

    private static Resultats resultats = null;

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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {

        String file = new String(Files.readAllBytes(Paths.get("20161125-120251-ALL.txt")));

        String roomName; String pcName; String pcIP; int envoyes; int recus; int temps_moyen; List<String> rooms;
        List<String> pcString; String room; int startFile;

        int count = file.length() - file.replace("SALLE", "").length();

        for (int i = 0; i < count; i = i + 1) {
            room = findRoom(file);
            roomName = findRoomName(room);
            pcString = findPCString(room);
            String[] pcArr = pcString.stream().toArray(String[]::new);
            for (int j = 0; j < pcArr.length; j = j + 1)
            {
                pcName = findPCName(pcArr[i]);
                pcIP = findPcIp(pcArr[i]);
                envoyes = Integer.parseInt(findSent(pcArr[i]));
                recus = Integer.parseInt(findReceived(pcArr[i]));
                temps_moyen = Integer.parseInt(findAverageTime(pcArr[i]));

                process(roomName, pcName, pcIP, envoyes, recus, temps_moyen);
            }
            file = file.replace(room,"");
        }


        resultats = new Resultats();

        //process("Salle1", "hello", "Poste2", 0, 10, 5);

        if (resultats != null && resultats.getSalles().size() > 0) {
            for (int i = 0; i < resultats.getSalles().size(); i++) {

                log("------------- Dans la salle " + resultats.getSalle(i).getName() + " ------------- ");
                for (int j = 0; j < resultats.getSalle(i).getPostes().size(); j++) {

                    log("---- " + resultats.getSalle(i).getPoste(j).getIp() + " - " + resultats.getSalle(i).getPoste(j).getName() + " ----");
                    log("Paquets envoyés : " + resultats.getSalle(i).getPoste(j).getEnvoyes() + " " +
                            "Paquets recus : " + resultats.getSalle(i).getPoste(j).getRecus() + " " +
                            "Moyenne : " + resultats.getSalle(i).getPoste(j).getTemps()
                    );
                }
            }
        }
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

        int count = room.length() - room.replace("[", "").length();

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

        int startPos;int endPos; String room;

        startPos = file.indexOf("SALLE");
        endPos = file.indexOf("SALLE", startPos);
        if (endPos != -1) {
            endPos = file.indexOf("==", startPos);
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
