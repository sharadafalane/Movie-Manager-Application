package com.movie.movieManager.auth.models;

import jakarta.persistence.*;
import java.util.Date;

@Entity
public class ForgotPassword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer fpid;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = false)
    private Date expirationTime;

    @OneToOne
    private User user;

    public ForgotPassword() {
    }

    public ForgotPassword(Integer fpid, Integer otp, Date expirationTime, User user) {
        this.fpid = fpid;
        this.otp = otp;
        this.expirationTime = expirationTime;
        this.user = user;
    }

    public Integer getFpid() {
        return fpid;
    }

    public void setFpid(Integer fpid) {
        this.fpid = fpid;
    }

    public Integer getOtp() {
        return otp;
    }

    public void setOtp(Integer otp) {
        this.otp = otp;
    }

    public Date getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Date expirationTime) {
        this.expirationTime = expirationTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class Builder {
        private Integer fpid;
        private Integer otp;
        private Date expirationTime;
        private User user;

        public Builder fpid(Integer fpid) {
            this.fpid = fpid;
            return this;
        }

        public Builder otp(Integer otp) {
            this.otp = otp;
            return this;
        }

        public Builder expirationTime(Date expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder user(User user) {
            this.user = user;
            return this;
        }

        public ForgotPassword build() {
            return new ForgotPassword(fpid, otp, expirationTime, user);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}