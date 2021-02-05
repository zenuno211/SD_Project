import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TaggedConnection implements AutoCloseable{

    DataInputStream dis;
    DataOutputStream dos;
    Socket socket;
    Lock rlock = new ReentrantLock();
    Lock wlock = new ReentrantLock();

    public static class Frame {
        public final int tag;
        public final byte[] data;
        public Frame(int tag, byte[] data){
            this.tag = tag;
            this.data = data;
        }
    }

   
    
    public TaggedConnection(Socket socket) throws IOException {
        this.socket = socket;
        this.dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        this.dis = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
    }

    public void send(Frame frame) throws IOException {
        send(frame.tag, frame.data);
    }

    public void send(int tag, byte[] data) throws IOException {
        
        try{
            wlock.lock();

            dos.writeInt(tag);
            dos.writeInt(data.length);
            dos.write(data);

            dos.flush();
        } finally {
            wlock.lock();
        }
    }

    public Frame receive() throws IOException{
        byte[] data;
        int tag;        
        try {
            rlock.lock();
            
            tag = dis.readInt();
            int length = dis.readInt();
            
            data = new byte[length];
            
            
            dis.readFully(data);

            return new Frame(tag, data);
        } finally {
            rlock.unlock();
        }
    }    

    public void close() throws IOException {
        socket.close();
    }
}
