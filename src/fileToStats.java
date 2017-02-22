import com.sun.org.apache.xpath.internal.operations.Bool;

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
        // DEBUT DU PROGRAMME

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
        //log(file);
        int roomNamePosS; int roomNamePosE; String roomName; int pcNamePos; int ipPosS; int ipPosE;
        String pcName; String pcIP; int posEnv0; int posEnv1; String envoyes; int posRec0; int posRec1;
        String recus; int posTem0; int posTem1; int posNext; String temps_moyen; Boolean endOfFile = false;

        while (!endOfFile) {
            roomNamePosS = file.indexOf("SALLE");
            roomNamePosE = file.indexOf(":", roomNamePosS);
            if (roomNamePosS != -1) {
                roomName = file.substring(roomNamePosS, roomNamePosE);
            }
            pcNamePos = file.indexOf("sur") + 4;
            ipPosS = file.indexOf("[") +1 ;
            ipPosE = file.indexOf("]");
            pcName = file.substring(pcNamePos, ipPosS - 1);
            pcIP = file.substring(ipPosS, ipPosE);
            posEnv0 = file.indexOf("envoyes") + 10;
            posEnv1 = file.indexOf(", recus");
            envoyes = file.substring(posEnv0, posEnv1);
            posRec0 = file.indexOf("recus") + 8;
            posRec1 = file.indexOf(", perdus");
            recus = file.substring(posRec0, posRec1);
            posTem0 = file.indexOf("Moyenne");
            posTem1 = file.indexOf("ms", posTem0);
            posNext = file.indexOf("Envoi", 10);
            if (posNext == -1) {
                endOfFile = true;
                temps_moyen = file.substring(posTem0 + 10, posTem1);
            }
            else {
                //temps_moyen = file.substring(posTem0 + 10, posNext);
                temps_moyen = file.substring(posTem0 + 10, posTem1);
            }

            file = file.substring(posEnv0, posEnv1);

            process(roomName, pcName, pcIP, Integer.parseInt(envoyes), Integer.parseInt(recus), Integer.parseInt(temps_moyen));
        }


        resultats = new Resultats();


        //process("Salle1", "Poste2", 0, 10);
        //process("Salle2", "Poste1", 5, 0);
        //process("Salle1", "Poste1", 5, 5);
        // A la fin
        // Je lis les salles
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
