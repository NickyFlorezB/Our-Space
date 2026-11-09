package com.nicky.ourspace.infrastructure.drivenadapters.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.push")
public class PushProperties {

    private String publicKey = "";
    private String privateKey = "";
    private String subject = "";

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public boolean isEnabled() {
        return !publicKey.isBlank() && !privateKey.isBlank() && !subject.isBlank();
    }

    public boolean isPartiallyConfigured() {
        return !publicKey.isBlank() || !privateKey.isBlank() || !subject.isBlank();
    }
}
