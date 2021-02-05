import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class MainWorker implements Runnable{
    TaggedConnection conn;
    ServerStatus serverStatus;

    public MainWorker(TaggedConnection conn, ServerStatus serverStatus){
        this.conn = conn;
        this.serverStatus = serverStatus;
    }

    public void run(){
        boolean running = true;
        //dados sobre permissões do cliente
        Boolean autenticado = false;
        Boolean infetado = false;
        Boolean authorized = false;

        String username = "";
        
        
        while (running) {
            try {

                TaggedConnection.Frame f = conn.receive();

                switch (f.tag) {

                    case 1: // login
                        TaggedConnection.Frame fPassword = conn.receive();
                        username = new String(f.data);
                        String password = new String(fPassword.data);
                        autenticado = serverStatus.login(username, password);

                        if(autenticado){
                            authorized = serverStatus.isAuthorized(username);
                            infetado = serverStatus.isInfetado(username);
                        }

                        if (autenticado && !infetado) {
                            conn.send(1, "true".getBytes());
                        } else if (infetado){
                            conn.send(1, "infetado".getBytes());
                        } else {
                            conn.send(1, "false".getBytes());
                        }
                        break;

                    case 2: // registo
                        TaggedConnection.Frame fPasswordReg = conn.receive();
                        TaggedConnection.Frame fAuthorized = conn.receive();
                        username = new String(f.data);
                        String passwordReg = new String(fPasswordReg.data);
                        authorized = new String(fAuthorized.data).equals("true");

                        Boolean registado = serverStatus.registo(username, passwordReg, authorized);
                        
                        if (registado) {
                            conn.send(2, "true".getBytes());
                            autenticado = true;
                            infetado = false;
                        } else {
                            conn.send(2, "false".getBytes());
                            authorized = false;
                        }
                        break;

                    case 3: // atualizarLocalizacao
                        if (autenticado) {
                            TaggedConnection.Frame fCoordenadaY = conn.receive();
                            int coordX = Integer.parseInt(new String(f.data));
                            int coordY = Integer.parseInt(new String(fCoordenadaY.data));

                            Coordinates coordenadas = new Coordinates(coordX, coordY);

                            Boolean moved = serverStatus.atualizarLocalizacao(username, coordenadas);
                            System.out.println(username + " moved to (" + coordX + "," + coordY + "): " + moved);
                            if (moved) {
                                conn.send(3, "true".getBytes());
                            } else {
                                conn.send(3, "false".getBytes());
                            }
                        }
                        break;

                    case 4: // número de  Pessoas em Localizacao
                        if (autenticado) {
                            TaggedConnection.Frame fCoordenadaYY = conn.receive();
                            int coordXX = Integer.parseInt(new String(f.data));
                            int coordYY = Integer.parseInt(new String(fCoordenadaYY.data));

                            Coordinates coord = new Coordinates(coordXX, coordYY);
                            int numPeople = serverStatus.getNPessoasLocalizacao(coord);
                            String numPeopleBytes = String.valueOf(numPeople);

                            conn.send(4, numPeopleBytes.getBytes());
                        }
                        break;
                        
                    case 6: //definir como infetado
                        if(autenticado){
                            infetado = serverStatus.setInfetado(username);
                        }
                        break;

                    case 7: //enviar mapa para user com autorização especial
                        if(authorized && autenticado){
                            conn.send(7, serverStatus.getMapaString().getBytes());
                        } else {
                            conn.send(7,"NA".getBytes());
                        }
                        break;
                }
            } catch (IOException e){
                e.printStackTrace();
                running = false;
            }
        }
        //conn.close();   
    }
}

class ServerSocketMainWorker implements Runnable{
    ServerSocket serverSocket;
    ServerStatus serverStatus;
    
