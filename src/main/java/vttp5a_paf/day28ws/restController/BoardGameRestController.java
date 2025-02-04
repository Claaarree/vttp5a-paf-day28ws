package vttp5a_paf.day28ws.restController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.JsonObject;
import vttp5a_paf.day28ws.service.BoardGameService;

@RestController
@RequestMapping("/api")
public class BoardGameRestController {

    @Autowired
    private BoardGameService boardGameService;

    @GetMapping(path = "/game/{game_id}/reviews", produces = "application/json")
    public ResponseEntity<String> getReviewsByGameId(@PathVariable (name = "game_id") Integer gid) {
        JsonObject jObject = boardGameService.getReviewsbByGameId(gid);
        return checkJObject(jObject);
    }

    @GetMapping(path = "/games/{order}", produces = "application/json")
    public ResponseEntity<String> getHighestLowestGames(@PathVariable String order) {
        JsonObject jObject = boardGameService.getHighestLowestGames(order);

        return ResponseEntity.ok().body(jObject.toString());
    }

    public ResponseEntity<String> checkJObject(JsonObject jObject) {
        if (jObject.containsKey("error")){
            return ResponseEntity.badRequest().body(jObject.toString());
        } else {
            return ResponseEntity.ok().body(jObject.toString());
        }
    }
    
}
