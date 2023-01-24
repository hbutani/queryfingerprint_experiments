package io.ad.query.search.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static org.apache.logging.log4j.core.util.Loader.getClassLoader;

public class Utils {

    public static String readTpcdsQuery(String queryFileName) throws IOException {
        return readResourceFile(String.format("queries/tpcds/%s.sql", queryFileName));
    }

    public static String readResourceFile(String path) throws IOException {
        String file = getClassLoader().getResource(path).getFile();
        BufferedReader br = new BufferedReader(new FileReader(file));
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
