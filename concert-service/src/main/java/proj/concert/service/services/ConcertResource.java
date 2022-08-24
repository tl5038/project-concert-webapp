package proj.concert.service.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import proj.concert.common.dto.*;
import proj.concert.common.types.BookingStatus;
import proj.concert.service.domain.*;

import proj.concert.service.util.*;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.awt.print.Book;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static proj.concert.service.util.TheatreLayout.createSeatsFor;

@Path("/concert-service")
@Produces({
        javax.ws.rs.core.MediaType.APPLICATION_JSON
})
@Consumes({
        javax.ws.rs.core.MediaType.APPLICATION_JSON
})
public class ConcertResource {
    private static Logger LOGGER = LoggerFactory.getLogger(ConcertResource.class);

    // For subscriptions
    private final List<AsyncResponse> subs = new Vector<>();
    private List<User> subscribers;

    /* Concert Methods */

    @GET
    @Path("/concerts")
    public Response getConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
        List<Concert> concerts = concertQuery.getResultList();

        if (concerts.isEmpty()) {
            return Response.noContent().build();
        }

        Set<ConcertDTO> concertDTOs = new HashSet<>();
        for (Concert c : concerts) {
            concertDTOs.add(c.convertToDTO());
        }

        em.close();

        GenericEntity<Set<ConcertDTO>> wrapped = new GenericEntity<Set<ConcertDTO>>(concertDTOs) {
        };
        return Response.ok(wrapped).build();
    }

    @GET
    @Path("/concerts/summaries")
    public Response getConcertSummaries() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Concert> concertQuery = em.createQuery("select c from Concert c", Concert.class);
        List<Concert> concerts = concertQuery.getResultList();

        if (concerts.isEmpty()){
            return Response.noContent().build();
        }

        Set<ConcertSummaryDTO> concertSummaryDTO = new HashSet<>();
        for (Concert c : concerts){
            concertSummaryDTO.add(c.convertToSummaryDTO());
        }

        em.close();

        GenericEntity<Set<ConcertSummaryDTO>> wrapped = new GenericEntity<Set<ConcertSummaryDTO>>(concertSummaryDTO){};

        return Response.ok(wrapped).build();
    }

    @GET
    @Path("/concerts/{id}")
    public Response retrieveConcert(@PathParam("id") long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            Concert concert = em.find(Concert.class, id);
            transaction.commit();
            if (concert == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(concert.convertToDTO()).build();
        }catch(Exception e){
            return Response.status(Response.Status.NOT_FOUND).build();
        } finally {
            em.close();
        }
    }

    @POST
    public Response createConcert(ConcertDTO concert) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        em.persist(concert);
        em.getTransaction().commit();
        em.close();
        return Response.created(URI.create("/concerts/" + concert.getId())).build();
    }

    @PUT
    public Response updateConcert(ConcertDTO concert) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        ConcertDTO oldConcert = em.find(ConcertDTO.class, concert.getId());
        em.getTransaction().commit();

        if (oldConcert != null) {
            em.getTransaction().begin();
            em.merge(concert);
            em.getTransaction().commit();
            em.close();
            return Response.status(204).build();
        }
        em.close();
        return null;
    }

    @DELETE
    @Path("/concerts/{id}")
    public Response deleteConcert(@PathParam("id") long id) {

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        ConcertDTO concert = em.find(ConcertDTO.class, id);
        em.getTransaction().commit();
        if (concert != null) {
            em.getTransaction().begin();
            em.remove(concert);
            em.getTransaction().commit();
            em.close();
            return Response.noContent().build();
        }
        em.close();
        throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    @DELETE
    public Response deleteAllConcerts() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        em.clear();
        em.getTransaction().commit();
        em.close();
        return Response.noContent().build();
    }

    /* Performer Methods */

    @GET
    @Path("/performers/{id}")
    public Response retrievePerformer(@PathParam("id") long id) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        Performer performer = em.find(Performer.class, id);
        em.getTransaction().commit();
        em.close();
        if (performer == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return Response.ok(performer.convertToDTO()).build();
    }

    @GET
    @Path("/performers")
    public Response getPerformers() {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        TypedQuery<Performer> performerQuery = em.createQuery("select p from Performer p", Performer.class);
        List<Performer> performers = performerQuery.getResultList();

        if (performers.isEmpty()) {
            return Response.noContent().build();
        }

        Set<PerformerDTO> performerDTOS = new HashSet<>();
        for (Performer p : performers) {
            performerDTOS.add(p.convertToDTO());
        }

        em.close();

        GenericEntity<Set<PerformerDTO>> wrapped = new GenericEntity<Set<PerformerDTO>>(performerDTOS) {
        };
        return Response.ok(wrapped).build();
    }

    /* Seats Methods */
    @GET
    @Path("/seats/{LocalDateTime}")
    public Response getSeatsForDate(@PathParam("LocalDateTime") String seatDate, @QueryParam("status") BookingStatus seatStatus){
        //format localDateTime String to date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDateTime seatDateTime = LocalDateTime.parse(seatDate, formatter);
        //get all seats with set date

        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();

        List<Seat> seats = em.createQuery("select s FROM Seat s WHERE s.date = :queryDate", Seat.class)
                .setParameter("queryDate", seatDateTime).getResultList();
        List<SeatDTO> seatsDTO = new ArrayList<>();
        for (Seat seat: seats){
            if(seatStatus == BookingStatus.Any || seat.getStatus() == seatStatus) {
                seatsDTO.add(seat.convertToDTO());
            }
        }
        em.close();
        return Response.ok(new GenericEntity<List<SeatDTO>>(seatsDTO){}).build();
    }

    /* Booking Methods */
    @POST
    @Path("/bookings")
    public Response makeBookingRequest(BookingRequestDTO dto, @CookieParam("auth") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            Concert concert;
            //get Concert and ensure it exists
            try {
                TypedQuery<Concert> bookingQuery = em.createQuery("select c from Concert c where c.id=:concertID", Concert.class)
                        .setParameter("concertID", dto.getConcertId());
                concert = bookingQuery.getSingleResult();
            } catch (Exception e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (!concert.getDates().contains(dto.getDate())) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }

            //get user and ensure they exist
            User user;
            try {
                user = (User) em.createQuery("SELECT U FROM User U WHERE U.token = :token")
                        .setParameter("token", token.getValue()).getSingleResult();
            } catch (Exception e) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            em.getTransaction().commit();
            //get all the concerts seats. We need to create seats for concerts if none exist

            //get seats for set date
            em.getTransaction().begin();
            List<Seat> seats = em.createQuery("select s FROM Seat s WHERE s.date = :queryDate", Seat.class)
                    .setParameter("queryDate", dto.getDate())
                    .setLockMode(LockModeType.OPTIMISTIC).getResultList();
            //
            Set<Seat> SeatsToBeBooked = new HashSet<>();
            for (Seat seat : seats) {
                if (seat.getConcert() == null) {
                    //link seat with concert to allow for deletion when concert is deleted.
                    seat.setConcert(concert);
                }
                if ((dto.getSeatLabels().contains(seat.getSeatLabel()))) {
                    if (seat.getStatus() == BookingStatus.Booked) {
                        return Response.status(Response.Status.FORBIDDEN).build();
                    } else {
                        //add seats to the ToBeBooked list for the booking DTO
                        SeatsToBeBooked.add(seat);
                        seat.setStatus(BookingStatus.Booked);
                    }
                }
            }
            em.getTransaction().commit();
            //throws an error if 2 users tried to book any seat concurrently

            //create Booking
            em.getTransaction().begin();
            Booking booking = new Booking();
            booking.setConcert(concert);
            booking.setDateTime(dto.getDate());
            booking.setUser(user);
            //get the actual seats that we wanted to get
            booking.setSeats(SeatsToBeBooked);
            em.persist(booking);
            em.getTransaction().commit();

            notifySubscribers(user, concert);

            //Return URI to view booking
            return Response.created(URI.create("/concert-service/booking/" + booking.getId())).build();
        } catch (Exception e){
            return Response.status(Response.Status.FORBIDDEN).build();
        } finally {
            em.close();
        }
    }

    @GET
    @Path("/bookings")
    public Response getBookingsForUser(@CookieParam("auth") Cookie token){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        User user;
        //get User and ensure they exist
        try {
            user = em.createQuery("SELECT u  from User u WHERE u.token = :token", User.class)
                    .setParameter("token", token.getValue()).getSingleResult();
        } catch (Exception e){
            em.close();
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        //get the users bookings
        List<Booking> userBookings = em.createQuery("SELECT b  from Booking b WHERE b.user = :user", Booking.class)
                .setParameter("user", user).getResultList();
        List<BookingDTO> booksDTO = new ArrayList<>();
        for (Booking booking : userBookings){
            booksDTO.add(booking.convertToDTO());
        }
        em.close();
        return Response.ok(new GenericEntity<List<BookingDTO>>(booksDTO){}).build();
    }

    @GET
    @Path("/booking/{id}")
    public Response retrieveBooking(@PathParam("id") long id, @CookieParam("auth") Cookie token) {
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        Booking booking = em.find(Booking.class, id);

        //get user and ensure the user is the one who created the Booking
        User user = em.createQuery("SELECT u from User u WHERE u.token = :token", User.class)
              .setParameter("token", token.getValue()).getSingleResult();
        em.getTransaction().commit();

        em.close();
        if (booking == null || user == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        } else if (booking.getUser() != user) {
            return Response.status(Response.Status.FORBIDDEN).build();
        }

        BookingDTO bookingDTO = booking.convertToDTO();
        return Response.ok(new GenericEntity<BookingDTO>(bookingDTO) {}).build();

    }

    @POST
    @Path("/login")
    public Response loginRequest(UserDTO dto){
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        try {
            User user = em.createQuery("SELECT u from User u WHERE u.username = :username", User.class)
                    .setParameter("username", dto.getUsername())
                    .getSingleResult();
            if (user.getToken() == null) {
                //create a user Token for Token based Authentication
                UUID token = UUID.randomUUID();
                user.setToken(token.toString());
                user.setTokenTimeStamp(LocalDateTime.now());
                em.merge(user);
                em.getTransaction().commit();
            }
            //In future we could add the ability to check the Timestamp and make sure the session was recent
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else if (!dto.getPassword().equals(user.getPassword())){
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }

            return Response.ok(user.convertToDTO()).cookie(new NewCookie("auth", user.getToken())).build();

        } catch (Exception e) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        } finally {
            em.close();
        }
    }

    /* Subscription Methods*/

    private static final BlockingQueue<AsyncResponse> suspended =
            new ArrayBlockingQueue<AsyncResponse>(5);


    // Post a message to be received by all subscribers
    @POST
    @Path("/subscribe/concertInfo")
    public void postMessage(@Suspended AsyncResponse sub, ConcertInfoSubscriptionDTO message, @CookieParam("auth") Cookie token) {
        LOGGER.info("IN SUBSCRIBE POST METHOD");

        // Check user has authorisation to subscribe
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();
        Concert concert = em.find(Concert.class, message.getConcertId());
        em.getTransaction().commit();

        //check user exists if not, make Response.Status.UNAUTHORIZED
        if (concert == null) {
            sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
        } else if (!concert.getDates().contains(message.getDate())) {
            sub.resume(Response.status(Response.Status.BAD_REQUEST).build());
        } else if (token == null) {
            sub.resume(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            LOGGER.info("TESTING VALID SUBSCRIPTION (Concert ID: " + message.getConcertId() +
                    ", Date: " + message.getDate() +
                    ", %Booked: " + message.getPercentageBooked() + ")");

            em.getTransaction().begin();
            User user = em.createQuery("SELECT u from User u WHERE u.token = :token", User.class)
                    .setParameter("token", token.getValue()).getSingleResult();
            em.getTransaction().commit();

            em.getTransaction().begin();
            TypedQuery<Seat> seatQuery = em.createQuery("select s FROM Seat s WHERE s.concert.id=:concertID AND s.date = :queryDate", Seat.class)
                    .setParameter("concertID", message.getConcertId())
                    .setParameter("queryDate", message.getDate());
            List<Seat> seats = seatQuery.getResultList();
            em.getTransaction().commit();

            int numSeats = seats.size();
            int bookedSeats = 0;
            subscribers.add(user);
            user.setSubConcertID(message.getConcertId());
            user.setThreashold(message.getPercentageBooked());

            for (Seat seat : seats) {
                if (seat.getStatus() == BookingStatus.Booked) {
                    bookedSeats++;
                }
            }
            float percentBooked = (float) bookedSeats / numSeats * 100;
            LOGGER.info("Total seats: " + numSeats +
                    ", Booked seats: " + bookedSeats +
                    ", %Booked: " + percentBooked);
            ConcertInfoNotificationDTO notification = new ConcertInfoNotificationDTO(numSeats - bookedSeats);
            sub.resume(notification);
        }
    }

    @POST
    @Path("/subscribe/concertInfo")
    public void notifySubscribers(User subscriber, Concert concert) throws InterruptedException {
        LOGGER.info("IN SUBSCRIBE POST METHOD");
        EntityManager em = PersistenceManager.instance().createEntityManager();
        em.getTransaction().begin();


        // Checks if user is one of the subscribers, and if so attempts to notify them if their threshold is met
        for (User sub : subscribers) {
            if (subscriber == sub) {
                em.getTransaction().begin();
                TypedQuery<Seat> seatQuery = em.createQuery("select s FROM Seat s WHERE s.concert.id=:concertID AND s.date = :queryDate", Seat.class)
                        .setParameter("concertID", concert.getId())
                        .setParameter("queryDate", concert.getDates());
                List<Seat> seats = seatQuery.getResultList();
                em.getTransaction().commit();

                int numSeats = seats.size();
                int bookedSeats = 0;

                for (Seat seat : seats) {
                    if (seat.getStatus() == BookingStatus.Booked) {
                        bookedSeats++;
                    }
                }

                float percentBooked = (float) bookedSeats / numSeats * 100;

                if (percentBooked >= subscriber.getThreashold()) {
                    em.getTransaction().begin();
                    ConcertInfoNotificationDTO notificationDTO = new ConcertInfoNotificationDTO(bookedSeats);
                    final AsyncResponse ar = suspended.take();
                    ar.resume(notificationDTO);
                    em.getTransaction().commit();
                }
            }
        }

    }



    @GET
    @Path("/subscribe/concertInfo")
    public void readMessage(@Suspended AsyncResponse ar) throws InterruptedException {
        suspended.put(ar);
    }
}
