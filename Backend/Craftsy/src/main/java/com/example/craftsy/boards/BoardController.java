package com.example.craftsy.boards;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
public class BoardController {
    @Autowired
    BoardRepository boardRepo;

    @Autowired
    UserRepository userRepo;

    /**
     * Creates a board for username with given boardName and description
     * @param username
     * @param boardBody
     * @return
     */
    @PostMapping("/board/create/{username}")
    Board createBoard(@PathVariable String username, @RequestBody Board boardBody){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boardBody.setDateCreated(LocalDateTime.now());
        boardBody.setUser(user);
        return boardRepo.save(boardBody);
    }

    /**
     * Deletes board with boardId
     * @param boardId
     * @return
     */
    @DeleteMapping("/board/{boardId}")
    String deleteBoard(@PathVariable Long boardId){
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        boardRepo.delete(board);
        return "board deleted";
    }
}
