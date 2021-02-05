import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerStatus{

    Mapa mapa; //mapa de coordenadas -> CoordStatus
    Map<String, UserStatus> mapUserStatus; //mapa de registos: username -> UserStatus
    
    public ServerStatus(){
        this.mapa = new Mapa(5);
        this.mapUserStatus = new HashMap<>();
    }

    public boolean login(String username, String password){
        boolean autenticado = false;
        UserStatus user = mapUserStatus.get(username);
        if(user!=null && user.checkPassword(password)){
            autenticado = true;
        }
        
        return autenticado;
    }

    public boolean registo(String username, String password, Boolean authorized){
        boolean registado = false;
        if(mapUserStatus.get(username)==null){
            UserStatus newUser = new UserStatus(username, password, authorized); 
            mapUserStatus.put(username,newUser);
            registado = true;
        }
        return registado;
    }

    public Boolean atualizarLocalizacao(String username, Coordinates coordenadas){
        Boolean success = mapa.move(username,coordenadas);
        if(success){
            //atualizar contactos entre utilizador a mover-se e utilizadores na nova localização
            Set<String> current = this.mapa.getCurrent(coordenadas);
            for(String userInCoord: current){
                if(!userInCoord.equals(username)){ //não adicionar utilizador aos seus próprios contactos
                    this.mapUserStatus.get(userInCoord).addContacto(username);
                    this.mapUserStatus.get(username).addContacto(userInCoord);
                }
            }
        }
        return success;
    }

    public int getNPessoasLocalizacao(Coordinates coordenadas){
        return mapa.getNumPessoasLocalizacao(coordenadas);
    }

    public void waitFor(Coordinates c) throws InterruptedException {
        this.mapa.waitFor(c);
    }

    public void waitForContact(String username) throws InterruptedException{
        this.mapUserStatus.get(username).await();
    }


    public boolean setInfetado(String username){
        UserStatus userS = this.mapUserStatus.get(username);
        
        if(userS != null){
            userS.setInfetado();

            this.mapa.updateInfetado(username);
            Set<String> contactados = userS.getUsersContactados();
            for(String c: contactados){
                this.mapUserStatus.get(c).alert();
            }
            
            return true;
        } else {
            return false;
        }
    }


    public boolean isInfetado(String username){
        UserStatus userS = this.mapUserStatus.get(username);
        return userS.isInfetado();
    }

    public boolean isAuthorized(String username){
        UserStatus userS = this.mapUserStatus.get(username);
        return userS.isAuthorized();
    }

    public String getMapaString(){
        return this.mapa.getMapaString();
    }
}  