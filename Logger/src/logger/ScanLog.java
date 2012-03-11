/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package logger;

import com.eaio.stringsearch.BNDM;
import com.eaio.stringsearch.BNDMWildcards;
import com.eaio.stringsearch.BoyerMooreHorspoolRaita;
import com.eaio.stringsearch.StringSearch;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.w3c.dom.*;

/**
 *
 * @author Tiago
 */
public class ScanLog implements Runnable {

    String filename = "portugal-FINAL.telnet.xml";
    String search;
    File _file = new File(filename);
    long _updateInterval = 1000;
    private String RED = "\033[1;31m";
    private String GREEN = "\033[1;32m";
    private String YELLOW = "\033[1;33m";
    private String BLUE = "\033[1;34m";
    private String MAGENTA = "\033[1;35m";
    private String CYAN = "\033[1;36m";
    private String NONE = "\033[0m";
    private static ConcurrentHashMap productList = new ConcurrentHashMap();
    private static ConcurrentHashMap serviceList = new ConcurrentHashMap();
    private static ArrayList<Open> openList = new ArrayList();
    private static long totalProduct = 0;
    private static long totalService = 0;
    private boolean t = false;
    private static int i = 0;
    private String registo = "";
    private final String start = "<host";
    private final String end = "</host";
    private final String open = "open";
    private final String service = "hostname=\"";
    private final String product = "product=\"";
    private final String address = "addr=\"";
    //0 -> normal
    //1 -> procurar por stirng
    private static int searchMode = 0;
    BNDM b = new BNDM();
    StringSearch so = new BoyerMooreHorspoolRaita();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {

        if (args.length > 0) {
            ScanLog l = new ScanLog(args);
            l.run();
        } else {
            System.out.println("Usage: java -jar Logger.jar <file> [optional string to search]");
        }
    }

    public ScanLog(String[] args) {
        filename = args[0];
        if (args.length > 1) {
            searchMode = 1;
        }

        if (searchMode == 0) {
            search = "open";
        } else if (searchMode == 1) {
            search = args[1];
        }
    }

    private static String getTagValue(String sTag, Element eElement) {
        NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();

        Node nValue = (Node) nlList.item(0);

        return nValue.getNodeValue();
    }

    public void run() {

        try {

            int i = 0;
            long total = 0;
            System.out.println("Start processing -> " + filename);
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            String serviceFound;
            String productFound;
            String addressFound = "";
            int x = -1;
            // Continue to read lines while 
            // there are still some left to read
            while ((line = br.readLine()) != null) {
                //Process line
                if (line.length() > 0) {
                    x = so.searchChars(line.toCharArray(), start.toCharArray());
                    if (x != -1) {
//                        System.out.println("Found start");
                        registo = "";
                        while ((line = br.readLine()) != null) {
                            x = so.searchChars(line.toCharArray(), end.toCharArray());
                            if (x != -1) {

                                //Encontrar host aberto
                                x = so.searchChars(registo.toCharArray(), open.toCharArray());
                                if (x != -1) {
                                    //Encontrar product name
                                    productFound = getAttribute(registo, product);
                                    if (productFound != null) {
                                        if (productFound.isEmpty()) {
                                            productFound = "UNKOWN";
                                        }
                                        Long n = (Long) productList.get(productFound);
                                        totalProduct++;
                                        if (n != null) {
                                            n = n + 1;
                                            productList.put(productFound, n);
                                        } else {

                                            productList.put(productFound, 1L);
                                        }
                                    }

                                    addressFound = getAttribute(registo, address);

                                    //Encontrar service
                                    serviceFound = getAttribute(registo, service);
                                    if (serviceFound != null) {
                                        if (serviceFound.isEmpty()) {
                                            serviceFound = "UNKOWN";
                                        }
                                        Long n = (Long) serviceList.get(serviceFound);
                                        totalService++;
                                        if (n != null) {
                                            n = n + 1;
                                            serviceList.put(serviceFound, n);
                                        } else {

                                            serviceList.put(serviceFound, 1L);
                                        }
                                    }

                                }
//                                System.out.println(registo);
                                registo = "";
                            } else {
//                                System.out.println("Not start");
                                registo += line;
                            }
                            addressFound = "";
                        }
                    }
                }
            }

            br.close();
        } catch (Exception e) {
            System.err.println("File input error");
            e.printStackTrace();
        }

        //Listar products
        for (Object object : productList.keySet()) {
            String object1 = (String) object;
            System.out.println(object1 + " - " + productList.get(object1));
        }
        System.out.println("Total - " + totalProduct + "\n\n\n\n");


        //Listar services
        for (Object object : serviceList.keySet()) {
            String object1 = (String) object;
            System.out.println(object1 + " - " + serviceList.get(object1));
        }
        System.out.println("Total - " + totalService);
    }

