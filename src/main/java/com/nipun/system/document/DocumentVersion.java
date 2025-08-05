package com.nipun.system.document;

import com.nipun.system.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_version")
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "document_id")
    private Document document;

    @Column(name = "version_number")
    private UUID versionNumber;

    @Column(name = "content")
    private String content;

    @ManyToOne
    @JoinColumn(name = "author")
    private User author;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    public void addData(Document document, User author, String content) {
        this.setAuthor(author);
        this.setDocument(document);
        this.setContent(content);
        this.setTimestamp(LocalDateTime.now());
        this.setVersionNumber(UUID.randomUUID());
    }
}
