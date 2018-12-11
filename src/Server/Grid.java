package Server;

public class Grid {
    int size;
    private Room board[][];

    public Grid(int size) {
        this.size = size;
        board = new Room[size][size];

        for (int i = 0; i < size*size; i++) {

            //On initialise les bordures de la futur pièce en fonction de sa position
            Border n,w,s,e;
            //Si on est sur les cases du haut on place un mur au nord
            n=(i<size) ? new Wall() : new Door(i-size, "h");
            //Si on est sur les cases du bord droit on place un mur à l'est
            e=(i%size==size-1) ? new Wall() : new Door(i+1, "v");
            //pareil pour le bord inférieur
            s=(i>=size*size-size) ? new Wall() : new Door(i+size, "h");
            //puis pour le bord gauche
            w=(i%size==0) ? new Wall() : new Door(i-1, "v");

            //On instantie la pièce
            Room current = new Room(i,n,w,s,e);

            //On l'ajoute au plateau
            board[i/size][i%size]=current;

        }
    }

    public void displayGrid(){
        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                System.out.print("x" + board[j][i].getNorth().toString() + board[j][i].getNorth().toString() + board[j][i].getNorth().toString() + "x");
            }
            System.out.println();
            for (int i = 0; i < size; i++) {
                //pour empecher le décalage si num case > 9
                if( board[j][i].getId() > 9)
                    System.out.print( board[j][i].getWest().toString() + " " + board[j][i].getId() + board[j][i].getEast().toString() );
                else
                    System.out.print( board[j][i].getWest().toString() + " " + board[j][i].getId() + " " + board[j][i].getEast().toString() );
            }
            if(j == size-1){
                System.out.println();
                for (int i = 0; i < size; i++) {
                    System.out.print("x" + board[j][i].getSouth().toString() + board[j][i].getSouth().toString() + board[j][i].getSouth().toString()+ "x");
                }
            }
            System.out.println();
        }
    }

    public Room getRoom(Integer x, Integer y){
        return board[x][y];
    }
}