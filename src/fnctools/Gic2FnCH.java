/* @author Leonardo Gutiérrez Ramírez <leogutierrezramirez.gmail.com> */
/* Dec 17, 2011 */

package fnctools;

import java.util.ArrayList;
import java.util.LinkedHashMap;

// Regex:
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Algoritmo que teniendo una cadena en la forma
 * S->a|aaB|Bab, devuelve la forma normal de Chomsky correspondiente.
 * @author Leonardo Gutiérrez Ramírez | leogutierrezramirez@gmail.com <a href="mailto:leogutierrezramirez@gmail.com">Leonardo Gutiérrez Ramírez</a>
 * @version 1.0
 */
public class Gic2FnCH {
    
    private LinkedHashMap<String, Integer> map = new LinkedHashMap<String, Integer>();
    private final String regex = "(\\{\\w+\\})";
    private int count = 0;

    private StringBuilder chomsky;
    private String parteA;
    private String parteB;
    private StringBuilder finalS;
    private ArrayList<String> chomskyList = null;
    private ArrayList<String> elementos = null;
    private String _gic;

    public LinkedHashMap<String, Integer> getMap() {
        return map;
    }
    
    /**
     * Constructor predeterminado.
     */
    public Gic2FnCH() {
        chomsky = new StringBuilder("");
        finalS = new StringBuilder("");
        parteA = "";
        parteB = "";
        chomskyList = new ArrayList<String>();
        elementos = new ArrayList<String>();
        // Limpiar el map:
        map.clear();
        count = 0;
    }
    
    /**
     * El siguiente método genera la forma normal de Chomsky basado en un String.
     * Para ver el resultado ejecute el método getChomskyForm().
     * 
     * {.*} - Simbolo NO Terminal no definido, es decir, que no tiene FNCH
     * [a-z] - Terminal
     * 
     * @param gic - GIC string.
     */
    public void generate(String gic) {
        
        gic = gic.trim();
        _gic = gic.trim();
        
        elementos.clear();
        
        chomsky.delete(0, chomsky.length());
        
        ArrayList<Elemento> list = new ArrayList<Elemento>();
        ArrayList<String> definidos = new ArrayList<String>();
        Tabla tabla = new Tabla();
        
        for(String s : Tools.getCases(gic).split("\\|")) {
            
            if(Tools.isChomsky(s)) {
                chomsky.append(s).append("|");
                elementos.add(s);
            } else {
                parteA = s.substring(0, s.length()/2);
                parteB = s.substring(s.length()/2, s.length());
                
                if(Tools.isTerminal(parteA)) {
                    
                    chomsky.append(parteA);
                    elementos.add(parteA);
                    
                } else {
                    
                    String _s = "{" + parteA + "}";
                    
                    chomsky.append(_s);
                    elementos.add(_s);
                    if(!tabla.getTabla().containsKey(_s)) {
                        tabla.put(_s, new Elemento(_s, false));
                        list.add(new Elemento(_s, false));
                    }
                    
                }
                
                if(Tools.isTerminal(parteB)) {
                    chomsky.append(parteB).append("|");
                    elementos.add(parteB);
                } else {
                    
                    String _s = "{" + parteB + "}";
                    
                    chomsky.append(_s);
                    elementos.add(_s);
                    
                    if(!tabla.getTabla().containsKey(_s)) {
                        tabla.put(_s, new Elemento(_s, false));
                        list.add(new Elemento(_s, false));
                    }
                }
            }
        }
        
        StringBuilder temp = new StringBuilder(chomsky.substring(0, chomsky.length() - 1));
        chomsky.delete(0, chomsky.length());
        chomsky.append(temp.toString());
        
        setChomsky(Tools.getNameProduction(gic) + " --> " + chomsky);
        
        int i = 0;
        while(isListFull(list) == false) {
            
                finalS.delete(0, finalS.length());
                parteA = "";
                parteB = "";
            
                if(Tools.isChomsky(Tools.getStringBtwn(list.get(i).getChomskyStr()))) {
                    definidos.add(list.get(i).getChomskyStr() + "->" + Tools.getStringBtwn(list.get(i).getChomskyStr()));
                    list.get(i).setDefined(true);
                
                } else {
                
                    list.get(i).setDefined(true);
                    parteA = Tools.getStringBtwn(list.get(i).getChomskyStr()).substring(0, Tools.getStringBtwn(list.get(i).getChomskyStr()).length() / 2);
                    parteB = Tools.getStringBtwn(list.get(i).getChomskyStr()).substring(Tools.getStringBtwn(list.get(i).getChomskyStr()).length() / 2, Tools.getStringBtwn(list.get(i).getChomskyStr()).length());
                    
                    if(Tools.isTerminal(parteA)) {
                        finalS.append(parteA);
                    } else {
                        
                        finalS.append("{").append(parteA).append("}");
                        
                        if(!tabla.getTabla().containsKey("{" + parteA + "}")) {
                        
                            tabla.put("{" + parteA + "}", new Elemento("{" + parteA + "}", false));
                            list.add(new Elemento("{" + parteA + "}", false));
                            
                        }
                    }
                
                    if(Tools.isTerminal(parteB)) {
                        finalS.append(parteB);
                    } else {
                        
                        finalS.append("{").append(parteB).append("}");
                        
                        if(!tabla.getTabla().containsKey("{" + parteB + "}")) {
                            
                            tabla.put("{" + parteB + "}", new Elemento("{" + parteB + "}", false));
                            list.add(new Elemento("{" + parteB + "}", false));
                            
                        }
                    }
                    
                    definidos.add(list.get(i).getChomskyStr() + "->" + finalS);
                    
                }
            i = getIndex(list);
        }
        setChomskyList(definidos);
    }
    
