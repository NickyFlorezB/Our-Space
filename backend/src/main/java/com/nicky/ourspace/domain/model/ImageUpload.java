package com.nicky.ourspace.domain.model;

import java.io.InputStream;

public record ImageUpload(
        InputStream content,
        long size) {
}