package com.elice.aurasphere.contents.repository;


import com.elice.aurasphere.contents.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long>, ImageCustomRepository {

}
