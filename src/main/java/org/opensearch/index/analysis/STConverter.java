package org.opensearch.index.analysis;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * some parts of code copied from:http://code.google.com/p/java-zhconverter/
 */
public class STConverter {
    
    private static final Logger LOGGER = Logger.getLogger(STConverter.class.getName());

    private Properties charMap = new Properties();
    private Properties revCharMap = new Properties();
    private Set<String> conflictingSets  = new HashSet<String>();
    private static STConverter instance=new STConverter();
    
    public STConverter(){


        InputStream file1 = null;
        file1 = this.getClass().getResourceAsStream("/t2s.properties");
        InputStreamReader is = null;
        try {
            is = new InputStreamReader(file1, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOGGER.log(Level.SEVERE, "Unsupported character encoding " + e.getMessage(), e);
        }
        if (is != null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(is);
                charMap.load(reader);
            } catch (FileNotFoundException e) {
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "IOException in loading charMap: " + e.getMessage(), e);
            } finally {
                try {
                    if (reader != null)
                        reader.close();
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        initializeHelper();
    }

    private void initializeHelper() {

        Map stringPossibilities = new HashMap();
        Iterator iter = charMap.keySet().iterator();
        while (iter.hasNext()) {

            //fill revmap
            String key = (String) iter.next();
            revCharMap.put(charMap.get(key),key);

            if (key.length() >= 1) {

                for (int i = 0; i < (key.length()); i++) {
                    String keySubstring = key.substring(0, i + 1);
                    if (stringPossibilities.containsKey(keySubstring)) {
                        Integer integer = (Integer)(stringPossibilities.get(keySubstring));
                        stringPossibilities.put(keySubstring, Integer.valueOf(integer) + 1);

                    } else {
                        stringPossibilities.put(keySubstring, 1);
                    }

                }
            }
        }

        iter = stringPossibilities.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            int value = (Integer)stringPossibilities.get(key);
            if (value > 1) {
                conflictingSets.add(key);
            }
        }
    }

    public String convert(STConvertType type,String in) {
           Map<Object, Object> map=charMap;
        if(type== STConvertType.SIMPLE_2_TRADITIONAL){
            map=revCharMap;
        }

        StringBuilder target = new StringBuilder();
        StringBuilder source = new StringBuilder();

        for (int i = 0; i < in.length(); i++) {

            char c = in.charAt(i);
            String key = Character.toString(c);
            source.append(key);

            if (conflictingSets.contains(source.toString())) {
            } else if (map.containsKey(source.toString())) {
                target.append(map.get(source.toString()));
                source.setLength(0);
            } else {
                CharSequence sequence = source.subSequence(0, source.length()-1);
                source.delete(0, source.length()-1);
                mapping(map, target, new StringBuilder(sequence));
            }
        }

        mapping(map, target, source);

        return target.toString();
    }

    public static STConverter getInstance(){
        if(instance==null){instance = new STConverter();}
        return instance;
    }

    public String convert(String text, STConvertType converterType) {
        return getInstance().convert(converterType,text);
    }

    private static void mapping(Map<Object, Object> map, StringBuilder outString, StringBuilder stackString) {
        while (stackString.length() > 0){
            if (map.containsKey(stackString.toString())) {
                outString.append(map.get(stackString.toString()));
                stackString.setLength(0);

            } else {
                outString.append(Character.toString( stackString.charAt(0)) );
                stackString.delete(0, 1);
            }

        }
    }

}

