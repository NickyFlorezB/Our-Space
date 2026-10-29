package com.nicky.ourspace.application.dto;

import java.io.InputStream;

import com.nicky.ourspace.domain.model.Image;

public record ImageDownload(Image image, InputStream content) {
}
