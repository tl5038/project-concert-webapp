package proj.concert.service.domain;

import proj.concert.common.dto.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="BOOKINGS")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "concert", nullable = false)
    private Concert concert;

    @Column(name = "datetime")
    private LocalDateTime bookingDateTime;

    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Seat> seats;

    public Booking() {
    }

    public BookingDTO convertToDTO(){
        List<SeatDTO> seatsDTO = new ArrayList<>();

        for (Seat s : seats) {
            seatsDTO.add(s.convertToDTO());
        }

        return new BookingDTO(this.id, this.bookingDateTime, seatsDTO);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Concert getConcert() {
        return concert;
    }

    public void setConcert(Concert concert) {
        this.concert = concert;
    }

    public LocalDateTime getDateTime() {
        return bookingDateTime;
    }

    public void setDateTime(LocalDateTime reservationDateTime) {
        this.bookingDateTime = reservationDateTime;
    }

    public Set<Seat> getSeats() {
        return seats;
    }

    public void setSeats(Set<Seat> seats) {
        this.seats = seats;
    }
}
