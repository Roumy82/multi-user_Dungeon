package Server;
import java.io.Serializable;
import java.util.Objects;

/**
 * Classe mère des différentes entités du serveur de jeu (Avatar, Monstres)
 */
public abstract class Entity implements Serializable, Cloneable {

    protected final String name;
    protected Integer lifePoint;
    protected Integer maxLifePoint;
    protected Integer position;
    protected boolean isInLife;
    protected long uid;

    /**
     * Constructeur de la classe
     */
    public Entity(){
        uid=Long.MIN_VALUE + ((long) Math.random() * (Long.MAX_VALUE - Long.MIN_VALUE));
        name="Boo";
        isInLife=true;
        maxLifePoint=10;
    }

    /**
     * Redéfinition la méthode équals pour comparer deux entités
     * @param obj
     * @return
     */
    //On redéfinis la méthode equals pour pouvoir comparer des Entité
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        // null check
        if (obj == null)
            return false;
        // type check and cast
        if (getClass() != obj.getClass())
            return false;

        Entity ent = (Entity) obj;
        // field comparison
        return Objects.equals(name, ent.name);
    }

    /**
     * Redéfinition de la fonction hashCode
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 0,i = 0;
        for(char a : name.toCharArray()){
            i++;
            hash = hash + ((int) a * 3)* i;
        }
        return hash;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Entity(String name){
        this.name = name;
        isInLife = true;
    }

    /**
     * Récupère le nome de l'entité
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Récupère le nombre de points de vie de l'entité
     * @return
     */
    public Integer getLifePoint() {
        return lifePoint;
    }

    /**
     * Récupère le nombre maximal de point de vie de l'entité
     * @return
     */
    public Integer getMaxLifePoint() {
        return maxLifePoint;
    }

    /**
     * Permet de modifier le nombre de points de vie de l'entité
     * @param lifePoint
     *          Nouvelle valeur de la vie de l'entité
     */
    public void setLifePoint(int lifePoint) {
        this.lifePoint = lifePoint;
    }

    /**
     * Modifie la valeur de MaxLifePoint
     * @param maxLifePoint
     */
    public void setMaxLifePoint(Integer maxLifePoint) {
        this.maxLifePoint = maxLifePoint;
    }

    /**
     * Récupère la position de l'entité sur le plateau
     * @return
     */
    public Integer getPosition() {
        return position;
    }

    public boolean isInLife() {
        return lifePoint>0;
    }

    /**
     * Permet de retirer de la vie à l'entité (dans un combat, lors d'un escape, etc)
     * @param lifeLosed
     *              Quantité de vie perdue
     * @return
     *              Retourne la nouvelle vie
     */
    public int loseLife(int lifeLosed) {
        if(!isInLife){
            System.out.println("Vous êtes déjà mort");
            return -1;
        }
        int currentLife = this.getLifePoint();
        this.setLifePoint(currentLife - lifeLosed);
        if(lifePoint<=0){
            isInLife=false;
            System.out.println("Vous êtes mort.");
        }
        return this.getLifePoint();
    }

    /**
     * Permet à une entité de restorer sa vie et se réanimer si nécessaire
     */
    public void restoreLife(){
        lifePoint=maxLifePoint;
        this.isInLife = true;
    }



}
