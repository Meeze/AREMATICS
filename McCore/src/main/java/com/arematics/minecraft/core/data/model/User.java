package com.arematics.minecraft.core.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.WhereJoinTable;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Data
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
    private Timestamp lastJoin;
    private String lastIp;
    private Timestamp lastIpChange;
    @OneToOne
    @JoinColumn(name = "rank", referencedColumnName = "id")
    private Rank rank;
    @OneToOne
    @JoinColumn(name = "display_rank", referencedColumnName = "id")
    private Rank displayRank;
    @WhereJoinTable(clause = "until IS NULL OR until > NOW()")
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_permission", joinColumns = {@JoinColumn(name = "uuid")},
            inverseJoinColumns = { @JoinColumn(name = "permission")})
    private Set<Permission> userPermissions;
}
