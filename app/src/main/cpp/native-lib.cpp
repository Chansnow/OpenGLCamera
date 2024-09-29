#include <jni.h>
#include <string>
#include "inc/BeautyShot_Image_Algorithm.h"
#include "inc/BeautyShot_Video_Algorithm.h"
#include "inc/merror.h"

#include <android/log.h>
#include <stdio.h>
#include <stdlib.h>

#define LOG_TAG "NativeLib"
#define LOGCATE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

// 函数声明
void setOffScreen(ASVLOFFSCREEN *pImg, unsigned char *pData, int iWidth, int iHeight, int iPitch);
//void processVideo(unsigned char *pData, int iWidth, int iHeight, int iPitch);

// 定义一个全局变量来存储 BeautyShot_Video_Algorithm 实例
static BeautyShot_Video_Algorithm* g_BeautyShot_Video_Algorithm = nullptr;

// 初始化 BeautyShot_Video_Algorithm 实例
void initBeautyShotVideoAlgorithm() {
    if (g_BeautyShot_Video_Algorithm == nullptr) {
        MRESULT nRet = Create_BeautyShot_Video_Algorithm(BeautyShot_Video_Algorithm::CLASSID, &g_BeautyShot_Video_Algorithm);
        if (nRet != MOK || g_BeautyShot_Video_Algorithm == nullptr) {
            LOGCATE("Create_BeautyShot_Video_Algorithm, fail");
            return;
        }
        nRet = g_BeautyShot_Video_Algorithm->Init(ABS_INTERFACE_VERSION, ABS_MAX_VIDEO_FACE_NUM);
        if (nRet != MOK) {
            LOGCATE("Init BeautyShot_Video_Algorithm failed");
            return;
        }
    }
}

// 释放 BeautyShot_Video_Algorithm 实例
void uninitBeautyShotVideoAlgorithm() {
    if (g_BeautyShot_Video_Algorithm != nullptr) {
        g_BeautyShot_Video_Algorithm->UnInit();
        g_BeautyShot_Video_Algorithm->Release();
        g_BeautyShot_Video_Algorithm = nullptr;
    }
}

// 设置图像参数
void setOffScreen(ASVLOFFSCREEN* pImg, uint8_t* pData, int iWidth, int iHeight, int iPitch) {
    pImg->u32PixelArrayFormat = ASVL_PAF_NV21;
    pImg->i32Width = iWidth;
    pImg->i32Height = iHeight;
    pImg->pi32Pitch[0] = iPitch;
    pImg->pi32Pitch[1] = iPitch;
    pImg->ppu8Plane[0] = pData;
    pImg->ppu8Plane[1] = pData + pImg->pi32Pitch[0] * pImg->i32Height;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_openglcamera_GLCameraActivity_processPreviewData(JNIEnv *env, jobject thiz, jbyteArray jData, jint width, jint height, jint pitch) {

    // 初始化 BeautyShot_Video_Algorithm
    initBeautyShotVideoAlgorithm();

    // 获取 Java 传入的图像数据
    jbyte* data = env->GetByteArrayElements(jData, nullptr);
    jsize dataLength = env->GetArrayLength(jData);

    // 创建 ASVLOFFSCREEN 结构体
    ASVLOFFSCREEN tScreenSrc = {0};
    setOffScreen(&tScreenSrc, (uint8_t*)data, width, height, pitch);

    // 处理图像
    MRESULT nRet = g_BeautyShot_Video_Algorithm->Process(&tScreenSrc, &tScreenSrc, nullptr, nullptr, 270);


    if (nRet != MOK) {
        LOGCATE("Process image failed");
        env->ReleaseByteArrayElements(jData, data, 0);
        return nullptr;
    }

//    LOGCATE("Successfully Processing PreviewData");

    // 创建一个 jbyteArray 来存储处理后的图像数据
    jbyteArray result = env->NewByteArray(dataLength);
    env->SetByteArrayRegion(result, 0, dataLength, data);

    // 释放原始数据
    env->ReleaseByteArrayElements(jData, data, 0);

    // 释放 BeautyShot_Video_Algorithm 实例
//    uninitBeautyShotVideoAlgorithm();

    return result;
}


// 设置美颜参数
extern "C" JNIEXPORT void JNICALL
Java_com_example_openglcamera_GLCameraActivity_setBeautyParameters(JNIEnv *env, jobject thiz, jint skinSoften, jint skinBrighten, jint eyeEnlargment, jint noseHighlight, jint faceSlender, jint skinSoftenType) {
    if (g_BeautyShot_Video_Algorithm != nullptr) {
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_BASIC_SKIN_SOFTEN, skinSoften);
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_BASIC_SKIN_BRIGHTEN, skinBrighten);
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_SHAPE_EYE_ENLARGEMENT, eyeEnlargment);
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_SHAPE_NOSE_HIGHLIGHT, noseHighlight);
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_SHAPE_FACE_SLENDER, faceSlender);
        g_BeautyShot_Video_Algorithm->SetFeatureLevel(ABS_KEY_SKIN_SOFTEN_TYPE, skinSoftenType);
