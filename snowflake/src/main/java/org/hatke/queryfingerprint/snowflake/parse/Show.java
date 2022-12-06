package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

import java.io.PrintStream;
import java.util.ArrayList;

import java.util.function.Function;
import java.util.stream.Collectors;

public class Show {

    static <T>  void show(PrintStream out,
                   ImmutableCollection<T> collection,
                   ImmutableList<String> columnHeaders,
                   Function<T, String>... getters
                   ) {
        int numCols = columnHeaders.size();
        assert getters.length == numCols;
        int[] colSizes = new int[numCols];
        String[] colHeadersArr = columnHeaders.toArray(new String[columnHeaders.size()]);

        for(int i=0; i < numCols; i++) {
            colSizes[i] = colHeadersArr[i].length();
        }

        for(T elem : collection) {
            for(int i=0; i < numCols; i++) {
                colSizes[i] = Math.max(colSizes[i], getters[i].apply(elem).length());
            }
        }



        StringBuilder fmtBldr = new StringBuilder();
        for(int i=0; i < numCols; i++) {
            if (i > 0) {
                fmtBldr.append(" | ");
            }
            fmtBldr.append("%").append(i+1).append("$").append(colSizes[i]).append("s");
        }
        fmtBldr.append(" \n");
        String fmt = fmtBldr.toString();

        out.format(fmt, (Object[]) colHeadersArr);

        int lineSz = 0;
        for(int i=0; i < numCols; i++) {
            lineSz += colSizes[i];
        }
        lineSz += (3 * colSizes.length - 1);
        out.println("-".repeat(lineSz));

        String[] values = new String[numCols];
        for(T elem : collection) {
            for(int i=0; i < numCols; i++) {
                values[i] = getters[i].apply(elem);
            }
            out.format(fmt, (Object[])  values);
        }
    }

    private static class Collect {
        ArrayList<SingleQB> qbs = new ArrayList<>();
        ArrayList<CatalogTable> tables = new ArrayList<>();

        ArrayList<SourceRef> sourceRefs = new ArrayList<>();
        ArrayList<Column> columns = new ArrayList<>();

        void collect(QueryAnalysis qA) {
            for (Source s : qA.ctes().values()) {
                collect(s);
            }
            collect(qA.getTopLevelQB());
        }

        void collect(SingleQB qb) {
            if (!qbs.contains(qb)) {
                qbs.add(qb);
                for (Source s : qb.getFromSources()) {
                    collect(s);
                }
            }
        }

        void collect(SourceRef srcRef) {
            if (!sourceRefs.contains(srcRef)) {
                sourceRefs.add(srcRef);
                collect(srcRef.getSource());
            }
        }

        void collect(CatalogTable tbl) {
            if (!tables.contains(tbl)) {
                tables.add(tbl);
            }
        }

        void collect(Source src) {
            if (src instanceof SourceRef) {
                collect((SourceRef) src);
            } else if (src instanceof CatalogTable) {
                collect((CatalogTable) src);
            } else if (src instanceof SingleQB) {
                collect((SingleQB) src);
            }
            for (Column c : src.columns()) {
                collect(c);
            }
        }

        void collect(Column col) {
            if (!columns.contains(col)) {
                columns.add(col);
            }
        }
    }

    public static void show(QueryAnalysis qA, PrintStream out) {
        Collect collect = new Collect();
        collect.collect(qA);

        out.println("Single QBs:");

        show(out,
                ImmutableList.<SingleQB>copyOf(collect.qbs),
                ImmutableList.of("id", "isTopLevel", "qb_type", "parent_qb", "parent_clause",
                        "from", "columns"),
                qb -> Integer.toString(qb.getId()),
                qb -> Boolean.toString(qb.isTopLevel()),
                qb -> qb.getQbType().name(),
                qb -> qb.getParentQB().map(qb1 -> Integer.toString(qb1.getId())).orElseGet(() -> ""),
                qb -> qb.getParentClause().map(cl -> cl.name()).orElseGet(() -> ""),
                qb -> qb.getFromSources().stream().map(s -> Integer.toString(s.getId())).
                        collect(Collectors.joining(", ", "[", "]")),
                qb -> qb.getColumns().stream().map(c -> c.getName() + "(" + c.getId() + ")").
                        collect(Collectors.joining(", ", "[", "]"))
        );

        out.println("\nTables:");
        show(out,
                ImmutableList.<CatalogTable>copyOf(collect.tables),
                ImmutableList.of("id", "name", "full_name"),
                t -> Integer.toString(t.getId()),
                CatalogTable::getName,
                CatalogTable::getFqName
                );

        out.println("\nSource References:");
        show(out,
                ImmutableList.<SourceRef>copyOf(collect.sourceRefs),
                ImmutableList.of("id", "in qb", "refers", "alias", "full_alias"),
                t -> Integer.toString(t.getId()),
                t -> Integer.toString(t.getInSource().getId()),
                t -> Integer.toString(t.getSource().getId()),
                sR -> sR.getAlias().orElseGet(() -> ""),
                sR -> sR.getFqAlias().orElseGet(() -> "")
        );

        out.println("\nColumns:");
        show(out,
                ImmutableList.<Column>copyOf(collect.columns),
                ImmutableList.of("id", "name", "full_name", "source_id"),
                c -> Integer.toString(c.getId()),
                Column::getName,
                Column::getFQN,
                c -> Integer.toString(c.getSource().getId())
        );
    }


}
