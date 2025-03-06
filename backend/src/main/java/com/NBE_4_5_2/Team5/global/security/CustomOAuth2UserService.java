package com.NBE_4_5_2.Team5.global.security;

import com.NBE_4_5_2.Team5.domain.user.entity.User;
import com.NBE_4_5_2.Team5.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;

    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String oauthId = oAuth2User.getName();
        String providerType = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");

        String nickname = (String) properties.get("nickname");
        String profileImage = (String) properties.get("profile_image");
        String username = providerType + "__" + oauthId;

        Optional<User> opUser = userService.getUserByUsername(username);

        if (opUser.isPresent()) {
            User user = opUser.get();
            user.update(nickname);

            return new SecurityUser(user);
        }

        // 회원가입 구현
        User user = userService.createUser(username, "", "", nickname, "", profileImage);

        return new SecurityUser(user);
    }
}
