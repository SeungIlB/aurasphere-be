package com.elice.aurasphere.user.repository;

import com.elice.aurasphere.user.entity.Profile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    Optional<Profile> findByUserId(Long userId);

    boolean existsByNickname(String nickname);

    // 닉네임으로 검색 시 자신의 닉네임은 제외
    boolean existsByNicknameAndUserIdNot(@Param("nickname") String nickname, @Param("userId") Long userId);
}