    private void setChomsky(String s) {
        this.chomsky = new StringBuilder(s);
    }
    
    private void setChomskyList(ArrayList<String> definidos) {
        this.chomskyList = definidos;
    }
    
    /**
     * Devuelve las definiciones basándose en la forma normal de chomsky generada
     * @return ArrayList<String>
     */
    public ArrayList<String> getResultProductions() {
        ArrayList<String> temp = new ArrayList<String>();
        for (String s : this.chomskyList) {
                String regex = "(\\{\\w+\\})";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(s);
                
                while(m.find()) {
                    String symbol = m.group(1);
                    if(!map.containsKey(symbol)) {
                        map.put(symbol, ++count);
                    } 
                    s = s.replace(symbol, "X" + map.get(symbol));
                }
                temp.add(s);
            }
        return temp;
    }
    
    /**
     * Devuelve la forma normal de chomsky después de haberla generado con el método generate().
     * @return String
     */
    public String getChomskyForm() {
        return chomsky.toString();
    }
    
    private static boolean isListFull(ArrayList<Elemento> lista) {
        for(byte i = 0; i < lista.size(); i++) {
            if(lista.get(i).isDefined() == false) {
                return false;
            }
        }
        return true;
    }
    
    private static int getIndex(ArrayList<Elemento> lista) {
        for(byte i = 0; i < lista.size(); i++) {
            if(lista.get(i).isDefined() == false) {
                return i;
            }
        }
        return -1;
    }
    
    public String getFNCH() {
        String fnc = "";
        int c = 0;
        for(String s : elementos) {
            
            if(s.contains("{")) {
                if(map.containsKey(s)) {
                    s = "X" + map.get(s);
                } else {
                    map.put(s, ++count);
                    s = "X" + map.get(s);
                }
            }
            
            if(Tools.isTerminalLower(s)) {
                fnc += s + "|";
                c = 0;
            } else {
                fnc += s;
                c++;
                if(c == 2) {
                    fnc += "|";
                    c = 0;
                }
            }
        }
        if(fnc.substring(fnc.length() - 1).equals("|")) {
            String s = Tools.getNameProduction(_gic) + " -> " + fnc.substring(0, fnc.length() - 1);
            return s;
        }
        return Tools.getNameProduction(_gic) + " -> " + fnc;
    }
    
}
class Only {
    private static final Only INSTANCE;
    static {
        INSTANCE = new Only();
    }
    private Only() {}
    public static final Only getInstace() {
        return INSTANCE;
    }
}