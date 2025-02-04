package vttp5a_paf.day28ws.service;

import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import vttp5a_paf.day28ws.repository.BoardGameRepository;

@Service
public class BoardGameService {
    
    @Autowired
    private BoardGameRepository boardGameRepository;

    public JsonObject getReviewsbByGameId(Integer gid) {
        Optional<Document> opt = boardGameRepository.getReviewsbByGameId(gid);
        
        if (opt.isEmpty()){
            JsonObject jObject = Json.createObjectBuilder()
                    .add("error", "The provided game ID does not exist!")
                    .build();
            return jObject;
        } else{
            Document game = opt.get();
            game.append("timestamp", LocalDateTime.now().toString());

            JsonObject gameObject = Json.createReader(new StringReader(game.toJson()))
                    .readObject();
            return gameObject;
        }
    }

    public JsonObject getHighestLowestGames(String order) {
        List<Document> gamesList = boardGameRepository.getHighestLowestGames(order);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Document d: gamesList) {
            JsonObject jObject = Json.createReader(new StringReader(d.toJson())).readObject();
            JsonArray jArray = jObject.getJsonArray("games");
            jsonArrayBuilder.add(jArray);
        }

        JsonObject result = Json.createObjectBuilder()
                .add("rating", order)
                .add("games", jsonArrayBuilder.build())
                .add("timestamp", LocalDateTime.now().toString())
                .build();

        return result;
    }
}
