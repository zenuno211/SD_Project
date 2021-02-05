import java.util.HashMap;
import java.util.Map;
import java.util.Set;



public class Mapa { 

    private Map<Integer,Map<Integer, CoordStatus>> mapa;
    final Integer size;

    public Mapa(int N){
        size = N;
        mapa = new HashMap<Integer,Map<Integer, CoordStatus>>();
        for(int i = 0; i < N; i++){
            mapa.put(i, new HashMap<Integer,CoordStatus>());
            for(int j = 0; j < N; j++){
                mapa.get(i).put(j, new CoordStatus());
            }
        }
    }

    public void setPosition(String name, Coordinates coord){
        this.mapa.get(coord.x).get(coord.y).add(name);
    }

    public void updateInfetado(String username){
        this.mapa.values().forEach(m -> m.values().forEach(l -> {
            l.setInfetado(username);
        }));
    }

    public void remove(String name){
        this.mapa.values().forEach(m -> m.values().forEach(l -> {
            if(l.contains(name)){
                l.remove(name); //remove user dos utilizadores atuais da coordenada
                l.signal(); //possivelmente signal aos threads que esperarem que a coordenada esteja vazia
            };
        }));
    }

    public Boolean move(String name, Coordinates coord){
        if(coord.x >= this.size || coord.y >= this.size){ //verifica se coordenadas são válidas
            return false;
        }
        remove(name); //remove da posição anterior
        setPosition(name, coord); //adiciona à nova posição
        return true;
    }

    public Set<String> getCurrent(Coordinates c){
        return this.mapa.get(c.x).get(c.y).getCurrent();
    }


    public int getNumPessoasLocalizacao(Coordinates coord){
        return this.mapa.get(coord.x).get(coord.y).currentSize();  
    }

    public void waitFor(Coordinates c) throws InterruptedException {
        this.mapa.get(c.x).get(c.y).waitForEmpty();
    }   

    public String getMapaString(){
        StringBuilder sb = new StringBuilder();
        sb.append("(Visitantes, Infetados)\n");
        this.mapa.values().forEach(
                            m -> {m.values().forEach(
                                            l -> {
                                                sb.append("(" + l.pastSize() + "," + l.infected() +") ");
                                            }); 
                                            sb.append("\n");
                            });

        return sb.toString();
    }
    
}