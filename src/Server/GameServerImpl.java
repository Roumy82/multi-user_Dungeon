package Server;

import DataBase.DataBaseLink;
import Client.Avatar;
import Client.IPlayer;
import Server.Server_Interface.IGameServerManagement;
import Server.Server_Interface.IServerControllerServerSide;
import javafx.util.Pair;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Serveur de jeu
 * Il implémente l'interface RMI possédant toutes ses méthodes.
 * Le code interne de chaque méthode est décrit dans la classe GameServerSimple
 */
public class GameServerImpl extends UnicastRemoteObject implements IGameServerManagement {

    private static GameServerSimple gs;
    private DataBaseLink dbl = new DataBaseLink();

    protected GameServerImpl() throws RemoteException {
        super();
    }

    /**
     * Connection d'un joueur sur le serveur
     * @param avUsed
     *              Avatar du joueur
     * @param position
     *              Position de l'avatar sur le plateau
     * @param player
     *              Identifiant du player
     * @return
     * @throws RemoteException
     */
    @Override
    public int connection(Avatar avUsed, Integer position, IPlayer player) throws RemoteException{
        return gs.connection(avUsed, position, player);
    }

    /**
     * Méthode appelée lorsque le joueur s'échappe
     * @param avUsed
     *              Avatar du joueur
     * @param goTo
     *              Direction de l'échappatoire
     * @return
     * @throws RemoteException
     */
    @Override
    public int escape(Avatar avUsed, String goTo) throws RemoteException{
        return gs.escape(avUsed, gs.getPosition(avUsed), goTo);
    }

    /**
     * Méthode appelée lorsque que le joueur déplace son avatar
     * @param avUsed
     *              Avatar du joueur
     * @param goTo
     *              Direction du déplacement
     * @return
     * @throws RemoteException
     */
    @Override
    public int move(Avatar avUsed, String goTo) throws RemoteException{
        int res = 0;
        try {
            res = gs.move(avUsed, goTo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(res);
        return res;
    }


    /**
     * Méthode appelée lorsque le joueur attaque un autre joueur
     * @param ifAvatar
     *              Avatar à attaquer
     * @param attacker
     *              Avatar attaquant
     * @param lifeLosed
     *              Puissance de l'attaque
     * @return
     * @throws RemoteException
     */
    @Override //Attaque d'un joueur sur un autre joueur
    public int attackAvatar(Avatar ifAvatar, Avatar attacker, int lifeLosed) throws RemoteException {
        try {
            return gs.attackAvatar(ifAvatar, attacker, lifeLosed);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Méthode appelée lorsque le joueur attaque le monstre de la case
     * @param attacker
     *              Avatar attaquant
     * @param position
     *              Position actuelle de l'avatar du joueur
     * @param lifeLosed
     *              Puissance de l'attaque
     * @return
     * @throws RemoteException
     */
    @Override //Attaque d'un joueur sur un monstre
    public int attackM(Avatar attacker, Integer position, int lifeLosed) throws RemoteException {
        try {
            return gs.attackM(attacker, position, lifeLosed);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Méthode appelée à chaque tour et permettant au monstre d'attaquer un avatar
     * @param target
     *              Avatar cible
     * @param position
     *              Position du monstre attaquant
     * @param lifeLosed
     *              Puissance de l'attaque
     * @return
     * @throws RemoteException
     */
    @Override //Attaque d'un monstre sur un joueur
    public int attackMonster(Avatar target, Integer position, int lifeLosed) throws RemoteException {
        return gs.attackMonster(target, position, lifeLosed);
    }


    /**
     * Affiche des informations sur la partie
     * @throws RemoteException
     */
    @Override
    public void displayGameInfo() throws RemoteException {
        gs.displayGameInfo();
    }

    /**
     * Permet de mettre la zone souhaitée à jour suite à des changements
     * @param z
     *              Zone à update
     * @throws RemoteException
     */
    @Override
    public void updateZone(Zone z) throws RemoteException {
        gs.setZ(z);
        System.out.println(gs.getZ());
    }


    /**
     * Connexion au serveur MySQL
     */
    public void connectDB(){
        dbl.connectDB();
    }

    /**
     * Insertion de nouvelles données dans la BD
     * @param datas
     *          La donnée à insérer
     * @param table
     *          La table où insérer
     */
    public void insertDB(String datas, String table) {
        dbl.insertDB(datas,table);
    }

    /**
     * Mise à jour d'une donnée dans la BD
     * @param data1
     *          La donnée que l'on veut modifier
     * @param data2
     *          La nouvelle valeur de la donnée
     * @param table
     *          La table où se trouve la donnée
     * @param option1
     *          Donnée qui sert d'indice dans la table pour savoir où modifier la donnée (après le WHERE)
     * @param option2
     *          Valeur de l'indice voulu
     */
    public void updateDB(String data1, String data2, String table, String option1, String option2) {
        String datas = data1 + "=" + data2;
        String options = option1 + "=" + option2;
        dbl.updateDB(datas,table,options);
    }

    /**
     * Recherche dans la BD
     * @param datas
     *          Donnée dont on veut la valeur
     * @param table
     *          Table où elle se trouve
     * @param option1
     *
     * @param option2
     */
    public void searchDB(String datas, String table, String option1, String option2) {
        String options = option1 + "=" + option2;
        dbl.searchDB(datas,table,options);
    }


    @Override
    public Pair<Room, Entity> getRoomInfo(Entity avatar) throws RemoteException{
        return gs.getRoomInfo(avatar);
    }

    public static void main(String args[]) throws Exception {
        //Démarre le rmiregistry
        //LocateRegistry.createRegistry(1099);
        GameServerImpl obj = new GameServerImpl();
        IServerControllerServerSide mainServer = (IServerControllerServerSide) Naming.lookup("//localhost/Dungeon");
        Pair<Grid,Zone> res = mainServer.gameServerConnection(obj);
        gs = new GameServerSimple(res.getKey(),res.getKey().size,res.getValue());
        System.out.println(gs.getZ().getKey()+ " " + gs.getZ().getValue());
        Thread thread = new Thread(gs);
        thread.start();
        //Naming.rebind("Dungeon", obj);
        System.out.println("Server.GameServerImpl launched");

        /*obj.connectDB();
        obj.insertDB("(5,2)","Monstre");
        obj.insertDB("(6,4)","Monstre");
        obj.insertDB("(7,3)","Monstre");

        obj.searchDB("Vie","Monstre","Place","6");
        obj.updateDB("Vie","2","Monstre","Place","6");
        obj.searchDB("Vie","Monstre","Place","6");*/

        //Scanner scan = new Scanner(System.in);
        //String answer=scan.nextLine();
        //if(answer=="Q"){
        //mainServer.gameServerDisconnection(gs.getZ());
        //System.out.println(answer);
        //exit();
        //}
    }

}
