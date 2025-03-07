package com.sample.base.common.config.env;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DotEnvConfig {
    @Bean
    public Dotenv dotenv() {
        // .env 파일을 읽어서 환경변수로 사용
        String profiles = System.getProperty("spring.profiles.active","local");
        return  Dotenv.configure().directory("./profiles/"+profiles)
//                .ignoreIfMissing() // .env 파일이 없어도 에러 발생 안함
                .load();
    }
}
