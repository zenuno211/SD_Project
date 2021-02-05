import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


public class Client {
    

    //retorna username se autenticado, senão vazio
    private static String login(TaggedConnection c){
        Scanner scn = new Scanner(System.in);
        Boolean authenticated = false;
        Boolean infected = false;
        String username = null;
        String password = null;

        
        System.out.println("Inserir username: ");
        username = scn.nextLine();
        System.out.println("Inserir password: ");
        password = scn.nextLine();


        try{
            //send Login Request (tag = 1)
            c.send(1, username.getBytes());

            c.send(1, password.getBytes());

            // get reply
            TaggedConnection.Frame f = c.receive();

            String received = new String(f.data);
            
            if(f.tag != 1){
                System.out.println("Erro resposta Servidor");
            } else {
                authenticated = (received.equals("true"));
                infected =  (received.equals("infetado"));
            }
            
        } catch (IOException e){
            System.out.println("Erro de comunicação");
        }

        if(!authenticated){
            System.out.println("Não foi possível efetuar o login");
        }
        if(infected){
            System.out.println("Não pode interagir com a aplicação por estar infetado");
        }
        //scn.close();
        return authenticated ? username : "";
    }

    //retorna username se registado, senão vazio
    private static String registo(TaggedConnection c){
        Scanner scn = new Scanner(System.in);
        Boolean registered = false;
        String username = null;
        String password = null;

        while(!registered){
            System.out.println("Inserir username: ");
            username = scn.nextLine();
            System.out.println("Inserir password: ");
            password = scn.nextLine();
            System.out.println("Tem autorização especial? 1-Sim/2-Não");
            int inputAuthorized = scn.nextInt();
            boolean authorized = (inputAuthorized == 1);

            try{
                //send Register Request (tag = 2)
                c.send(2, username.getBytes());

                c.send(2, password.getBytes());

                if(authorized){
                    c.send(2,"true".getBytes());
                } else {
                    c.send(2,"false".getBytes());
                }

                // get reply
                TaggedConnection.Frame f = c.receive();
                
                String received = new String(f.data);

                
                if(f.tag != 2){
                    System.out.println("Erro resposta Servidor");
                } else {
                    registered = (received.equals("true"));
                }
                
            } catch (IOException e){
                System.out.println("Erro de comunicação");
            }
        }

        if(!registered){
            System.out.println("Não foi possível efetuar o registo");
        }

        //scn.close();
        return registered ? username : "";
    }

    

    //void atualizarLocalização(Coordinates coord);

    private static void atualizarLocalizacao(TaggedConnection c){
        Scanner scn  = new Scanner(System.in);
        //Coordinates coordenadas;
        String coordX;
        String coordY;
        Boolean confirmation = false;

        System.out.println("Insira a coordenada em x: ");
        coordX = Integer.toString(scn.nextInt());
        System.out.println("insira a coordenada em y: ");
        coordY = Integer.toString(scn.nextInt());

        try{
            //send atualizarLocalizacao Request (tag = 3)
            c.send(3, coordX.getBytes());

            c.send(3, coordY.getBytes());

            // get reply
            TaggedConnection.Frame f = c.receive();
            String received = new String(f.data);
            

            if(f.tag != 3){
                System.out.println("Erro resposta Servidor");
            } else {
                confirmation = (received.equals("true"));
                if(!confirmation){
                    System.out.println("Não foi possível atualizar a localização: coordenadas inválidas");
                }
            }
            
        } catch (IOException e){
            System.out.println("Erro de comunicação");
        }
        //scn.close();
    }



    private static int getNPessoasLocalizacao(TaggedConnection c){
        Scanner scn  = new Scanner(System.in);
        //Coordinates coordenadas;
        String coordX;
        String coordY;
        int nrPeople = 0;

        System.out.println("Insira a coordenada em x: ");
        coordX = Integer.toString(scn.nextInt());
        System.out.println("insira a coordenada em y: ");
        coordY = Integer.toString(scn.nextInt());

        try{
            //send getNPessoasLocalizacao Request (tag = 4)
            c.send(4, coordX.getBytes());

            c.send(4, coordY.getBytes());

            // get reply
            TaggedConnection.Frame f = c.receive();
            String received = new String(f.data);
            
                       
            if(f.tag != 4){
                System.out.println("Erro resposta Servidor");
            }
            else{
                nrPeople = Integer.parseInt(received);
            }

            System.out.println("Estão " + nrPeople + " pessoas na coordenada (" + coordX + "," + coordY + ")");
            scn.nextLine();
            scn.nextLine();

            
        } catch (IOException e){
            System.out.println("Erro de comunicação");
        }
        //scn.close();
        return nrPeople;

    }

    private static void getAvisoLocalizacaoVazia(TaggedConnection c){
        Scanner scn  = new Scanner(System.in);
        //Coordinates coordenadas;
        String coordX;
        String coordY;

        System.out.println("Insira a coordenada em x: ");
        coordX = Integer.toString(scn.nextInt());
        System.out.println("insira a coordenada em y: ");
        coordY = Integer.toString(scn.nextInt());
        
        Thread thread = new Thread(()-> {
            try{                  
                Socket waiterSocket = new Socket("localhost", 23456);
                TaggedConnection waiterConnection = new TaggedConnection(waiterSocket);
                waiterConnection.send(5, " ".getBytes());

                //send LocalizacaoVazia Request (tag = 5)
                waiterConnection.send(5, coordX.getBytes());
                
                waiterConnection.send(5, coordY.getBytes());
                
                // get reply
                TaggedConnection.Frame f = waiterConnection.receive();
                
                if(f.tag != 5){
                    System.out.println("Erro resposta Servidor");
                }
                else{
                    System.out.println("\nAviso: Coordenada ("+coordX+","+coordY+") vazia");
                
                }
            }catch(IOException e){
                System.out.println("Erro de comunicação thread");
            }
        });

        thread.start();
        
    
    }

