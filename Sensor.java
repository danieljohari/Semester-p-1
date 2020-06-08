
// Serielport bibliotek - jssc
import jssc.*;

public class Sensor {
    private SerialPort serialPort = null; //ingen givet værdi til variablen
    //private double maaltSP02= 0.0; //Williams tanke

    public Sensor(int portnummer) { //sensor metode
        String[] portnames = null;// String array til at læse porte
        try {
            portnames = SerialPortList.getPortNames();
            serialPort = new SerialPort(portnames[portnummer]); //ud fra string array, vælger porten tilknyttet
            serialPort.openPort(); //åbner serielporten
            serialPort.setRTS(true);  // Ready To Send sat til sand
            serialPort.setDTR(true); // Data Terminal Ready sat til sand
            serialPort.setParams(115200, 8, 1, SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(serialPort.FLOWCONTROL_NONE);
        } catch (SerialPortException e) {
            e.printStackTrace();
          // Try-Catch tester coden under udførsel og fanger derefter fejlen.

        }

    }
    //OVENSTÅENDE KODE TAGET FRA IT PROJEKT, SOM INSPIRATION.

    public int[] getValue() { //getValue metoden med et heltals Arrray. metode til at hente data fra Arduino + sensor
                              //System.out.println("getValue"); //test for at se om det virker

        String result = null;
        int[] data = new int[2]; //heltals array med 2 pladser
        try {
            while (serialPort.getInputBufferBytesCount() < 14) { //så længe tegnende er under 14, gør følgende:
                try {
                    Thread.sleep(20); //fra arduino kommer de hvert 40 millis men i java 20 millisekunder.
                                                     // ellers ville man kunne miste nogle bider af data.
                } catch (InterruptedException e) {
                }
            }
                result = serialPort.readString(); //læser string fra sensor og arduino metode

            String[] results = null;
            if (result != null && result.charAt(result.length()-1)=='£') { // hvis resultatet ikke er null
                                                                           // og resultatets længde minus en
                                                                           // koden skal stoppe ved tegnet ' £ '

                //System.out.println(result); // test til at se om det virker her til

                result = result.substring(0, result.length() - 1); // resultatet går fra 0 til længden af det hele -1
                 results = result.split(" ");               // splitter vores 2 værdier op, så en rød og en IR

                //System.out.println(results.length); // test til igen at se om det virker
            }

                if (results != null && results.length >= 2) { // hvis resultatet er NOT null
                                                              // og længden er lig eller større end 2

                    //System.out.println(results[0] + " " + results[1]); // Test for at se om det virker


                    try {
                        data[0] = Integer.parseInt(results[0]); // Fra String til heltal, med data[0] / Rød
                    } catch (NumberFormatException e) {
                        data[0] = 0; // Sætter data[0] = 0, således der kan kommer ny måling ind
                    }

                    try {
                        data[1] = Integer.parseInt(results[1]); // Fra String til heltal, med data[1] / IR
                    } catch (NumberFormatException e) {
                        data[1] = 0;                        // Sætter data[1] = 0, således der kan kommer ny måling ind
                    }


                        }else { data[0] = data[1] = 0; } //Hvis if sætningen ikke er opfyldt, sæt data[0] og data[1] til 0

                        } catch (SerialPortException e) {
                             e.printStackTrace();
                          }
                               return data; // Returnere data til sidst. returnerer en Integer
                           }


    //public double getMaaltSP02() { //WILLIAM TANKE
      //  return maaltSP02;
    /* WILLIAM TANKE
                Hvis vi skal have en værdi for SP02 med fra Arduino at sammenligne med, så kan vi skrive det til serialporten fra Arduino, ved at bruge et eksisterende eksempel.
                Heraf skal vi så have med, at bufferbytecount skal stige med 4 bytes, og -1 i linje 49 skal erstattes med -5 .
                Vi laver så en substring: result.substring(result.length-5,result.length-1); og kan gemme dem som en double (konverter først fra string til double).
                Gem det som en global variabel, der kan hentes vha. getSP02

                 */
    }



