package com.mainapp.respository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mainapp.entity.User;


@Repository
public interface UserRespository extends JpaRepository<User,String>
{
  public User  findByUsernameAndPassword(String name, String password);
}
