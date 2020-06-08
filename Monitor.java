import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Monitor {


    private Sensor sensor;
    private static FileWriter fw;
    private static BufferedWriter bw;
    private static PrintWriter pw;


    public static void main(String[] args) throws InterruptedException, IOException {

        fw = new FileWriter("målinger.txt", true);
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw, true);

        new Monitor().start();

        //Ovenstående kode er hentet fra IT projektet.

    }

    private int start() {

        sensor = new Sensor(0);

        int[] irArray = new int[100];
        int[] redArray = new int[100];
        // Array / tabel af 100 målinger.

        int count = 0;



        while (true) {

            int[] data = sensor.getValue();
            irArray[count] = data[1];
            redArray[count] = data[0];
            // Gemmer de målte data i et Array.

            count++;

            //System.out.println(count); //test

            if (count >= 100) {
                double SPO2 = calcSPO2(irArray, redArray, count, data); //Metode hvor koden til udregning af SPO2 findes i
                double bpm = calcBPM(irArray); // Metode hvor koden til udregning af BPM findes i
                bpm = Math.floor(bpm*100d/100d);
                SPO2 = Math.floor(SPO2*1e5/1e5);
                System.out.println("SPO2: " + SPO2 + "%");
                System.out.println("BPM: " + bpm);
                System.out.println(" ");
                count = 0;

            }

        }
    }


    //BPM
    private double calcBPM(int[] irArray) { // Metoden for vores kalkulering af BPM
        int[] diffArray = new int[irArray.length - 1]; // længden af vores array -1, fordi den sidste måling i arrayet ikke kan tages den 1. afledte af.
        int bcount = 0;
        int firstSlope= 0;
        int lastSlope = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        //max = min og min = max, fordi vi bruger graferne for den første afledte. derfor er den omvendt
        //System.out.println(max + " " + min);
        for (int i = 0; i < diffArray.length; i++){
            diffArray[i] = irArray[i+1]-irArray[i]; //første afledte
            if(diffArray[i] < min) { //bliver vores afledte under den nye minimum,
                min = diffArray[i];
            }
            if (diffArray[i] > max) {
                max = diffArray[i];
            }
        }
        double grænse = 0.6 * min + 0.4 * max; //midddelværdien i bølgen

        //System.out.println("G: "grænse);

        boolean first = false; // set første til falsk
        for (int i =1; i < diffArray.length; i++){
            if (diffArray[i] < grænse && diffArray[i-1] >= grænse) {
                if (!first) {
                    firstSlope = i; // sætter første søjle til antal talte.
                    first = true;
                } else {
                    lastSlope = i; //sidste søjle til antal talte og tæller antal slag op.
                    bcount++;

                }
            }

        }
        double secsElapsed =  (lastSlope-firstSlope) * 0.04; // sekunder gået ved 100 målinger fra første til sidste søjle.
       // System.out.println(secsElapsed);
        double tidSlag = bcount/secsElapsed; //slag pr tid
        //System.out.println(tidSlag);
        double bpm = tidSlag * 60; // tid pr slag, ganget med 60 for minutter

        return bpm;
    }


    //SPO2
    private double calcSPO2(int[] irArray, int[] redArray, int count, int[] data) {
        int sumIR = 0;
        int sumR = 0;

        for (int i = 0; i < count; i++) {
            sumIR += irArray[i];
            sumR += redArray[i];
            // sum af 100 målinger, både for rød og IR
            pw.println("IR rådata: " + irArray[i] + " Rød rådata: " + redArray[i]);




        }

        //System.out.println(sumIR + " " + sumR); //test

        double aveIR = (double)sumIR / count; //int til double pga størrelse
        double aveR = (double)sumR / count;
        //Gennemsnittet af de 100 målinger



        //System.out.println(aveR + " " + aveIR); //test

        double AR = 0;
        double AIR = 0;

        //System.out.println(aveIR); //test

        for (int i = 0; i < 100; i++) {

            AR += (redArray[i] - aveR) * (redArray[i] - aveR);
            AIR += (irArray[i] - aveIR) * (irArray[i] - aveIR);
            // For løkker: for at kunne regne summen af 100 AC værdi målinger

            //System.out.println(AR + " " + AIR); //test

        }


        double ACR = AR/count;
        double ACIR = AIR/count;
        // Gennemsnittet af AC værdien ^2.

        //System.out.println("G: " + ACR  + " " + ACIR); //test

        double rmsR = Math.sqrt(ACR);
        double rmsIR = Math.sqrt(ACIR);
        //kvadratroden af gennemsnittet af de 100 målinger i ^2.  = RMS - root means square
        // RMS fordi der både er positive og negative. Denne måde bliver det positivt.

        //System.out.println("SQRT: " + rmsR + " " + rmsIR); // test


        double R = ((rmsR / aveR) / (rmsIR / aveIR));
        // Forholdet mellem rød og IR  ---- ac og dc værdi

         //System.out.println("R: " + R); //test
        pw.println();
        pw.println("Sum IR: " +sumIR+ " Sum Rød: " +sumR);
        pw.println();
        pw.println("Gennemsnit IR: " + aveIR + " Gennemsnit Rød: " +aveR);
        pw.println();
        pw.println("RMS IR: " + rmsIR+ " RMS Rød: " +rmsR);
        pw.println();
        pw.println("Ratio - R: " +R);
        pw.println();

        // gem i fil rå data. udskrift er SPo2 og bpm sker i terminalen.

        double a = -45.060;
        double b = 30.354;
        double c = 94.845;
        // a , b og c er fundet fra SparkFun bilbioteket. konstante koefficienter for sensor MAX30102

       double SPO = (a * R * R) + (b * R) + c;

        //double SPO2 = 110 - 25 * R; //anden måde at regne SPO2
        //System.out.println("SPO1: " + SPO +  " SPO2: " + SPO2);



        count = 0;


        return SPO;

          /* Formel:
                DC værdi = Gennemsnitsværdi af 100 målinger, både for rød og IR --- Det er knogler, væv osv. Fx alt der er konstant
                AC værdi =  Nuværende måling - (minus) - gennemsnitsværdi ------- både for RØD OG IR ----- Blodstrømmen, vekslende måling.

                Steppet hedder AC.RMS: RMS = Root means square
                Sæt AC værdi ^2 for hver af de 100 målinger ----- rød og IR
                ACRød og ACIR = Tag kvadratroden af AC^2 værdien. så får man AC.RMS
                Step færdigt.

                R = Forholdet mellem Rød og Infrarød

                DC.måling = gennemsnittet af foretagne målinger i et array fx, 100 målinger

                R = (AC.RMS (rød) / DC.måling (rød)) / (AC.RMS (IR) / DC.måling (IR))

                SPO2 = a * R^2 + b * R + c
                SPO2 kan også regnes som: SPO2 = 100-25 * R

                andengrads polynomium med faste koefficienter.
             */

            /*
            Udregning for SPO2 niveau. Fundet fra SparkFun bibliotek i arduino koden
              a, b o c værdier fundet i SparkFun SPO2 algorithme bibliotek, åbnet fra notepad.
             */
    }
}
