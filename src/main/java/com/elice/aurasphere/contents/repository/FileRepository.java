package com.elice.aurasphere.contents.repository;


import com.elice.aurasphere.contents.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<File, Long>, FileCustomRepository {

}
