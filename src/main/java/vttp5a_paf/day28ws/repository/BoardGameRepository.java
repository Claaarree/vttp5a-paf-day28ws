package vttp5a_paf.day28ws.repository;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

@Repository
public class BoardGameRepository {

    @Autowired
    private MongoTemplate template; 
    
    // db.games.aggregate([
    //     {$match: {gid: 1}},
    //     {$lookup: {
    //         from: 'comments',
    //         foreignField: 'gid',
    //         localField: 'gid',
    //         as: 'reviews'
    //         }
    //     },
    //     {$project: { gid: '$gid', 
    //                 name: '$name',
    //                 year: '$year',
    //                 rank: '$ranking',
    //                 average: {$avg: '$reviews.rating'},
    //                 users_rated: '$users_rated',
    //                 url: '$url',
    //                 thumbnail: '$image',
    //                 reviews: '$reviews'
    //                 }
    //     }
    // ])
    public Optional<Document> getReviewsbByGameId(Integer gid) {
        Criteria criteria = Criteria.where("gid")
                .is(gid);
        MatchOperation matchGid = Aggregation.match(criteria);

        LookupOperation getReviewsForGame = Aggregation
        .lookup("comments", "gid", "gid", "reviews");

        // how to get $avg?? 
        ProjectionOperation projectResults = Aggregation
                .project("gid", "name", "year","rank", "users_rated", "url", "reviews")
                .and("image").as("thumbnail");

        Aggregation pipeline = Aggregation.newAggregation(matchGid, getReviewsForGame, projectResults);

        Document result = template.aggregate(pipeline, "games", Document.class).getUniqueMappedResult();

        return Optional.ofNullable(result);
    }
}
