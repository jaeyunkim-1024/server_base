package com.sample.base.kafka.topics.email.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sample.base.common.dto.CustomResponseDto;
import com.sample.base.common.util.VerifyCodeUtil;
import com.sample.base.kafka.topics.email.model.EmailDto;
import com.sample.base.kafka.topics.email.service.EmailProducer;
import com.sample.base.security.model.CustomUserDetails;
import com.sample.base.security.provider.JwtTokenProvider;
import com.sample.base.user.enums.UserRoles;
import com.sample.base.user.service.AuthService;
import com.sample.base.user.service.CustomUserDetailService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin("*")
@RestController
@RequestMapping(value = "/kafka/topic/email")
@RequiredArgsConstructor
public class EmailController {
    private final EmailProducer producer;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final CustomUserDetailService customUserDetailService;

    @PostMapping(value = "/send")
    public CustomResponseDto<Object> sendMessage(HttpServletRequest request) {
        boolean isCert = jwtTokenProvider.isCertUser(request);
        if(isCert) {
            return CustomResponseDto
                    .builder()
                    .data("이미 인증한 회원입니다.")
                    .build();
        }

        String email = jwtTokenProvider.getPrincipal(request);
        String verifyCode = VerifyCodeUtil.generated();
        EmailDto emailDto = EmailDto.builder()
                .email(email)
                .verifyCode(verifyCode)
                .build();
        this.producer.send(emailDto.toString());
        return CustomResponseDto
                .builder()
                .data("이메일이 발송되었습니다.")
                .build();
    }

    @PostMapping(value = "/verify")
    public CustomResponseDto<Object> verify(@RequestParam(name = "verifyCode", required = false)String verifyCode, HttpServletRequest request) {
        boolean isCert = jwtTokenProvider.isCertUser(request);
        if(isCert) {
            return CustomResponseDto
                    .builder()
                    .data("이미 인증한 회원입니다.")
                    .build();
        }
        String email = jwtTokenProvider.getPrincipal(request);
        EmailDto dto = EmailDto.builder()
                .email(email)
                .verifyCode(verifyCode)
                .build();
        Object token = "인증코드를 확인해주세요.";
        if(authService.verifyCode(dto) != -1){
            /// 토큰 재발급
            try{
                /// 기존 토큰 만료
                jwtTokenProvider.tokenExpire(email);

                /// 신규 토큰 발급
                CustomUserDetails customUserDetails = customUserDetailService.loadUserByUsername(email);
                token = jwtTokenProvider.reIssuedToken(email,customUserDetails,"ROLE_"+ UserRoles.USER.getType());
            }catch (JsonProcessingException e) {
                token = e.getMessage();
            }
        }
        return CustomResponseDto
                .builder()
                .data(token)
                .build();
    }
}
