package com.arematics.minecraft.data.global.model;

import com.arematics.minecraft.data.share.model.Permission;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.WhereJoinTable;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Audited
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
    @NotAudited
    private String lastIp;
    private Timestamp lastIpChange;
    @OneToOne
    @JoinColumn(name = "rank", referencedColumnName = "id")
    private Rank rank;
    @OneToOne
    @JoinColumn(name = "display_rank", referencedColumnName = "id")
    private Rank displayRank;
    @NotAudited
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_configurations", joinColumns = @JoinColumn(name = "uuid"))
    @MapKeyColumn(name = "name")
    private Map<String, Configuration> configurations;
    @NotAudited
    @WhereJoinTable(clause = "until IS NULL OR until > NOW()")
    @OneToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_permission", joinColumns = {@JoinColumn(name = "uuid")},
            inverseJoinColumns = { @JoinColumn(name = "permission")})
    private Set<Permission> userPermissions;
}