    private String getAttribute(String line, String attribute) {
        String s = "";
        int x = so.searchChars(line.toCharArray(), attribute.toCharArray());
        if (x != -1) {
            String subString = registo.substring(product.length() + x, registo.length());
            s = subString.substring(0, so.searchChars(subString.toCharArray(), "\"".toCharArray()));
            return s;
        } else {
            return null;
        }
    }
    
    
    
    
    
    
    
    
    
//    private String processBDMD(String line) {
//        try {
//            System.out.println("line " + line);
//            BNDM b = new BNDM();
//            StringSearch so = new BoyerMooreHorspoolRaita();
//            String IP = "";
//            String service = "";
//            String version = "";
//            String IPRegex = "(";
//            BNDMWildcards bRegex = new BNDMWildcards();
//            Object o = bRegex.processString(IPRegex);
//            int ipLocation = bRegex.searchString(line, IPRegex, o);
//            if (ipLocation == -1) {
//            } else {
//                int ipLocationLastPos = bRegex.searchString(line, ")");
////                System.out.println("IP -> " + line.substring(ipLocation + 1, ipLocationLastPos));
//            }
////            System.out.println(ipLocation);
//            int x = so.searchChars(line.toCharArray(), search.toCharArray());
//            if (x == -1) {
//            } else {
//
//                if (line.length() > 20) {
//                    version = line.substring(21, line.length());
//                }
////            System.out.println(found);
//                total++;
//                i++;
//
//                if (version.isEmpty() || version.length() == 0) {
//                    version = "UNKOWN";
//                }
//                Long n = (Long) listaServico.get(version.trim());
//                if (n != null) {
//                    n = n + 1;
//                    listaServico.put(version.trim(), n);
//                } else {
//                    listaServico.put(version.trim(), 1L);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    private String process(String s) {
//        String returnS = "";
//        String ip = "";
//        String[] linha = s.split(" ");
//        if (linha.length > 1) {
//            if (linha[0].equals("Interesting")) {
////                System.out.println("Interesting!!");
//                ip = linha[linha.length - 1].substring(1, linha[linha.length - 1].length() - 2);
////                System.out.println(ip);
//            } else if (linha[0].equals("PORT")) {
////                System.out.println("Port!!");
//            } else {
//                String porto = "";
//                String estado = "";
//                String servico = "";
//                String fabricante = "";
////                System.out.println("linha 1 " + linha[1]);
//                if (linha[1].equals("open")) {
////                    System.out.println("Open");
//                    total++;
//                    i++;
//                    for (int i = 0; i < linha.length; i++) {
//                        String string = linha[i];
//                        if (i == 0) {
//                            porto = string;
//                        } else if (i == 1) {
//                            estado = string;
//                        } else if (i == 3) {
//                            servico = string;
//                        } else if (i > 4) {
//                            fabricante += string;
//                            if (i < linha.length) {
//                                fabricante += " ";
//                            }
//                        }
//                    }
//
//                    Open o = new Open();
//                    o.setFabricante(fabricante);
//                    o.setIp(ip);
//                    o.setPorto(porto);
//                    openList.add(o);
////                    System.out.println("porto - " + porto);
////                    System.out.println("estado - " + estado);
////                    System.out.println("servico - " + servico);
////                    System.out.println("fabricante - " + fabricante);
//                    if (fabricante.isEmpty()) {
//                        fabricante = "UNKOWN";
//                    }
//                    Long n = (Long) listaServico.get(fabricante.trim());
////            System.out.println("Adicionar fabricante - "+fabricante );
////        System.out.println("Este fabricante já tem -> " + n);
//                    if (n != null) {
//                        n = n + 1;
//                        listaServico.put(fabricante.trim(), n);
//                    } else {
////            System.out.println("Nao existe fabricante - "+fabricante);
//                        listaServico.put(fabricante.trim(), 1L);
//                    }
//
//                }
//            }
//        }
//        return returnS;
//    }
}
