package proj.concert.service.domain;

import proj.concert.common.dto.BookingRequestDTO;
import proj.concert.common.dto.UserDTO;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="USERS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private int userID;

    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "FIRSTNAME")
    private String firstname;

    @Column(name = "LASTNAME")
    private String lastname;

    @Column(name = "VERSION", nullable = false)
    private int version;

    @Column(name = "token")
    private String token;

    @Column(name = "tokenTimeStamp")
    private LocalDateTime tokenTimeStamp;

    public User() {
    }

    public UserDTO convertToDTO(){
        return new UserDTO(username, password);
    }

    public int getID() {
        return userID;
    }

    public void setID(int id) {
        this.userID = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String _username) {
        this.username = _username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String newToken) {
        this.token = newToken;
    }

    public LocalDateTime getTokenTimeStamp() {
        return this.tokenTimeStamp;
    }

    public void setTokenTimeStamp(LocalDateTime tokenTimeStamp) {
        this.tokenTimeStamp = tokenTimeStamp;
    }

    private long subConcertID;
    private int threashold;

    public void setSubConcertID(long subConcertID) {
        this.subConcertID = subConcertID;
    }

    public void setThreashold(int threashold) {
        this.threashold = threashold;
    }

    public long getSubConcertID() {
        return subConcertID;
    }

    public int getThreashold() {
        return threashold;
    }
}
