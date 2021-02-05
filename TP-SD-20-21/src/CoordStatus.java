import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Classe que inclui informação relativa a uma coordenada do mapa
public class CoordStatus {
    
    private Set<String> current;
    private Set<String> past;
    private Integer infetados;
    private Integer hasWaiting;
    private ReentrantLock lock;
    private Condition cond;

    public CoordStatus(){
        this.current = new HashSet<>();
        this.past = new HashSet<>();
        this.lock = new ReentrantLock();
        this.cond = lock.newCondition();
        this.hasWaiting = 0;
        this.infetados = 0;
    }

    public boolean contains(String nome){
        lock.lock();
        try{
            return this.current.contains(nome);
        } finally {
            lock.unlock();
        }
        
    }

    public boolean add(String nome){
        lock.lock();
        try{
            this.past.add(nome);
            return this.current.add(nome);
        } finally {
            lock.unlock();
        }
    }

    public boolean remove(String nome){
        lock.lock();
        try{
            return this.current.remove(nome);
        } finally {
            lock.unlock();
        }
        
    }


    public void waitForEmpty() throws InterruptedException {
        this.lock.lock();
        try{
            this.hasWaiting++;
            while(this.current.size() != 0){
                this.cond.await();
            }
            this.hasWaiting--;
        } finally {
            lock.unlock();
        }
    }

    public void setInfetado(String nome){
        lock.lock();
        try{
            if(this.past.contains(nome)){
                this.infetados++;
            }
        } finally {
            lock.unlock();
        }
        
    }

    public void signal(){
        lock.lock();
        try{
            if(this.hasWaiting != 0){ //signal aos threads que esperam que a coordenada esteja vazia
                this.cond.signalAll();
            }
        } finally{
            lock.unlock();
        }
        
    }

    public int infected(){
        this.lock.lock();
        try{
            return this.infetados;
        } finally {
            this.lock.unlock();
        }
        
    }

    public int currentSize(){
        this.lock.lock();
        try{
            return this.current.size();
        } finally {
            this.lock.unlock();
        }
    }

    public int pastSize(){
        this.lock.lock();
        try{
            return this.past.size();
        } finally {
            this.lock.unlock();
        }
    }

    public Set<String> getCurrent(){
        this.lock.lock();
        try{
            return this.current;
        } finally {
            this.lock.unlock();
        }
    }
}
