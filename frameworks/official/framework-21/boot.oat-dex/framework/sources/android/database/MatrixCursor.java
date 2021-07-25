package android.database;

import java.util.ArrayList;

public class MatrixCursor extends AbstractCursor {
    private final int columnCount;
    private final String[] columnNames;
    private Object[] data;
    private int rowCount;

    public MatrixCursor(String[] columnNames2, int initialCapacity) {
        this.rowCount = 0;
        this.columnNames = columnNames2;
        this.columnCount = columnNames2.length;
        this.data = new Object[(this.columnCount * (initialCapacity < 1 ? 1 : initialCapacity))];
    }

    public MatrixCursor(String[] columnNames2) {
        this(columnNames2, 16);
    }

    private Object get(int column) {
        if (column < 0 || column >= this.columnCount) {
            throw new CursorIndexOutOfBoundsException("Requested column: " + column + ", # of columns: " + this.columnCount);
        } else if (this.mPos < 0) {
            throw new CursorIndexOutOfBoundsException("Before first row.");
        } else if (this.mPos < this.rowCount) {
            return this.data[(this.mPos * this.columnCount) + column];
        } else {
            throw new CursorIndexOutOfBoundsException("After last row.");
        }
    }

    public RowBuilder newRow() {
        int row = this.rowCount;
        this.rowCount = row + 1;
        ensureCapacity(this.rowCount * this.columnCount);
        return new RowBuilder(row);
    }

    public void addRow(Object[] columnValues) {
        if (columnValues.length != this.columnCount) {
            throw new IllegalArgumentException("columnNames.length = " + this.columnCount + ", columnValues.length = " + columnValues.length);
        }
        int i = this.rowCount;
        this.rowCount = i + 1;
        int start = i * this.columnCount;
        ensureCapacity(this.columnCount + start);
        System.arraycopy(columnValues, 0, this.data, start, this.columnCount);
    }

    public void addRow(Iterable<?> columnValues) {
        int start = this.rowCount * this.columnCount;
        int end = start + this.columnCount;
        ensureCapacity(end);
        if (columnValues instanceof ArrayList) {
            addRow((ArrayList) columnValues, start);
            return;
        }
        int current = start;
        Object[] localData = this.data;
        for (Object columnValue : columnValues) {
            if (current == end) {
                throw new IllegalArgumentException("columnValues.size() > columnNames.length");
            }
            localData[current] = columnValue;
            current++;
        }
        if (current != end) {
            throw new IllegalArgumentException("columnValues.size() < columnNames.length");
        }
        this.rowCount++;
    }

    private void addRow(ArrayList<?> columnValues, int start) {
        int size = columnValues.size();
        if (size != this.columnCount) {
            throw new IllegalArgumentException("columnNames.length = " + this.columnCount + ", columnValues.size() = " + size);
        }
        this.rowCount++;
        Object[] localData = this.data;
        for (int i = 0; i < size; i++) {
            localData[start + i] = columnValues.get(i);
        }
    }

    private void ensureCapacity(int size) {
        if (size > this.data.length) {
            Object[] oldData = this.data;
            int newSize = this.data.length * 2;
            if (newSize < size) {
                newSize = size;
            }
            this.data = new Object[newSize];
            System.arraycopy(oldData, 0, this.data, 0, oldData.length);
        }
    }

    public class RowBuilder {
        private final int endIndex;
        private int index;
        private final int row;

        RowBuilder(int row2) {
            this.row = row2;
            this.index = MatrixCursor.this.columnCount * row2;
            this.endIndex = this.index + MatrixCursor.this.columnCount;
        }

        public RowBuilder add(Object columnValue) {
            if (this.index == this.endIndex) {
                throw new CursorIndexOutOfBoundsException("No more columns left.");
            }
            Object[] objArr = MatrixCursor.this.data;
            int i = this.index;
            this.index = i + 1;
            objArr[i] = columnValue;
            return this;
        }

        public RowBuilder add(String columnName, Object value) {
            for (int i = 0; i < MatrixCursor.this.columnNames.length; i++) {
                if (columnName.equals(MatrixCursor.this.columnNames[i])) {
                    MatrixCursor.this.data[(this.row * MatrixCursor.this.columnCount) + i] = value;
                }
            }
            return this;
        }
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getCount() {
        return this.rowCount;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String[] getColumnNames() {
        return this.columnNames;
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public String getString(int column) {
        Object value = get(column);
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public short getShort(int column) {
        Object value = get(column);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        return Short.parseShort(value.toString());
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getInt(int column) {
        Object value = get(column);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.parseInt(value.toString());
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public long getLong(int column) {
        Object value = get(column);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public float getFloat(int column) {
        Object value = get(column);
        if (value == null) {
            return 0.0f;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return Float.parseFloat(value.toString());
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public double getDouble(int column) {
        Object value = get(column);
        if (value == null) {
            return 0.0d;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return Double.parseDouble(value.toString());
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public byte[] getBlob(int column) {
        return (byte[]) get(column);
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public int getType(int column) {
        return DatabaseUtils.getTypeOfObject(get(column));
    }

    @Override // android.database.AbstractCursor, android.database.Cursor
    public boolean isNull(int column) {
        return get(column) == null;
    }
}