//        LOGCATE("Successfully Change Parameters: skinSoften = %d", skinSoften);
    }
}


// Native method to process image file
extern "C"
JNIEXPORT void JNICALL
Java_com_example_nv12_1jni_MainActivity_processImageFile(JNIEnv *env, jobject /* this */, jstring filePath) {
    const char *nativeFilePath = env->GetStringUTFChars(filePath, 0);

    // 读取图像数据
    FILE *file = fopen(nativeFilePath, "rb");
    if (file == nullptr) {
        // 错误处理
        return;
    }

    // 获取图像宽度、高度和行跨度
    int width = 1080; // 请替换为实际的宽度
    int height = 1920; // 请替换为实际的高度
    int pitch = ((width + 15) / 16) * 16;

    int frameLength = pitch * height * 3 / 2;
    uint8_t *data = (uint8_t *)malloc(frameLength);

    if (data != nullptr) {
        size_t readSize = fread(data, 1, frameLength, file);
        if (readSize == frameLength) {
            // 初始化 Beauty SDK
            BeautyShot_Image_Algorithm *beautyAlgorithm = nullptr;
            if (Create_BeautyShot_Image_Algorithm(BeautyShot_Image_Algorithm::CLASSID, &beautyAlgorithm) == MOK && beautyAlgorithm != nullptr) {
                MRESULT result = beautyAlgorithm->Init(ABS_INTERFACE_VERSION);
                if (result == MOK) {
                    // 设置美颜参数
                    beautyAlgorithm->SetFeatureLevel(ABS_KEY_BASIC_SKIN_SOFTEN, 30);
                    // 处理图像
                    ASVLOFFSCREEN srcImage = {0};
                    srcImage.u32PixelArrayFormat = ASVL_PAF_NV21;
                    srcImage.i32Width = width;
                    srcImage.i32Height = height;
                    srcImage.pi32Pitch[0] = pitch;
                    srcImage.pi32Pitch[1] = pitch;
                    srcImage.ppu8Plane[0] = data;
                    srcImage.ppu8Plane[1] = data + pitch * height;

                    ASVLOFFSCREEN dstImage = srcImage;
                    int orientation = 270; // 图像方向
                    result = beautyAlgorithm->Process(&srcImage, &dstImage, nullptr, nullptr, orientation);
                    if (result == MOK) {
                        // 保存处理后的图像
                        FILE *outFile = fopen("processed_image.jpg", "wb");
                        if (outFile != nullptr) {
                            fwrite(dstImage.ppu8Plane[0], 1, frameLength, outFile);
                            fclose(outFile);
                        }
                    }
                    beautyAlgorithm->UnInit();
                }
                beautyAlgorithm->Release();
            }
            free(data);
        }
        fclose(file);
    }

    LOGCATE("Successfully Processing Photo");

    env->ReleaseStringUTFChars(filePath, nativeFilePath);
}
