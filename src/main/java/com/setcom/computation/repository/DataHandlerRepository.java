package com.setcom.computation.repository;


import com.setcom.computation.model.MyModel;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DataHandlerRepository extends MongoRepository<MyModel, String> { // <nazwa modelu, typ modelu>
    // moja wlasna metoda
    // User findUserByFirstName(String name);
}
