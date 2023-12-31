package com.arematics.minecraft.data.global.model;

import lombok.*;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Audited
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user")
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(name = "arematics_connection", nullable = false)
    private UUID arematicsConnection;
    @Id
    @Column(name = "uuid", nullable = false)
    @Type(type = "org.hibernate.type.UUIDCharType")
    private UUID uuid;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "last_join")
    private Timestamp lastJoin;
    @NotAudited
    @Column(name = "last_ip")
    private String lastIp;
    @Column(name = "last_ip_change")
    private Timestamp lastIpChange;
    @OneToOne
    @JoinColumn(name = "rank", referencedColumnName = "id")
    private Rank rank;
    @OneToOne
    @JoinColumn(name = "display_rank", referencedColumnName = "id")
    private Rank displayRank;
    @Column(name = "user_karma", nullable = false)
    private int karma;
    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_configurations", joinColumns = @JoinColumn(name = "uuid"))
    @MapKeyColumn(name = "name")
    private Map<String, Configuration> configurations;
    @NotAudited
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_friends", joinColumns = {@JoinColumn(name = "uuid")},
            inverseJoinColumns = { @JoinColumn(name = "target_uuid")})
    private Set<User> friends;
    @ManyToOne(fetch = FetchType.EAGER)
    @NotAudited
    private ChatTheme activeTheme;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
