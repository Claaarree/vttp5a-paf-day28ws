package vttp5a_paf.day28ws.repository;

import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.Fields;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LimitOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;

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

    //     db.games.aggregate([
    //     {$lookup: {
    //         from: 'comments',
    //         foreignField: 'gid',
    //         localField: 'gid',
    //         as: 'reviews',
    //         pipeline: [
    //             {$sort: {rating: -1}},
    //             {$project: {review_id: '$_id', c_text: 1, user: 1, _id: 0, rating: 1}}
    //             ]
    //         }
    //     },
    //     {$unwind: '$reviews'},
    //     {$group: {_id: '$gid', 
    //         games: {$push: {_id: '$gid', 
    //         name: '$name', 
    //         rating: '$reviews.rating', 
    //         user: '$reviews.user', 
    //         comment: '$reviews.c_text', 
    //         review_id: '$reviews.review_id'}}}
    //     }
    // ])
    public List<Document> getHighestLowestGames(String order) {
        SortOperation sortByRanking = Aggregation.sort(Sort.by("rating"));
        if(order.equalsIgnoreCase("highest")){
            sortByRanking.and(Direction.ASC, "rating");
            System.out.println("in highest");
        } else{
            sortByRanking.and(Direction.DESC, "rating");
            System.out.println("in lowest");

        }

        ProjectionOperation commentsProjection = Aggregation
                .project("c_text", "user", "rating")
                .and("_id").as("review_id")
                .andExclude("_id");

        LookupOperation getCommentsForGames = LookupOperation.newLookup()
                .from("comments")
                .localField("gid")
                .foreignField("gid")
                .pipeline(sortByRanking, commentsProjection)
                .as("reviews");

        UnwindOperation unwindReviews = Aggregation.unwind("reviews");

        GroupOperation groupComments = Aggregation.group("gid")
                .push(new BasicDBObject()
                .append("_id", "$gid")
                .append("name", "$name")
                .append("rating", "$reviews.rating")
                .append("user", "$reviews.user")
                .append("comment", "$reviews.c_text")
                .append("review_id", "$reviews.review_id"))
                .as("games");

        LimitOperation limit = Aggregation.limit(5);

        Aggregation pipeline = Aggregation.newAggregation(getCommentsForGames, unwindReviews, groupComments, limit);
        
        return template.aggregate(pipeline, "games", Document.class).getMappedResults();
    }
}
