package com.nipun.system.document.trash;

import com.nipun.system.document.base.Document;
import com.nipun.system.document.branch.Branch;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "document_trash")
@Entity
public class Trash {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @OneToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "document_id")
    private Document document;

    @OneToOne(
            fetch = FetchType.LAZY
    )
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "added_date")
    public LocalDateTime added_date;
}
