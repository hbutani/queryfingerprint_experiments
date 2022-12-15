package org.hatke.queryfingerprint.snowflake.parse;

import com.google.common.collect.ImmutableList;

import java.util.Optional;

public interface Column extends ColumnRef {

    int getId();

    String getName();
    String getFQN();

    Source getSource();

    default Optional<QB> appearsInQB() {
        Source s = getSource();
        if (s instanceof QB) {
            return Optional.of((QB) s);
        } else {
            return Optional.empty();
        }
    }

    default Column getColumn() {
        return this;
    }

    default boolean isCorrelated() {
        return false;
    }


    /**
     * @return if this is a {@link CatalogColumn} return it.
     */
    Optional<Column> asCatalogColumn();

    class CorrelateColRef implements ColumnRef {
        private final Column parentCol;
        private final QB qb;


        public CorrelateColRef(Column parentCol, QB qb) {
            this.parentCol = parentCol;
            this.qb = qb;
        }

        @Override
        public Column getColumn() {
            return parentCol;
        }

        @Override
        public Optional<QB> appearsInQB() {
            return Optional.of(qb);
        }

        @Override
        public boolean isCorrelated() {
            return true;
        }
    }
}
