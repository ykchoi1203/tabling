package com.younggeun.tabling.model;

import com.younggeun.tabling.persist.entity.PartnerEntity;
import com.younggeun.tabling.persist.entity.UserEntity;
import lombok.Data;

import java.util.List;

public class Auth {
    @Data
    public static class SignIn {
        private String userId;
        private String password;
    }

    @Data
    public static class SignUp {
        private String userId;
        private String userName;
        private String password;
        private String phoneNumber;
        private List<String> roles;

        public UserEntity toEntity() {
            return UserEntity.builder()
                    .userId(this.userId)
                    .password(this.password)
                    .phoneNumber(this.phoneNumber)
                    .name(userName)
                    .roles(this.roles)
                    .build();
        }

        public PartnerEntity toPartnerEntity() {
            return PartnerEntity.builder()
                    .partnerId(this.userId)
                    .password(this.password)
                    .name(this.userName)
                    .phoneNumber(this.phoneNumber)
                    .roles(this.roles)
                    .build();
        }
    }
}
