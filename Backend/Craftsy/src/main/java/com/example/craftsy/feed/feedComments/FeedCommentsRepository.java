package com.example.craftsy.feed.feedComments;

import com.example.craftsy.feed.Feed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FeedCommentsRepository extends JpaRepository<FeedComments, Long> {
    List<FeedComments> findByFeedOrderByLikesDesc(Feed feed);
    @Transactional
    @Modifying
    @Query("DELETE FROM FeedComments c WHERE c.feed = :feed")
    void deleteAllByFeed(Feed feed);
}
