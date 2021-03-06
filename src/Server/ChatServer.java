package Server;

import Client.Avatar;
import Client.IPlayer;
import Server.Server_Interface.IChatServerManagement;
import Server.Server_Interface.IServerControllerServerSide;
import javafx.util.Pair;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class ChatServer  extends UnicastRemoteObject implements IChatServerManagement {
    private int available;
    private Grid gGrid;
    private Zone z = new Zone(0,0);
    int size = 8;
    private Map<Integer, List<Avatar>> positionMap;
    private Map<Avatar, IPlayer> lclient = new LinkedHashMap<>();
    private List<Avatar> listAvatar;

    /**
     * Constructeur du ChatServer
     *
     * @throws RemoteException
     */
    public ChatServer() throws RemoteException{
        super();
        gGrid=new Grid(0);
        this.size = 0;
        positionMap = new LinkedHashMap<>();
        listAvatar = new LinkedList<>();
        available=0;
        this.z = new Zone(0,0);
    }

    /**
     * Constructeur utilisé pour créer un ChatServer configuré
     * @param grid
     * Grille utilisé par le chat server
     * @param size
     * Taille de la grille
     * @param z
     * Zone géré
     * @throws RemoteException
     */
    public ChatServer(Grid grid, int size, Zone z) throws RemoteException{
        super();
        gGrid=grid;
        this.size = size;
        positionMap = new LinkedHashMap<>();
        listAvatar = new LinkedList<>();
        available=1;
        this.z = z;
        for (int i = 0; i < size*size; i++) {
            positionMap.put(i, new ArrayList<Avatar>());
        }
        gGrid.displayGrid();
    }

    /**
     * Permet de mettre à jour le ChatServer
     * @param grid
     * nouvelle grille du chat server
     * @param size
     * Nombre de case à gérer
     * @param z
     * Zone de case à gérer
     */
    public void setChatServer(Grid grid, int size, Zone z){
        gGrid=grid;
        this.size = size;
        available=1;
        this.z = z;
        for (int i = 0; i < size*size; i++) {
            positionMap.put(i, new ArrayList<Avatar>());
        }
        gGrid.displayGrid();
    }

    /**
     * Permet de parler sur le serveur de chat
     * @param sender
     *          Avatar à l'origine du message
     * @param text
     *          Texte envoyé par l'avatar
     * @throws RemoteException
     */
    @Override
    public void speak(Avatar sender, String text) throws RemoteException {
        List<Avatar> lav = positionMap.get(sender.getPosition());
        for (Avatar receiver : lav) {
            try{

                IPlayer rcv = lclient.get(receiver);
                rcv.receiveMessage(sender, text);
            }
            catch (Exception e){
                System.out.println("client injoignable");
                if(!reachable(lclient.get(receiver))){
                    disconnectPlayer(lclient.get(receiver),receiver);
                    System.out.println("client supprimé car injoignable");
                }
                else{
                    try {
                    lclient.get(receiver).receiveMessage(sender, text);
                    }
                    catch (RemoteException re){
                        disconnectPlayer(lclient.get(receiver),receiver);
                        System.out.println("client supprimé car instable");
                    }

                }
            }
        }
    }


    /**
     * Mets à jour la zone à gérer.
     * Survient lors de l'ajout ou la supression d'un serverchat dans la partie
     * @param z
     * Nouvelle zone à gérée
     * @throws RemoteException
     */
    @Override
    public void updateZone(Zone z) throws RemoteException {
        this.z=z;
        System.out.println(this.getZ());
    }

    /**
     * Permet à un joueur de se connecter au serveur de chat
     * @param av
     * avatar qui se connecte
     * @param position
     * position de l'avatar
     * @return
     * si le serveur est disponible ou non
     * @throws RemoteException
     */
    @Override
    public int connection(Avatar av, Integer position, IPlayer player) throws RemoteException {
        if(available==0)
            return available;
        if(lclient.containsKey(av)) return -1;
        System.out.println("nouveau client : "+av.getName());
        List<Avatar> user = positionMap.get(position);
        user.add(av);
        listAvatar.add(av);
        lclient.put(av,player);
        System.out.println(lclient);
        System.out.println("-----");
        System.out.println(listAvatar);
        return available;
    }

    /**
     * déconnecte un joueur
     * @param p
     * joueur a déconnecté
     * @param av
     * Avatar du joueur
     */
    public void disconnectPlayer(IPlayer p, Avatar av){
        //Ajouter ici la sauvegarde
        av = getAvatar(av);
        boolean res = positionMap.get(av.getPosition()).remove(av);
        System.out.println(res);
        System.out.println(av.getPosition());
        /*for( Avatar sup : positionMap.get(av.getPosition())){
            if(sup.getName()==av.getName()){
                System.out.println("suppress !!!!");
                res = positionMap.get(av.getPosition()).remove(sup);
                System.out.println(res);
                break;
            }
        }*/
        lclient.remove(p);

    }

    /**
     * Teste si le client est joignable
     * effectue 5 ping
     * @param p
     * @return
     * true si un ping a fonctionné
     * false sinon
     */
    private boolean reachable(IPlayer p){
        Integer res=0;
        for (int i = 0; i < 3; i++) {
            try {
                res=p.ping();
            } catch (RemoteException e) {

            }
            if(res==1)return true;
        }
        return false;
    }

    /**
     * Permet de retrouver la position d'un avatar sur la grille
     * @param av
     * @return
     * L'avatar avec la bonne position
     */
    public Avatar getAvatar(Avatar av){

        List<Avatar> lav = positionMap.get(av.getPosition());
        for (Avatar avatar : lav) {
            if(avatar.equals(av))
                return  avatar;
        }

        //Si on ne le trouves pas à la bonne position on le cherche dans les cases ajacentes
        int pos[]={(av.getPosition()+1 & 0xff)%(size*size),(av.getPosition()-1 & 0xff)%(size*size),(av.getPosition()+size & 0xff)%(size*size),(av.getPosition()-size & 0xff)%(size*size)};
        for (int i : pos) {
            if(positionMap.get(i).contains(av)){
                av.setPosition(i);
                Avatar avatar= getAvatar(av);
                avatar.setPosition(i);
                return avatar;
            }
        }

        //Si il n'existe pas on quitte
        if(positionMap.containsValue(av)) return null;

        //Sinon on cherche dans toute la grille
        for (Map.Entry<Integer, List<Avatar>> entry : positionMap.entrySet())
        {
            if(positionMap.get(entry.getKey()).contains(av)){
                av.setPosition(entry.getKey());
                Avatar avatar= getAvatar(av);
                avatar.setPosition(entry.getKey());
                return avatar;
            }
        }
        return null;

    }

    /**
     * Peu utilisé permet de déplacer le joueur en fonction de la direction voulu
     * @param avUsed
     * @param goTo
     * direction
     * @return
     * @throws RemoteException
     */
    @Override
    public int move(Avatar avUsed, String goTo) throws RemoteException {
        System.out.println("pav "+avUsed.getPosition());
        avUsed=getAvatar(avUsed);
        System.out.println("pav "+avUsed.getPosition());
        if(avUsed==null) return -10;
        if(!avUsed.isInLife)return -9;
        int position = avUsed.getPosition();
        Integer x,y;
        x=position/8;
        y=position%8;
        Room r = gGrid.getRoom(x,y);
        Integer dest;
        switch (goTo) {
            case "N" : dest = r.getNorth().dest; break;
            case "W" : dest = r.getWest().dest; break;
            case "E" : dest = r.getEast().dest; break;
            case "S" : dest = r.getSouth().dest; break;
            default : dest = -1; break;
        }
        if(dest==-1)
            return -1;
        //Si le serveur ne gère pas la case
        if(dest<(Integer) z.getKey() || dest>(Integer) z.getValue()) {
            System.out.println(dest+" non géré");
            disconnectPlayer(lclient.get(avUsed),avUsed);
            return -2;
        }
        positionMap.get(position).remove(avUsed);
        positionMap.get(dest).add(avUsed);
        avUsed.setPosition(dest);
        return dest;

    }

    /**
     * Méthode privilégiée pour les déplacements. Déplace automatiquement l'avatar à la position voulue
     * @param avUsed
     * @param position
     * position visée
     * @return
     * @throws RemoteException
     */
    @Override
    public int moveTo(Avatar avUsed, Integer position) throws RemoteException {
        if(position<(Integer) z.getKey() || position>(Integer) z.getValue()) {
            System.out.println(position+" non géré");
            disconnectPlayer(lclient.get(avUsed),avUsed);
            return position;
        }
        System.out.println(positionMap.get(getAvatar(avUsed).getPosition()).remove(avUsed));
        System.out.println("from "+avUsed.position+" to "+position);
        positionMap.get(avUsed.position).remove(avUsed);
        avUsed.setPosition(position);
        positionMap.get(position).add(avUsed);
        return 0;
    }

    /**
     * Récupère les autres avatars présents sur la même case
     * @param av
     *          Avatar souhaitant connaître ses voisins
     * @return
     * @throws RemoteException
     */
    @Override
    public List<Avatar> getNeighbour(Avatar av) throws RemoteException {
        av = getAvatar(av);
        return positionMap.get(av.getPosition());
    }

    /**
     * Permet de se déconnecter du serveur de chat
     * @param av
     *          Avatar se déconnectant
     * @param player
     *          Joueur de l'avatar
     * @throws RemoteException
     */
    @Override
    public void disconnection(Avatar av, IPlayer player) throws RemoteException{
        Avatar avUsed=getAvatar(av);
        int position = avUsed.getPosition();
        positionMap.get(position).remove(av);
        lclient.remove(av);
    }

    public Zone getZ() {
        return z;
    }

    /**
     * création du chatServer,
     * Ajout du chat sur le registre rmi,
     * connexion au serverManager
     * gestion deconnexion
     * @param args
     * @throws Exception
     */
    public static void main(String args[]) throws Exception {
        // Démarre le rmiregistry
        //LocateRegistry.createRegistry(1099);
        ChatServer obj = new ChatServer();
        IServerControllerServerSide mainServer = (IServerControllerServerSide) Naming.lookup("//localhost/Dungeon");
        Pair<Grid,Zone> res = mainServer.chatServerConnection(obj);
        obj.setChatServer(res.getKey(),res.getKey().size,res.getValue());
        System.out.println(obj.getZ().getKey()+ " " + obj.getZ().getValue());
        System.out.println("Q pour arrêter le serveur.");
        Scanner scan = new Scanner(System.in);

        String answer=scan.nextLine();
        System.out.println(answer);
        //if(answer=="Q"){
            mainServer.chatServerDisconnection(obj.getZ());
            System.out.println(answer);
            return;
        //}

    }
}
