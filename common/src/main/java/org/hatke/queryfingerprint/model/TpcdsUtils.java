package org.hatke.queryfingerprint.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TpcdsUtils {

    public static String readTpcdsQuery(String queryFileName) throws IOException {
        return readFile(String.format("queries/tpcds/%s.sql", queryFileName));
    }

    public static String readFile(String path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(path));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String text = sb.toString();
            return text;
        } finally {
            br.close();
        }
    }
}
