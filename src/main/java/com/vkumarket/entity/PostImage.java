package com.vkumarket.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "post_images")
public class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Post post;

    @Column(nullable = false)
    private String url;

    public PostImage() {}
    public PostImage(Post post, String url) { this.post = post; this.url = url; }

    public Long getId() { return id; }
    public Post getPost() { return post; }
    public String getUrl() { return url; }
}
