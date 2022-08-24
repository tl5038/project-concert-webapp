package proj.concert.service.domain;

import proj.concert.common.dto.ConcertDTO;
import proj.concert.common.dto.PerformerDTO;
import proj.concert.common.types.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "PERFORMERS")
public class Performer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;
    @Column(name = "NAME", nullable = false)
    private String name;
    @Column(name = "IMAGE_NAME", nullable = false)
    private String imageName;
    @Column(name = "BLURB", nullable = false, columnDefinition = "TEXT")
    private String blurb;

    @Enumerated(EnumType.STRING)
    @Column(name = "GENRE", nullable = false)
    private Genre genre;

    public Performer() { }

    public Performer(Long id, String name, String imageName, Genre genre) {
        this.id = id;
        this.name = name;
        this.imageName = imageName;
        this.genre = genre;
    }

    public Performer(String name, String imageName, Genre genre) {
        this(null, name, imageName, genre);
    }

    public PerformerDTO convertToDTO() {
        return new PerformerDTO(this.id, this.name, this.imageName, this.genre, this.blurb);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getBlurb() {
        return blurb;
    }

    public void setBlurb(String newBlurb) {
        this.blurb = newBlurb;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Performer, id: ");
        buffer.append(id);
        buffer.append(", name: ");
        buffer.append(name);
        buffer.append(", s3 image: ");
        buffer.append(imageName);
        buffer.append(", genre: ");
        buffer.append(genre.toString());

        return buffer.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Performer))
            return false;
        if (obj == this)
            return true;

        Performer rhs = (Performer) obj;
        return new EqualsBuilder().
                append(name, rhs.name).
                isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31).
                append(name).hashCode();
    }
}
