#include "opencv_processor.hpp"

#include <opencv2/imgproc.hpp>
#include <cstring>
#include <chrono>

namespace {
constexpr int EDGE_LOW_THRESHOLD = 50;
constexpr int EDGE_HIGH_THRESHOLD = 150;
}

void OpenCvProcessor::initialise(int width, int height) {
    if (width == cachedWidth_ && height == cachedHeight_) {
        return;
    }
    cachedWidth_ = width;
    cachedHeight_ = height;
    yuvFrame_ = cv::Mat(height + height / 2, width, CV_8UC1);
    rgbaFrame_ = cv::Mat(height, width, CV_8UC4);
    grayFrame_ = cv::Mat(height, width, CV_8UC1);
    edgeFrame_ = cv::Mat(height, width, CV_8UC1);
}

int64_t OpenCvProcessor::processFrame(const uint8_t* yData,
                                      const uint8_t* uData,
                                      const uint8_t* vData,
                                      int pixelStride,
                                      int rowStride,
                                      int width,
                                      int height,
                                      bool edgeMode) {
    initialise(width, height);

    // Copy Y plane
    for (int y = 0; y < height; ++y) {
        std::memcpy(
            yuvFrame_.ptr(y),
            yData + y * rowStride,
            width);
    }

    // Copy UV planes interleaving to NV21 format
    uint8_t* uvDst = yuvFrame_.ptr(height);
    int uvHeight = height / 2;
    for (int row = 0; row < uvHeight; ++row) {
        for (int col = 0; col < width / 2; ++col) {
            int idx = row * rowStride + col * pixelStride;
            uvDst[row * width + col * 2] = vData[idx];     // V
            uvDst[row * width + col * 2 + 1] = uData[idx]; // U
        }
    }

    cv::cvtColor(yuvFrame_, rgbaFrame_, cv::COLOR_YUV2RGBA_NV21);

    if (edgeMode) {
        cv::cvtColor(rgbaFrame_, grayFrame_, cv::COLOR_RGBA2GRAY);
        cv::GaussianBlur(grayFrame_, grayFrame_, cv::Size(5, 5), 1.4);
        cv::Canny(grayFrame_, edgeFrame_, EDGE_LOW_THRESHOLD, EDGE_HIGH_THRESHOLD);
        cv::cvtColor(edgeFrame_, rgbaFrame_, cv::COLOR_GRAY2RGBA);
    }

    return std::chrono::steady_clock::now().time_since_epoch().count();
}

