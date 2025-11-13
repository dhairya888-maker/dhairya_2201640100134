#pragma once

#include <GLES2/gl2.h>
#include <opencv2/core.hpp>

class GlTextureUploader {
public:
    void bindTexture(GLuint textureId);
    void upload(const cv::Mat& rgba);

private:
    GLuint boundTexture_ = 0;
    int lastWidth_ = 0;
    int lastHeight_ = 0;
};

