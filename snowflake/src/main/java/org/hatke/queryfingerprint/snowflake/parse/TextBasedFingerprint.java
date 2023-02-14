package org.hatke.queryfingerprint.snowflake.parse;

import gudusoft.gsqlparser.EDbVendor;
import gudusoft.gsqlparser.ETokenType;
import gudusoft.gsqlparser.TGSqlParser;
import gudusoft.gsqlparser.TSourceToken;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class TextBasedFingerprint {

    public String generate(EDbVendor vendor, String sql) throws Exception {
        TGSqlParser tgSqlParser = new TGSqlParser(vendor);
        return generate(tgSqlParser, sql);
    }

    public String generate(TGSqlParser tgSqlParser, String sql) throws Exception {
        tgSqlParser.sqltext = sql;
        if (tgSqlParser.parse() != 0) {
            throw new Exception("Cannot parse statement " + tgSqlParser.getErrormessage());
        }
        for (int i = 0; i < tgSqlParser.sourcetokenlist.size(); i++) {
            TSourceToken st = tgSqlParser.sourcetokenlist.get(i);
            if (st.tokentype == ETokenType.ttwhitespace ||
                    st.tokentype == ETokenType.ttreturn ||
                    st.tokentype == ETokenType.ttsemicolon
            ) {
                st.astext = "";
            }
            if ((st.tokentype == ETokenType.ttnumber)
                    || (st.tokentype == ETokenType.ttsqstring)
            ) {
                st.astext = "#";
            }
        }
        try {
            String transformed = tgSqlParser.sqlstatements.get(0).toString();
            return hash(transformed);
        } catch (Throwable th) {
            throw new Exception("Cannot fingerprint statement", th);
        }
    }


    private String hash(String text) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] bytesOfMessage = text.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(bytesOfMessage);
        return String.format("%032x", new BigInteger(1, digest));
    }
}
