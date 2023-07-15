package com.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.EXUser;
import com.example.entity.LoginRequest;
@Repository
public interface AuthenticationRepository extends MongoRepository<EXUser, String>{

	LoginRequest findByUserid(String id);
}