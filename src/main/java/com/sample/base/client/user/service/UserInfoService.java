package com.sample.base.client.user.service;

import com.sample.base.client.user.dto.UserInfoDto;
import com.sample.base.client.user.dto.UserInfoUpdateRequestDto;
import com.sample.base.client.user.entity.UserInfo;
import com.sample.base.client.user.repository.UserInfoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class UserInfoService {
    private final UserInfoRepository userInfoRepository;
    private final UserTokenService userTokenService;

    public UserInfoDto update(Long userSeq, UserInfoUpdateRequestDto dto, String emailAtToken) {
        UserInfo org = userInfoRepository.findById(userSeq).orElse(null);
        /// 사용자 없으면
        if(org == null){
            return UserInfoDto.builder()
                    .userName(dto.getUsername())
                    .email(dto.getEmail())
                    .build();
        }

        UserInfo compare = userInfoRepository.findUserInfoByEmail(emailAtToken);
        Long compareUserSeq = Optional.ofNullable(compare.getUserSeq()).orElseGet(() -> -1L);
        /// 타인 수정 불가능
        if(userSeq.equals(compareUserSeq)){
            UserInfo updateEntity = dto.toEntity(userSeq);
            UserInfo merge = org.mergeFrom(updateEntity);
            /// 이메일 변경되었을 때
            if(!emailAtToken.equals(merge.getEmail())){
                int cnt = userInfoRepository.countUserInfoByEmail(merge.getEmail());
                if(cnt > 0){
                    throw new IllegalArgumentException("이메일 중복");
                }

                userInfoRepository.save(merge);
                /// 이메일 변경 시, 기존 토큰 만료
                if(!emailAtToken.equals(merge.getEmail())){
                    userTokenService.setDisableToken(emailAtToken);
                }
            }

            return UserInfoDto.fromEntity(merge);
        }
        return UserInfoDto.builder()
                .userName(dto.getUsername())
                .email(dto.getEmail())
                .build();
    }
}
