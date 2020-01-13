package com.jojoldu.book.springboot.config.auth;

import com.jojoldu.book.springboot.config.auth.dto.OAuthAttributes;
import com.jojoldu.book.springboot.config.auth.dto.SessionUser;
import com.jojoldu.book.springboot.domain.user.User;
import com.jojoldu.book.springboot.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.Collections;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final HttpSession httpSession;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService delegate = new DefaultOAuth2UserService();
        OAuth2User oAuth2User = delegate.loadUser(userRequest);

        // 현재 로그인 진행 중인 서비스를 구분하는 코드 (구글/네이버/...)
        String registrationId = userRequest
                .getClientRegistration()
                .getRegistrationId();
        log.debug(String.format("CustomOAuth2UserService.loadUser의 registrationId: $s", registrationId));

        /*
        로그인 진행 시 키가되는 필드값 (Primary Key)
        구글의 기본 코드는 "sub" 이다. (구글/네이버/.. 로그인을 동시 진행할 때 필요함)
         */
        String userNameAttributeName = userRequest
                .getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();
        log.debug(String.format("CustomOAuth2UserService.loadUser의 userNameAttributeName: $s", userNameAttributeName));

        /*
        OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
         */
        OAuthAttributes attributes = OAuthAttributes.of(registrationId,
                userNameAttributeName, oAuth2User.getAttributes());

        User user = saveOrUpdate(attributes);
        httpSession.setAttribute("user", new SessionUser(user)); // 세션에 사용자정보를 저장하기 위한 dto

        return new DefaultOAuth2User(
                Collections.singleton(
                    new SimpleGrantedAuthority(user.getRoleKey())),
                attributes.getAttributes(),
                attributes.getNameAttributeKey()
        );
    }

    private User saveOrUpdate(OAuthAttributes attributes) {
        log.debug(String.format("CustomOAuth2UserService.saveOrUpdate의 attributes: $s", attributes));

        User user = userRepository.findByEmail(attributes.getEmail())
                .map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
                .orElse(attributes.toEntity());

        log.debug(String.format("CustomOAuth2UserService.saveOrUpdate의 user: $s", user.toString()));

        return userRepository.save(user);
    }

}
