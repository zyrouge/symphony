use std::{
    ffi::{CStr, CString, NulError},
    path::Path,
};

use symphony_taglib_sys::taglib_file_is_valid;

use crate::{
    taglib_audio::TagLibAudio, taglib_error::TagLibError, taglib_picture::TagLibPicture,
    taglib_property_iterator::TagLibPropertyIterator,
};

#[derive(Debug)]
pub struct TagLibFile {
    stream: *mut symphony_taglib_sys::TagLib_IOStream,
    file: *mut symphony_taglib_sys::TagLib_File,
}

impl TagLibFile {
    pub fn from_path(path: &Path) -> Result<TagLibFile, TagLibError> {
        #[cfg(unix)]
        let file = unsafe {
            use std::os::unix::ffi::OsStrExt;
            let c_filename =
                CString::new(path.as_os_str().as_bytes()).map_err(|_| TagLibError::InvalidFile)?;
            symphony_taglib_sys::taglib_file_new(c_filename.as_ptr())
        };
        #[cfg(windows)]
        let file = unsafe {
            use std::os::windows::ffi::OsStrExt;
            let mut c_wide_filename: Vec<u16> = path.as_os_str().encode_wide().collect();
            if c_wide_filename.contains(&0) {
                return Err(TagLibError::InvalidFile);
            }
            c_wide_filename.push(0);
            symphony_taglib_sys::taglib_file_new_wchar(c_wide_filename.as_ptr())
        };
        if file.is_null() {
            return Err(TagLibError::FileCreationFailed);
        }
        let is_valid = unsafe { taglib_file_is_valid(file) } == 1;
        if !is_valid {
            unsafe {
                symphony_taglib_sys::taglib_file_free(file);
            }
            return Err(TagLibError::InvalidFile);
        }
        Ok(TagLibFile {
            stream: std::ptr::null_mut(),
            file,
        })
    }

    #[cfg(unix)]
    pub(crate) fn from_taglib_iostream(
        stream: *mut symphony_taglib_sys::TagLib_IOStream,
    ) -> Result<TagLibFile, TagLibError> {
        let file = unsafe { symphony_taglib_sys::taglib_file_new_iostream(stream) };
        if file.is_null() {
            unsafe {
                symphony_taglib_sys::taglib_iostream_free(stream);
            }
            return Err(TagLibError::StreamCreationFailed);
        }
        let is_valid = unsafe { taglib_file_is_valid(file) } == 1;
        if !is_valid {
            unsafe {
                symphony_taglib_sys::taglib_file_free(file);
                symphony_taglib_sys::taglib_iostream_free(stream);
            }
            return Err(TagLibError::InvalidFile);
        }
        Ok(TagLibFile { stream, file })
    }

    /// The file descriptor `fd` will be duplicated. The original file descriptor `fd` needs to be
    /// managed separately and is never closed by this type.
    /// The duplicate file descriptor shares the file position with original descriptor `fd`,
    /// so reading or parsing moves the caller's offset as well.
    /// Seek `fd` back if a known position is needed afterwards.
    #[cfg(unix)]
    pub fn from_file_descriptor(fd: i32, readonly: bool) -> Result<TagLibFile, TagLibError> {
        let stream =
            unsafe { symphony_taglib_sys::taglib_fd_iostream_new(fd, i32::from(readonly)) };
        if stream.is_null() {
            return Err(TagLibError::StreamCreationFailed);
        }
        TagLibFile::from_taglib_iostream(stream)
    }

    /// The file descriptor `fd` will be duplicated. The original file descriptor `fd` needs to be
    /// managed separately and is never closed by this type.
    ///
    /// The duplicate shares the file position with `fd`, so reading or parsing moves the
    /// caller's offset as well. Seek `fd` back if a known position is needed afterwards.
    #[cfg(unix)]
    pub fn from_named_file_descriptor(
        fd: i32,
        readonly: bool,
        filename: &str,
    ) -> Result<TagLibFile, TagLibError> {
        let c_filename = CString::new(filename).map_err(|_| TagLibError::InvalidFile)?;
        let stream = unsafe {
            symphony_taglib_sys::taglib_named_fd_iostream_new(
                fd,
                i32::from(readonly),
                c_filename.as_ptr(),
            )
        };
        if stream.is_null() {
            return Err(TagLibError::StreamCreationFailed);
        }
        TagLibFile::from_taglib_iostream(stream)
    }

    pub fn keys(&self) -> TagLibPropertyIterator {
        let c_keys = unsafe { symphony_taglib_sys::taglib_property_keys(self.file) };
        TagLibPropertyIterator::from_taglib_keys(c_keys)
    }

    pub fn get_key(&self, key: &str) -> Option<String> {
        let c_key = CString::new(key).ok()?;
        let c_value_raw =
            unsafe { symphony_taglib_sys::taglib_property_get(self.file, c_key.as_ptr()) };
        if c_value_raw.is_null() {
            return None;
        }
        if unsafe { *c_value_raw }.is_null() {
            unsafe { symphony_taglib_sys::taglib_property_free(c_value_raw) };
            return None;
        }
        let c_value = unsafe { CStr::from_ptr(*c_value_raw) };
        let value = c_value.to_string_lossy().into_owned();
        unsafe { symphony_taglib_sys::taglib_property_free(c_value_raw) };
        Some(value)
    }

    pub fn set_key(&mut self, key: &str, value: Option<&str>) -> Result<(), NulError> {
        let c_key = CString::new(key)?;
        let c_value = value.map(CString::new).transpose()?;
        unsafe {
            symphony_taglib_sys::taglib_property_set(
                self.file,
                c_key.as_ptr(),
                c_value.as_ref().map_or(std::ptr::null(), |v| v.as_ptr()),
            );
        }
        Ok(())
    }

    pub fn get_picture(&self) -> Option<TagLibPicture> {
        let c_key = CString::new("PICTURE").ok()?;
        let c_properties =
            unsafe { symphony_taglib_sys::taglib_complex_property_get(self.file, c_key.as_ptr()) };
        if c_properties.is_null() {
            return None;
        }
        Some(TagLibPicture::from_taglib_complex_properties(c_properties))
    }

    pub fn audio(&self) -> Option<TagLibAudio> {
        let c_properties = unsafe { symphony_taglib_sys::taglib_file_audioproperties(self.file) };
        if c_properties.is_null() {
            return None;
        }
        Some(TagLibAudio::from_taglib_audio_properties(c_properties))
    }

    pub fn save(&mut self) -> bool {
        unsafe { symphony_taglib_sys::taglib_file_save(self.file) == 1 }
    }
}

impl Drop for TagLibFile {
    fn drop(&mut self) {
        unsafe {
            symphony_taglib_sys::taglib_file_free(self.file);
            if !self.stream.is_null() {
                symphony_taglib_sys::taglib_iostream_free(self.stream);
            }
        }
    }
}
