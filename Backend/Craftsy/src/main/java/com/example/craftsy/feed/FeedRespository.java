package com.example.craftsy.feed;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedRespository extends JpaRepository<Feed, Long> {
}
