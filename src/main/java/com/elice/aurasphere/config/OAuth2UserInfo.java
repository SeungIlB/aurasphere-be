package com.elice.aurasphere.config;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;
    protected String provider;  // provider 정보 추가

    public OAuth2UserInfo(Map<String, Object> attributes, String provider) {
        this.attributes = attributes;
        this.provider = provider;
    }

    public abstract String getId();
    public abstract String getName();
    public abstract String getEmail();

    // 기본값을 포함한 닉네임 getter
    public String getNickname() {
        String nickname = getRawNickname();
        return nickname != null ? nickname : generateDefaultNickname();
    }

    // 기본값을 포함한 프로필 이미지 getter
    public String getImageUrl() {
        String imageUrl = getRawImageUrl();
        return imageUrl != null ? imageUrl : getDefaultProfileImage();
    }

    // 각 Provider별 실제 닉네임을 가져오는 추상 메서드
    protected abstract String getRawNickname();

    // 각 Provider별 실제 이미지 URL을 가져오는 추상 메서드
    protected abstract String getRawImageUrl();

    // 기본 닉네임 생성
    protected String generateDefaultNickname() {
        return provider + "User" + getId().substring(0, Math.min(getId().length(), 8));
    }

    // 기본 프로필 이미지 URL
    protected String getDefaultProfileImage() {
        return "/images/default_profile.png";
    }
}

class NaverOAuth2UserInfo extends OAuth2UserInfo {
    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response"), "NAVER");
    }

    @Override
    public String getId() {
        return (String) attributes.get("id");
    }

    @Override
    public String getName() {
        return (String) attributes.get("name");
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email");
    }

    @Override
    protected String getRawNickname() {
        return (String) attributes.get("nickname");
    }

    @Override
    protected String getRawImageUrl() {
        return (String) attributes.get("profile_image");
    }
}

class KakaoOAuth2UserInfo extends OAuth2UserInfo {
    public KakaoOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes, "KAKAO");
    }

    @Override
    public String getId() {
        return String.valueOf(attributes.get("id"));
    }

    @Override
    public String getName() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("name");
    }

    @Override
    public String getEmail() {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        if (kakaoAccount == null) {
            return null;
        }
        return (String) kakaoAccount.get("email");
    }

    @Override
    protected String getRawNickname() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("nickname");
    }

    @Override
    protected String getRawImageUrl() {
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
        if (properties == null) {
            return null;
        }
        return (String) properties.get("profile_image");
    }
}
