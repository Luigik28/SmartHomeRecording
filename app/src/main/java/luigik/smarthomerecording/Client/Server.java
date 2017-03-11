package luigik.smarthomerecording.Client;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Server {

    private Socket socket = null;

    public Server(String ip, int port) throws IOException {
        socket = new Socket(ip,port);
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[8192];
        int len = 0;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
        }
    }

    public void sendFile(String nameFile) throws IOException, ClassNotFoundException {
        File transferFile = new File (nameFile);
        System.out.println("Invio file " + transferFile.getAbsolutePath());
        InputStream in = new FileInputStream(nameFile);
        OutputStream out = socket.getOutputStream();
        copy(in, out);
        out.close();
        in.close();
    }

}
