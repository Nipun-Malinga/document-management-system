package com.nipun.system.document;

import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shared_documents")
public class SharedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User sharedUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission")
    private Permission permission;

    public void addSharingData(User sharedUser, Document document, Permission permission) {
        this.setSharedUser(sharedUser);
        this.setDocument(document);
        this.setPermission(permission);
    }
}
