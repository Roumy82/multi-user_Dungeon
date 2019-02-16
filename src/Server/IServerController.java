package Server;

import javafx.util.Pair;

import java.rmi.RemoteException;

/**
 * Interface RMI du serveur controller, permettant de centraliser le contôle de tous les serveurs
 */
public interface IServerController extends java.rmi.Remote{
    public IGameServer findGameServer(Integer position) throws RemoteException;
    public IGameServer findGameServer(Integer position, String way) throws RemoteException;
    public IChatServer findChatServer(Integer position) throws RemoteException;
    public IChatServer findChatServer(Integer position, String way) throws RemoteException;
}
