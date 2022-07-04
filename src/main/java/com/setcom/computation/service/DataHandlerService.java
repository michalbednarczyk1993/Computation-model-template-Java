package com.setcom.computation.service;

import com.setcom.computation.model.MyModel;
import com.setcom.computation.repository.DataHandlerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataHandlerService {

    @Autowired
    private DataHandlerRepository repository;

    void create() {
        repository.save(new MyModel("1", "name"));
    }

    void delete(MyModel model) {
        repository.delete(model);
    }

    void deleteById(String id) {
        repository.deleteById(id);
    }


    void save(MyModel model) {
        repository.findBy()
        repository.save(model);
    }

}
