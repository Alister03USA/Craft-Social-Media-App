package com.example.craftsy.feed.feedComments;

import com.example.craftsy.feed.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedCommentsRepository extends JpaRepository<FeedComments, Long> {
    List<FeedComments> findByFeedOrderByLikesDesc(Feed feed);

}
