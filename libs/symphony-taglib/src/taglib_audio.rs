#[derive(Debug)]
pub struct TagLibAudio {
    pub bitrate: i32,
    pub channels: i32,
    pub length: i32,
    pub samplerate: i32,
}

impl TagLibAudio {
    /// `c_properties` should be a valid pointer.
    pub(crate) fn from_taglib_audio_properties(
        c_properties: *const symphony_taglib_sys::TagLib_AudioProperties,
    ) -> TagLibAudio {
        TagLibAudio {
            bitrate: unsafe { symphony_taglib_sys::taglib_audioproperties_bitrate(c_properties) },
            channels: unsafe { symphony_taglib_sys::taglib_audioproperties_channels(c_properties) },
            length: unsafe { symphony_taglib_sys::taglib_audioproperties_length(c_properties) },
            samplerate: unsafe {
                symphony_taglib_sys::taglib_audioproperties_samplerate(c_properties)
            },
        }
    }
}
