#include <jni.h>
#include <string>
#include "android/log_macros.h"
#include "tfile.h"
#include "tfilestream.h"
#include "tpropertymap.h"
#include "fileref.h"
#include "TagLibHelper.h"

jclass audioMetadataParserClass = nullptr;
jmethodID audioMetadataParserPutTagMethodId = nullptr;
jmethodID audioMetadataParserPutPictureMethodId = nullptr;
jmethodID audioMetadataParserPutAudioPropertyMethodId = nullptr;

extern "C" {
JNIEXPORT jint
JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    const auto _audioMetadataParserClass = env->FindClass(
            "me/zyrouge/symphony/metaphony/AudioMetadataParser");
    audioMetadataParserClass = reinterpret_cast<jclass>(env->NewGlobalRef(
            _audioMetadataParserClass));
    audioMetadataParserPutTagMethodId = env->GetMethodID(
            audioMetadataParserClass,
            "putTag", "(Ljava/lang/String;Ljava/lang/String;)V");
    audioMetadataParserPutPictureMethodId = env->GetMethodID(
            audioMetadataParserClass,
            "putPicture", "(Ljava/lang/String;Ljava/lang/String;[B)V");
    audioMetadataParserPutAudioPropertyMethodId = env->GetMethodID(
            audioMetadataParserClass,
            "putAudioProperty", "(Ljava/lang/String;I)V");
    env->DeleteLocalRef(_audioMetadataParserClass);
    return JNI_VERSION_1_6;
}

JNIEXPORT void
JNI_OnUnload(JavaVM *vm, void *) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }
    env->DeleteGlobalRef(audioMetadataParserClass);
    audioMetadataParserClass = nullptr;
    audioMetadataParserPutTagMethodId = nullptr;
    audioMetadataParserPutPictureMethodId = nullptr;
    audioMetadataParserPutAudioPropertyMethodId = nullptr;
}

JNIEXPORT jboolean JNICALL
Java_me_zyrouge_symphony_metaphony_AudioMetadataParser_readMetadata(
        JNIEnv *env,
        jobject thiz,
        jstring filename,
        jint fd) {
    const auto stream = std::make_unique<TagLib::FileStream>(fd, true);
    const auto file = TagLibHelper::detectParser(
            env->GetStringUTFChars(filename, nullptr),
            stream.get(),
            true,
            TagLib::AudioProperties::ReadStyle::Accurate);
    if (file == nullptr) {
        return static_cast<jboolean>(false);
    }
    if (file->tag()) {
        const auto tags = file->properties();
        ALOGI("3");
        if (!tags.isEmpty()) {
            for (const auto &[key, values]: tags) {
                const auto jKey = env->NewStringUTF(key.toCString(true));
                for (const auto &value: values) {
                    const auto jValue = env->NewStringUTF(value.toCString(true));
                    env->CallVoidMethod(
                            thiz,
                            audioMetadataParserPutTagMethodId,
                            jKey,
                            jValue);
                }
            }
        }
        const auto picture = file->complexProperties("PICTURE");
        for (const auto &x: picture) {
            const auto pictureType = x["pictureType"].toString();
            const auto mimeType = x["mimeType"].toString();
            const auto data = x["data"].toByteVector();
            const auto jPictureType = env->NewStringUTF(pictureType.toCString(true));
            const auto jMimeType = env->NewStringUTF(mimeType.toCString(true));
            const auto jDataSize = static_cast<jint>(data.size());
            const auto jData = env->NewByteArray(jDataSize);
            env->SetByteArrayRegion(
                    jData,
                    0,
                    jDataSize,
                    reinterpret_cast<const jbyte *>(data.data()));
            env->CallVoidMethod(
                    thiz,
                    audioMetadataParserPutPictureMethodId,
                    jPictureType,
                    jMimeType,
                    jData);
        }
    }
    const auto audioProperties = file->audioProperties();
    if (audioProperties) {
        env->CallVoidMethod(
                thiz,
                audioMetadataParserPutAudioPropertyMethodId,
                env->NewStringUTF("BITRATE"),
                static_cast<jint>(audioProperties->bitrate()));
        env->CallVoidMethod(
                thiz,
                audioMetadataParserPutAudioPropertyMethodId,
                env->NewStringUTF("LENGTH_SECONDS"),
                static_cast<jint>(audioProperties->lengthInSeconds()));
        env->CallVoidMethod(
                thiz,
                audioMetadataParserPutAudioPropertyMethodId,
                env->NewStringUTF("SAMPLE_RATE"),
                static_cast<jint>(audioProperties->sampleRate()));
        env->CallVoidMethod(
                thiz,
                audioMetadataParserPutAudioPropertyMethodId,
                env->NewStringUTF("CHANNELS"),
                static_cast<jint>(audioProperties->channels()));
    }
    return static_cast<jboolean>(true);
}
}