    public ServerSocketMainWorker(int port, ServerStatus serverStatus) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverStatus = serverStatus;
    }

    public void run(){
        try{
            Boolean isRunning = true;
            Socket socket;
            while(isRunning){
                socket = serverSocket.accept();
                Thread thread = new Thread(new MainWorker(new TaggedConnection(socket), serverStatus));
                thread.start();
            }

            serverSocket.close();

        } catch (IOException e){

        }
    }
}

class WaiterWorker implements Runnable{
    TaggedConnection conn;
    ServerStatus serverStatus;

    public WaiterWorker(TaggedConnection conn, ServerStatus serverStatus){
        this.conn = conn;
        this.serverStatus = serverStatus;
    }

    public void run(){
        boolean running = true;
        while(running){
            try{
                System.out.println("Waiter before receive");
                TaggedConnection.Frame f = conn.receive();

                switch (f.tag){
                    case 5:
                        TaggedConnection.Frame fcoordX = conn.receive();
                        TaggedConnection.Frame fcoordY = conn.receive();
                        int coordX = Integer.parseInt(new String(fcoordX.data));
                        int coordY = Integer.parseInt(new String(fcoordY.data));

                        this.serverStatus.waitFor(new Coordinates(coordX, coordY));

                        conn.send(5,"".getBytes());
                        running = false;

                }
            }catch(IOException e){
                System.out.println("erro no waiterWorker");
            } catch (InterruptedException e){
                System.out.println("Erro waitFor");
            }
        }

        //conn.close();
    }
}

class ServerSocketWaiterWorker implements Runnable{
    ServerSocket serverSocket;
    ServerStatus serverStatus;
    
    public ServerSocketWaiterWorker(int port, ServerStatus serverStatus) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverStatus = serverStatus;
    }

    public void run(){
        try{
            Boolean isRunning = true;
            Socket socket;
            while(isRunning){
                System.out.println("Waiting for clients");
                socket = serverSocket.accept();
                Thread thread = new Thread(new WaiterWorker(new TaggedConnection(socket), serverStatus));
                thread.start();
            }
            serverSocket.close();

        } catch (IOException e){

        }
    }
}

//Socket para Adicional 1
class InfectedWaiterWorker implements Runnable{
    TaggedConnection conn;
    ServerStatus serverStatus;

    public InfectedWaiterWorker(TaggedConnection conn, ServerStatus serverStatus){
        this.conn = conn;
        this.serverStatus = serverStatus;
    }

    public void run(){
        boolean running = true;
        try{
            TaggedConnection.Frame f = conn.receive();
            String username = new String(f.data);
            while(running){
                try{
                    System.out.println("WaiterInfected before send");

                    serverStatus.waitForContact(username);
                    conn.send(8,"".getBytes());
                            
                } catch (InterruptedException e){
                    System.out.println("Erro waitFor");
                }
            }
        }catch (IOException e){
            System.out.println("Erro IOException");
        }

        //conn.close();
    }
}

class ServerSocketInfectedWaiterWorker implements Runnable{
    ServerSocket serverSocket;
    ServerStatus serverStatus;
    
    public ServerSocketInfectedWaiterWorker(int port, ServerStatus serverStatus) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.serverStatus = serverStatus;
    }

    public void run(){
        try{
            Boolean isRunning = true;
            Socket socket;
            while(isRunning){
                socket = serverSocket.accept();
                Thread thread = new Thread(new InfectedWaiterWorker(new TaggedConnection(socket), serverStatus));
                thread.start();
            }
            serverSocket.close();

        } catch (IOException e){

        }
    }
}



public class Server {

    public static void main (String[] args) throws IOException {
        ServerStatus serverStatus = new ServerStatus();
        
        Thread mainWorker = new Thread(new ServerSocketMainWorker(12345, serverStatus));
        Thread waiterWorker = new Thread(new ServerSocketWaiterWorker(23456, serverStatus));
        Thread infectedWaiterWorker = new Thread(new ServerSocketInfectedWaiterWorker(34567, serverStatus));

        
        mainWorker.start();
        waiterWorker.start();
        infectedWaiterWorker.start();
    }

}