package compiler;

import java.io.*;

public class FileScanner {
    private final Reader reader;

    public FileScanner(String filePath) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(filePath));
    }


    public int readCharacter() throws IOException {
        return reader.read();
    }

    public void close() throws IOException {
        reader.close();
    }

    /**
     * Reads a line from the file.
     * @return The line read from the file,
     * or null if the end of the file is reached.
     * @throws IOException If an I/O error occurs.
     */
    public boolean isEmpty() throws IOException {
        reader.mark(1);
        int firstChar = reader.read();
        reader.reset();

        return (firstChar == -1);
    }
}
