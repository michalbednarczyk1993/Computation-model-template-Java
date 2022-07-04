package com.setcom.computation.repository;

import com.mongodb.client.result.UpdateResult;
import com.setcom.computation.model.MyModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class TestRepositoryImpl implements TestRepository {

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public void UpdateItemQuantity(String itemName, float newQuantity) {
        Query query = new Query(Criteria.where("jsonKey").is(itemName));
        Update update = new Update();
        update.set("jsonKeyToUpdate", newQuantity);
        UpdateResult result = mongoTemplate.updateFirst(query, update, MyModel.class);

        if (result== null) {
            System.out.println("no documents updated");
        } else {
            System.out.println(result.getMatchedCount() + " tyle dokumentów było zaktualizowanych");
        }

    }
}
