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


}