    private static void setDoente(TaggedConnection c){
        try {
            c.send(6, " ".getBytes());
        }catch(IOException e){
            System.out.println("Erro de comunicação");
        }
    }


    //Funcionalidades Adicionais
    private static void descarregaMapa(TaggedConnection c){
        try {
            Scanner scn = new Scanner(System.in);
            c.send(7, " ".getBytes());            

            TaggedConnection.Frame f = c.receive();

            if(f.tag != 7){
                System.out.println("Erro resposta Servidor");
            }
            else{
                String data = new String(f.data);
                if(data.equals("NA")){ //mensagem recebida quando Não Autorizado
                    System.out.println("Não tem permissões para esta funcionalidade");
                } else {
                    System.out.println(data);
                    scn.nextLine();
                }
            
            }
        }catch(IOException e){
            System.out.println("Erro de comunicação");
        }
    }

    //retornar username se autenticado ou vazio se não for autenticado
    private static String loginMenu(TaggedConnection c){
        int option = 0;
        Scanner menu = new Scanner(System.in);
        Boolean authenticated = false;
        Boolean running = true;
        String username = null;

        while(!authenticated && running){
            showLoginMenu();

            System.out.print("Escolha uma opção: ");
            if(menu.hasNext()){
                option = menu.nextInt();
            }            

            switch(option){
                case 1:
                    username = login(c);
                    authenticated = !username.isEmpty();
                    break;
                case 2:
                    username = registo(c);
                    authenticated = !username.isEmpty();
                    break;                    
                case 0:
                    System.out.println("Sessão encerrada");
                    running = false;
                    break;
                default:
                    System.out.println("Opção Inválida");
                    break;

            }
        }

        //se não estiver autenticado ou user tiver saído, retornar vazio, else retornar nome
        return (authenticated && running) ? username : "";
    }

    private static void showLoginMenu(){
        System.out.println("----------------------------------------");
        System.out.println("                LoginMenu               ");
        System.out.println("----------------------------------------");
        System.out.println();
        System.out.println("1 - Login");
        System.out.println("2 - Registar");
        System.out.println("0 - Sair");
    }


    private static void mainMenu(TaggedConnection c) {
        int option = 0;
        Scanner menu = new Scanner(System.in);
        boolean running = true;

        while(running) {

            showMenu();

            System.out.print("Escolha uma opção: ");
            if(menu.hasNext()){
                option = menu.nextInt();
            } else {
            }

            switch (option) {
                case 1:
                    atualizarLocalizacao(c);
                    break;
                case 2:
                    getNPessoasLocalizacao(c);
                    break;
                case 3:
                    getAvisoLocalizacaoVazia(c);
                    break;
                case 4:
                    setDoente(c);
                    System.out.println("As funcionalidades estão bloqueadas\nSessão encerrada");
                    running = false;
                    
                    break;
                case 5:
                    descarregaMapa(c);
                    break;
                default:
                    System.out.println("Opção Inválida");
                    break;
                case 0:
                    System.out.println("Sessão encerrada");
                    running = false;
                    break;
                    
            }
        }
        //menu.close();
    }

    private static void showMenu() {
        System.out.println("----------------------------------------");
        System.out.println("                   Menu                 ");
        System.out.println("----------------------------------------");
        System.out.println();
        System.out.println("1 - Atualizar a sua localização");
        System.out.println("2 - Número de pessoas numa localização");
        System.out.println("3 - Aviso quando não houver ninguém numa localização");
        System.out.println("4 - Sinalizar que está doente");
        System.out.println("5 - Descarregar mapa com todas as informações");
        System.out.println("0 - Sair");
    }

    public static Thread waitForRiskThread(String username){
        Thread thread = new Thread(()-> {
            try{                  
                Socket riskSocket = new Socket("localhost", 34567);
                TaggedConnection riskConnection = new TaggedConnection(riskSocket);
                riskConnection.send(8, username.getBytes());
                
                // get reply
                while(true){
                    TaggedConnection.Frame f = riskConnection.receive();
                
                    if(f.tag != 8){
                        System.out.println("Erro resposta Servidor");
                    }
                    else{
                        System.out.println("ALERTA! Contacto de risco!");
                    }
                }
            }catch(IOException e){
                System.out.println("Erro de comunicação thread");
            }
        });

        return thread;
    }

    public static void main(String[] args) {
        try {
            Socket s = new Socket("localhost", 12345);
            TaggedConnection c = new TaggedConnection(s);

            String username = loginMenu(c);

            if(!username.isEmpty()){ //se username.isEmpty(), o utilizador não foi autenticado
                Thread riskThread = waitForRiskThread(username);
                riskThread.start();
                mainMenu(c);
            }

        } catch (IOException e) {
            System.out.println("Erro - Servidor inacessível");
            return;
        }

    }
}