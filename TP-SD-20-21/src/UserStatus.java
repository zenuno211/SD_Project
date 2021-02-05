import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// Classe que guarda/trata a informação do utilizador
public class UserStatus {
    private String username;
    private String password;
    private Set<String> usersContactados;

    private boolean isInfetado;
    private boolean contactoDeRisco;
    private boolean hasSpecialAuthorization;
    private Integer nLogins;

    private Lock lock;
    private Condition contactCond;


    public UserStatus(String username, String password, Boolean hasSpecialAuthorization){
        this.username = username;
        this.password = password;
        this.usersContactados = new HashSet<>();
        this.isInfetado = false;
        this.contactoDeRisco = false;
        this.hasSpecialAuthorization = hasSpecialAuthorization;
        this.lock = new ReentrantLock();
        this.contactCond = lock.newCondition();
        this.nLogins = 0;
    }


    public String getPassword() {
        lock.lock();
        try{
            return this.password;
        } finally {
            lock.unlock();
        }
    }

    public boolean isInfetado(){
        lock.lock();
        try{
            return this.isInfetado;
        } finally {
            lock.unlock();
        }
    }

    public void setInfetado(){
        lock.lock();
        try{
            this.isInfetado = true;
        } finally {
            lock.unlock();
        }
    }

    public boolean isAuthorized(){
        lock.lock();
        try{
            return this.hasSpecialAuthorization;
        } finally {
            lock.unlock();
        }
    }

    public void addContacto(String username){
        lock.lock();
        try{
            this.usersContactados.add(username);
        } finally {
            lock.unlock();
        }
    }

    //Thread espera por 
    public void await() throws InterruptedException {
        lock.lock();
        try{
            int n = this.nLogins;
            while(!this.contactoDeRisco || n!=this.nLogins){
                this.contactCond.await();
            }
            this.contactoDeRisco = false;
        } finally {
            lock.unlock();
        }
    }

    public void alert(){
        lock.lock();
        try{
            this.contactoDeRisco = true;
            this.contactCond.signalAll();
        }finally {
            lock.unlock();
        }
    }

    public Set<String> getUsersContactados(){
        lock.lock();
        try{
            return this.usersContactados;
        } finally {
            lock.unlock();
        }
    }

    public boolean checkPassword(String password){
        lock.lock();
        try{
            Boolean ok = (this.password.equals(password));
            if(ok) this.nLogins++;
            return ok;
        } finally {
            lock.unlock();
        }
 }
}
