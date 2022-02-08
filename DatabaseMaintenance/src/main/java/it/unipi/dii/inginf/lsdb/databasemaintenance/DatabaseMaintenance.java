package it.unipi.dii.inginf.lsdb.databasemaintenance;

import com.mongodb.client.MongoCursor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseMaintenance {

    private static ConfigParams params;

    public static void main(String[] args) {
        try {
            params = ConfigParams.getInstance();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("fetching courses...");
        MongoDBDriver driver = MongoDBDriver.getInstance();
        MongoCursor<Course> toCheck = driver.fetchBigDocuments(params.getMaxReviews());

        while (toCheck.hasNext()) {
            Course c = toCheck.next();
            System.out.println("processing course " + c.getId().toString());
            Course updated = discardSomeReviews(c);
            driver.updateReviews(updated.getId(), c.getReviews());
            System.out.println("updated course " + c.getId().toString());
        }

        driver.closeConnection();
    }

    private static Course discardSomeReviews(Course c) {
        List<Review> reviews = c.getReviews();
        int count = 0;
        int minRating = c.getSum_ratings() / c.getNum_reviews();
        int maxRating;
        if (minRating == 5)
            maxRating = 5;
        else
            maxRating = minRating + 1;

        List<Review> toDelete = new ArrayList<>();
        for (Review r: reviews) {
            if (count >= params.getMaxReviewsAfterDiscard()) {
                toDelete.add(r);
                continue;
            }

            count++;
            if ((r.getRating() != minRating) && (r.getRating() != maxRating))
                toDelete.add(r);
        }

        if (reviews.size() - toDelete.size() < params.getMaxReviewsAfterDiscard()) {
            return deleteOldReviews(c);
        }

        for (Review r: toDelete)
            reviews.remove(r);

        c.setReviews(reviews);
        return c;
    }

    public static Course deleteOldReviews(Course c) {
        List<Review> reviews = c.getReviews();
        reviews.sort(Review::compareTo);
        List<Review> toDelete = new ArrayList<>();
        for (int i = 0; i < reviews.size(); i++) {
            if (i > params.getMaxReviewsAfterDiscard())
                break;
            toDelete.add(reviews.get(i));
        }

        for (Review r: toDelete)
            reviews.remove(r);

        c.setReviews(reviews);
        return c;
    }
}
