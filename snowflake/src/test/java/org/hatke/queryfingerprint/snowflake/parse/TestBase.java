package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.sqlenv.TSQLEnv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TestBase {
    protected static TSQLEnv sqlEnv = new TPCDSSQLEnv(EDbVendor.dbvsnowflake);

    public String readResourceFile(String path) throws IOException {
        String file = this.getClass().getClassLoader().getResource(path).getFile();
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
