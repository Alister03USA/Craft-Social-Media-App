package com.example.craftsy.boards;

import com.example.craftsy.SignUpDelete.Entity.Users;
import com.example.craftsy.SignUpDelete.Repository.UserRepository;
import com.example.craftsy.Tutorial.Entity.Tutorial;
import com.example.craftsy.Tutorial.Repository.TutorialRepository;
import com.example.craftsy.feed.Feed;
import com.example.craftsy.feed.FeedRepository;
import com.example.craftsy.patterns.Patterns;
import com.example.craftsy.patterns.PatternsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class BoardController {
    @Autowired
    BoardRepository boardRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    PatternsRepository patternRepo;

    @Autowired
    FeedRepository projectRepo;

    @Autowired
    TutorialRepository tutorialRepository;

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

    /**
     * add a pattern
     * @param boardId
     * @param username
     * @param patternName
     * @return
     */
    @PutMapping("/board/{boardId}/pattern/{username}/{patternName}")
    Board addPattern(@PathVariable Long boardId, @PathVariable String username, @PathVariable String patternName){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternRepo.findByUserAndPatternName(user, patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.addPattern(pattern);
        return boardRepo.save(board);
    }

    /**
     * removes a pattern from the board
     * @param boardId
     * @param username
     * @param patternName
     * @return
     */
    @PutMapping("/board/{boardId}/pattern/{username}/{patternName}/delete")
    Board removePattern(@PathVariable Long boardId, @PathVariable String username, @PathVariable String patternName){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Patterns pattern = patternRepo.findByUserAndPatternName(user, patternName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.removePattern(pattern);
        return boardRepo.save(board);
    }

    /**
     * adds a project
     * @param boardId
     * @param username
     * @param projectName
     * @return
     */
    @PutMapping("/board/{boardId}/project/{username}/{projectName}")
    Board addProject(@PathVariable Long boardId, @PathVariable String username, @PathVariable String projectName){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = projectRepo.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.addProject(project);
        return boardRepo.save(board);
    }

    /**
     * removes a project
     * @param boardId
     * @param username
     * @param projectName
     * @return
     */
    @PutMapping("/board/{boardId}/project/{username}/{projectName}/delete")
    Board removeProject(@PathVariable Long boardId, @PathVariable String username, @PathVariable String projectName){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Feed project = projectRepo.findByUserAndProjectName(user, projectName)
                .orElseThrow(() -> new RuntimeException("Pattern not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.removeProject(project);
        return boardRepo.save(board);
    }

    /**
     * add tutorial to board
     * @param boardId
     * @param tutorialId
     * @return
     */
    @PutMapping("/board/{boardId}/tutorial/{tutorialId}")
    Board addTutorial(@PathVariable Long boardId, @PathVariable Long tutorialId){
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.addTutorial(tutorial);
        return boardRepo.save(board);
    }

    /**
     * delete tutorial from board
     * @param boardId
     * @param tutorialId
     * @return
     */
    @PutMapping("/board/{boardId}/tutorial/{tutorialId}/delete")
    Board deleteTutorial(@PathVariable Long boardId, @PathVariable Long tutorialId){
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new RuntimeException("Tutorial not found"));
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.removeTutorial(tutorial);
        return boardRepo.save(board);
    }

    /**
     * gets board
     * @param boardId
     * @return
     */
    @GetMapping("/board/{boardId}")
    Board getBoard(@PathVariable Long boardId){
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        return board;
    }

    /**
     * gets all users boards
     * @param username
     * @return
     */
    @GetMapping("/board/user/{username}")
    List<Board> getUsersBoards(@PathVariable String username){
        Users user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Optional<List<Board>> optBoards = boardRepo.findByUser(user);
        List<Board> boards = new ArrayList<>();
        if(optBoards.isPresent()){
            boards = optBoards.orElseThrow();
        }
        return boards;
    }

    /**
     * edits description of board
     * @param description
     * @param boardId
     * @return
     */
    @PutMapping("/board/{boardId}/description")
    Board editDescription(@RequestBody String description, @PathVariable Long boardId){
        Board board = boardRepo.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found"));
        board.setDescription(description.replaceAll("^\"|\"$", ""));
        return boardRepo.save(board);
    }
}
