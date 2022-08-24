package proj.concert.service.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javax.persistence.*;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.ConcertSummaryDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.common.jackson.LocalDateTimeDeserializer;
import proj.concert.common.jackson.LocalDateTimeSerializer;

@Entity
@Table(name = "CONCERTS")
public class Concert implements Comparable<Concert> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;
    @Column(name = "TITLE", nullable = false)
    private String title;
    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageName;
    @Column(name = "BLURB", columnDefinition = "TEXT")
    private String blrb;

    @ElementCollection
    @CollectionTable(
            name = "CONCERT_DATES",
            joinColumns = @JoinColumn(name = "CONCERT_ID")
    )
    @Column(name = "DATE")
    private Set<LocalDateTime> dates = new HashSet<>();

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "CONCERT_PERFORMER",
            joinColumns = @JoinColumn(name = "CONCERT_ID"),
            inverseJoinColumns = @JoinColumn(name = "PERFORMER_ID")
    )
    private Set<Performer> performers = new HashSet<>();

    public Concert(Long id, String title, LocalDateTime date, Performer performer) {
        this.id = id;
        this.title = title;
        this.addDate(date);
        this.addPerformer(performer);
    }

    public Concert(String title, LocalDateTime date, Performer performer) {
        this(null, title, date, performer);
    }

    public Concert() {
    }

    public ConcertDTO convertToDTO() {
        List<PerformerDTO> performersDTO = new ArrayList<>();
        ConcertDTO newDTO = new ConcertDTO(this.id, this.title, this.imageName, this.blrb);

        for (Performer p : performers) {
            performersDTO.add(p.convertToDTO());
        }
        newDTO.setPerformers(performersDTO);
        newDTO.setDates(new ArrayList<>(this.dates));
        return newDTO;
    }

    public ConcertSummaryDTO convertToSummaryDTO() {
        return new ConcertSummaryDTO(this.id, this.title, this.imageName);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String newImage) {
        this.imageName = newImage;
    }

    public String getBlrb() {
        return blrb;
    }

    public void setBlrb(String newblrb) {
        this.blrb = newblrb;
    }

    @JsonSerialize(contentUsing = LocalDateTimeSerializer.class)
    @JsonDeserialize(contentUsing = LocalDateTimeDeserializer.class)
    public Set<LocalDateTime> getDates() {
        return this.dates;
    }

    public void addDate(LocalDateTime date) {
        this.dates.add(date);
    }
    public void removeDate(LocalDateTime date) {
        this.dates.remove(date);
    }

    public Set<Performer> getPerformers() {
        return performers;
    }

    public void addPerformer(Performer performer) {
        this.performers.add(performer);
    }
    public void removePerformer(Performer performer) {
        this.performers.remove(performer);
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Concert, id: ");
        buffer.append(id);
        buffer.append(", title: ");
        buffer.append(title);
        buffer.append(", dates: [");
        for (LocalDateTime date : dates) {
            buffer.append(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
            buffer.append(", ");
        }
        buffer.append("], featuring: [");
        for (Performer performer: performers) {
            buffer.append(performer.getName());
            buffer.append(", ");
        }
        buffer.append("]");

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        // Implement value-equality based on a Concert's title alone. ID isn't
        // included in the equality check because two Concert objects could
        // represent the same real-world Concert, where one is stored in the
        // database (and therefore has an ID - a primary key) and the other
        // doesn't (it exists only in memory).
        if (!(obj instanceof Concert))
            return false;
        if (obj == this)
            return true;

        Concert rhs = (Concert) obj;
        return new EqualsBuilder().
                append(title, rhs.title).
                isEquals();
    }

    @Override
    public int hashCode() {
        // Hash-code value is derived from the value of the title field. It's
        // good practice for the hash code to be generated based on a value
        // that doesn't change.
        return new HashCodeBuilder(17, 31).
                append(title).hashCode();
    }

    @Override
    public int compareTo(Concert concert) {
        return title.compareTo(concert.getTitle());
    }
}
