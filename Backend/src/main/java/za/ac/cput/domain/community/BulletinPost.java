/*
 AuditLog.java

 AuditLog POJO class

 Author: Mogamat Yaseen Kannemeyer 240453182
 Date: 04 September 2026
*/
package za.ac.cput.domain.community;

import jakarta.persistence.*;
import za.ac.cput.domain.enums.BulletinPostStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "bulletin_post")
public class BulletinPost {

    //  Variables/Attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long bulletinPostId;

    @Column(nullable = false, name = "author_id")
    private long authorId;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BulletinPostStatus status;

    @Column(nullable = false, name = "is_faculty_announcement")
    private boolean isFacultyAnnouncement;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "removed_at")
    private LocalDateTime removedAt;

    //  Constructors
    protected BulletinPost() {
        // Required by JPA
    }

    private BulletinPost(Builder builder) {
        this.bulletinPostId = builder.bulletinPostId;
        this.authorId = builder.authorId;
        this.title = builder.title;
        this.content = builder.content;
        this.status = builder.status;
        this.isFacultyAnnouncement = builder.isFacultyAnnouncement;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.removedAt = builder.removedAt;
    }

    //  Getters
    public long getBulletinPostId() {
        return bulletinPostId;
    }

    public long getAuthorId() {
        return authorId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public BulletinPostStatus getStatus() {
        return status;
    }

    public boolean isFacultyAnnouncement() {
        return isFacultyAnnouncement;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    //  toString
    @Override
    public String toString() {
        return "BulletinPost{" +
                "bulletinPostId=" + bulletinPostId +
                ", authorId=" + authorId +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", isFacultyAnnouncement=" + isFacultyAnnouncement +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", removedAt=" + removedAt +
                '}';
    }

    //  Builder Class
    public static class Builder {

        //  Variables/Attributes
        private long bulletinPostId;
        private long authorId;
        private String title;
        private String content;
        private BulletinPostStatus status;
        private boolean isFacultyAnnouncement;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime removedAt;

        //  Setters
        public Builder setBulletinPostId(long bulletinPostId) {
            this.bulletinPostId = bulletinPostId;
            return this;
        }

        public Builder setAuthorId(long authorId) {
            this.authorId = authorId;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setStatus(BulletinPostStatus status) {
            this.status = status;
            return this;
        }

        public Builder setFacultyAnnouncement(boolean facultyAnnouncement) {
            isFacultyAnnouncement = facultyAnnouncement;
            return this;
        }

        public Builder setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder setRemovedAt(LocalDateTime removedAt) {
            this.removedAt = removedAt;
            return this;
        }

        public Builder copy(BulletinPost bulletinPost) {
            this.bulletinPostId = bulletinPost.bulletinPostId;
            this.authorId = bulletinPost.authorId;
            this.title = bulletinPost.title;
            this.content = bulletinPost.content;
            this.status = bulletinPost.status;
            this.isFacultyAnnouncement = bulletinPost.isFacultyAnnouncement;
            this.createdAt = bulletinPost.createdAt;
            this.updatedAt = bulletinPost.updatedAt;
            this.removedAt = bulletinPost.removedAt;
            return this;
        }

        //  build method
        public BulletinPost build() {
            return new BulletinPost(this);
        }
    }
}
