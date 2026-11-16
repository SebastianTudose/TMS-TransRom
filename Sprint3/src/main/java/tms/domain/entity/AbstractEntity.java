package tms.domain.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import tms.persistence.listener.AuditEntityListener;

/** Superclasă a entităților: identificator, versiune (optimistic locking) și metadate de audit. */
@MappedSuperclass
@EntityListeners(AuditEntityListener.class)
public abstract class AbstractEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    @Column(name = "created_by", updatable = false, length = 254)
    private String createdByUser;

    @Column(name = "updated_by", length = 254)
    private String updatedByUser;

    @Column(name = "date_created", updatable = false)
    private LocalDateTime dateCreated;

    @Column(name = "date_updated")
    private LocalDateTime dateUpdated;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getCreatedByUser() { return createdByUser; }
    public void setCreatedByUser(String createdByUser) { this.createdByUser = createdByUser; }

    public String getUpdatedByUser() { return updatedByUser; }
    public void setUpdatedByUser(String updatedByUser) { this.updatedByUser = updatedByUser; }

    public LocalDateTime getDateCreated() { return dateCreated; }
    public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated = dateCreated; }

    public LocalDateTime getDateUpdated() { return dateUpdated; }
    public void setDateUpdated(LocalDateTime dateUpdated) { this.dateUpdated = dateUpdated; }


    @Override
    public boolean equals(Object obj) {
        if (id == null) return super.equals(obj);
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return id.equals(((AbstractEntity) obj).id);
    }

    @Override
    public int hashCode() { return id == null ? super.hashCode() : 31 + id.hashCode(); }

}
