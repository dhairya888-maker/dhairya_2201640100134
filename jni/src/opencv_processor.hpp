#pragma once

#include <cstdint>
#include <opencv2/core.hpp>

class OpenCvProcessor {
public:
    void initialise(int width, int height);
    int64_t processFrame(const uint8_t* yData,
                         const uint8_t* uData,
                         const uint8_t* vData,
                         int pixelStride,
                         int rowStride,
                         int width,
                         int height,
                         bool edgeMode);

    const cv::Mat& rgba() const { return rgbaFrame_; }

private:
    cv::Mat yuvFrame_;
    cv::Mat rgbaFrame_;
    cv::Mat grayFrame_;
    cv::Mat edgeFrame_;
    int cachedWidth_ = 0;
    int cachedHeight_ = 0;
};

