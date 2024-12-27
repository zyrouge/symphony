#include <jni.h>
#include <string>
#include "tfile.h"
#include "tfilestream.h"
#include "fileref.h"
#include "apefile.h"
#include "asffile.h"
#include "dsdifffile.h"
#include "dsffile.h"
#include "flacfile.h"
#include "itfile.h"
#include "modfile.h"
#include "mp4file.h"
#include "mpcfile.h"
#include "mpegfile.h"
#include "oggflacfile.h"
#include "oggfile.h"
#include "opusfile.h"
#include "speexfile.h"
#include "vorbisfile.h"
#include "aifffile.h"
#include "rifffile.h"
#include "wavfile.h"
#include "s3mfile.h"
#include "tfile.h"
#include "trueaudiofile.h"
#include "wavpackfile.h"
#include "xmfile.h"
#include "TagLibHelper.h"
#include "android/log_macros.h"

using namespace TagLib;

File *TagLibHelper::detectParser(
        FileName filename,
        IOStream *stream,
        bool readAudioProperties,
        AudioProperties::ReadStyle audioPropertiesStyle) {
    auto file = detectByExtension(filename, stream, readAudioProperties, audioPropertiesStyle);
    if (!file) {
        file = detectByContent(stream, readAudioProperties, audioPropertiesStyle);
    }
    return file;
}

// Copied from https://github.com/taglib/taglib/blob/master/taglib/fileref.cpp ;-;
File *TagLibHelper::detectByExtension(
        FileName filename,
        IOStream *stream,
        bool readAudioProperties,
        AudioProperties::ReadStyle audioPropertiesStyle) {
    String s = filename;
    String ext;
    if (const int pos = s.rfind("."); pos != -1)
        ext = s.substr(pos + 1).upper();
    ALOGI("%s", ext.toCString(true));

    if (ext.isEmpty())
        return nullptr;

    File *file = nullptr;

    if (ext == "MP3" || ext == "MP2" || ext == "AAC")
        file = new MPEG::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "OGG")
        file = new Ogg::Vorbis::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "OGA") {
        /* .oga can be any audio in the Ogg container. First try FLAC, then Vorbis. */
        file = new Ogg::FLAC::File(stream, readAudioProperties, audioPropertiesStyle);
        if (!file->isValid()) {
            delete file;
            file = new Ogg::Vorbis::File(stream, readAudioProperties, audioPropertiesStyle);
        }
    } else if (ext == "FLAC")
        file = new FLAC::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "MPC")
        file = new MPC::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "WV")
        file = new WavPack::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "SPX")
        file = new Ogg::Speex::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "OPUS")
        file = new Ogg::Opus::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "TTA")
        file = new TrueAudio::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "M4A" || ext == "M4R" || ext == "M4B" || ext == "M4P" || ext == "MP4" ||
             ext == "3G2" || ext == "M4V")
        file = new MP4::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "WMA" || ext == "ASF")
        file = new ASF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "AIF" || ext == "AIFF" || ext == "AFC" || ext == "AIFC")
        file = new RIFF::AIFF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "WAV")
        file = new RIFF::WAV::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "APE")
        file = new APE::File(stream, readAudioProperties, audioPropertiesStyle);
        // module, nst and wow are possible but uncommon extensions
    else if (ext == "MOD" || ext == "MODULE" || ext == "NST" || ext == "WOW")
        file = new Mod::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "S3M")
        file = new S3M::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "IT")
        file = new IT::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "XM")
        file = new XM::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "DSF")
        file = new DSF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if (ext == "DFF" || ext == "DSDIFF")
        file = new DSDIFF::File(stream, readAudioProperties, audioPropertiesStyle);

    // if file is not valid, leave it to content-based detection.

    if (file) {
        if (file->isValid())
            return file;
        delete file;
    }

    return nullptr;
}

File *TagLibHelper::detectByContent(
        IOStream *stream,
        bool readAudioProperties,
        AudioProperties::ReadStyle audioPropertiesStyle)
{
    File *file = nullptr;

    if(MPEG::File::isSupported(stream))
        file = new MPEG::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(Ogg::Vorbis::File::isSupported(stream))
        file = new Ogg::Vorbis::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(Ogg::FLAC::File::isSupported(stream))
        file = new Ogg::FLAC::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(FLAC::File::isSupported(stream))
        file = new FLAC::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(MPC::File::isSupported(stream))
        file = new MPC::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(WavPack::File::isSupported(stream))
        file = new WavPack::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(Ogg::Speex::File::isSupported(stream))
        file = new Ogg::Speex::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(Ogg::Opus::File::isSupported(stream))
        file = new Ogg::Opus::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(TrueAudio::File::isSupported(stream))
        file = new TrueAudio::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(MP4::File::isSupported(stream))
        file = new MP4::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(ASF::File::isSupported(stream))
        file = new ASF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(RIFF::AIFF::File::isSupported(stream))
        file = new RIFF::AIFF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(RIFF::WAV::File::isSupported(stream))
        file = new RIFF::WAV::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(APE::File::isSupported(stream))
        file = new APE::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(DSF::File::isSupported(stream))
        file = new DSF::File(stream, readAudioProperties, audioPropertiesStyle);
    else if(DSDIFF::File::isSupported(stream))
        file = new DSDIFF::File(stream, readAudioProperties, audioPropertiesStyle);

    // isSupported() only does a quick check, so double check the file here.

    if(file) {
        if(file->isValid())
            return file;
        delete file;
    }

    return nullptr;
}
