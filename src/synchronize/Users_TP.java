package synchronize;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;



/**
 * @author Svetlana
 * Users_TP - класс-сущность, который будем хранить в БД
 */
@Entity
@Table(name="USERS_TP")
public class Users_TP implements Serializable {
    
    protected Long id;    
    protected String name;    
    protected String passwd;

    public Users_TP() {
    }
    
    public Users_TP(String name, String passwd){
        this.name = name;
        this.passwd = passwd;
    }
    
    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    @Column(name="id")
    public Long getId() {
        return id;
    }
    
    @Column(name="name")
    public String getName() {
        return name;
    }
    
    @Column(name="passwd")
    public String getPasswd() {
        return passwd;
    }
    
    public void setId(Long i) {
        id = i;     
    }
    
    public void setName(String s) {
        name = s;
    }

    public void setPasswd(String s) {
        passwd = s;
    }

    @Override
    public boolean equals(Object o){
        if (!this.getClass().equals(o.getClass())) {
            return false;
        }
        if (!this.getName().equals(((Users_TP)o).getName())) {
            return false;
        }
        if (!this.getPasswd().equals(((Users_TP)o).getPasswd())) {
            return false;
        }
        return true;
    }
}

