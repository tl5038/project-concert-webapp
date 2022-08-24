package proj.concert.service.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import proj.concert.common.dto.SeatDTO;
import proj.concert.common.types.BookingStatus;

@Entity
@Table(name = "SEATS")
public class Seat {
	@Column(name = "reservedTimestamp", nullable = true)
	private LocalDateTime reservedTimestamp;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)
	private Concert concert;

	@Column(name = "seatLabel")
	private String seatLabel;

	@Column(name = "date")
	private LocalDateTime date;

	@Column(name = "cost")
	private BigDecimal cost;


	@Column(name = "status")
	@Enumerated(EnumType.STRING)
	private BookingStatus bookedStatus = BookingStatus.Unbooked;

	@Version
	private Integer version;


	public Seat(String seatLabel, boolean isBooked, LocalDateTime date, BigDecimal cost) {
		if (isBooked){
			bookedStatus = BookingStatus.Booked;
		} else {
			bookedStatus = BookingStatus.Unbooked;
		}

		this.seatLabel = seatLabel;
		this.date = date;
		this.cost = cost;
	}

	public LocalDateTime getReservedTimestamp() {
		return reservedTimestamp;
	}

	public void setReservedTimestamp(LocalDateTime newTimestamp) {
		this.reservedTimestamp = newTimestamp;
	}

	public Seat() {}

	public SeatDTO convertToDTO() {
		return new SeatDTO(seatLabel, this.cost);
	}

	public Concert getConcert() {
		return concert;
	}

	public void setConcert(Concert concert) {
		this.concert = concert;
	}

	public LocalDateTime getDate(){
		return this.date;
	}

	public void setDate(LocalDateTime newDate){
		this.date = newDate;
	}


	public String getSeatLabel() {
		return seatLabel;
	}

	public void setSeatLabel(String seatLabel) {
		this.seatLabel = seatLabel;
	}

	public BookingStatus getStatus() {
		return bookedStatus;
	}

	public void setStatus(BookingStatus bookedStatus) {
		this.bookedStatus = bookedStatus;
	}
}
