package com.example.craftsy.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedController {

    @Autowired
    FeedRespository feedRespository;

    
}
