#include "gl_texture_uploader.hpp"

void GlTextureUploader::bindTexture(GLuint textureId) {
    boundTexture_ = textureId;
}

void GlTextureUploader::upload(const cv::Mat& rgba) {
    if (boundTexture_ == 0) {
        return;
    }
    glBindTexture(GL_TEXTURE_2D, boundTexture_);

    if (rgba.cols != lastWidth_ || rgba.rows != lastHeight_) {
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RGBA,
            rgba.cols,
            rgba.rows,
            0,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            rgba.data
        );
        lastWidth_ = rgba.cols;
        lastHeight_ = rgba.rows;
    } else {
        glTexSubImage2D(
            GL_TEXTURE_2D,
            0,
            0,
            0,
            rgba.cols,
            rgba.rows,
            GL_RGBA,
            GL_UNSIGNED_BYTE,
            rgba.data
        );
    }
}

