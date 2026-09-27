use std::{ffi::CStr, os::raw::c_char};

#[derive(Debug)]
pub struct TagLibPicture {
    properties: *mut *mut *mut symphony_taglib_sys::TagLib_Complex_Property_Attribute,
    picture: symphony_taglib_sys::TagLib_Complex_Property_Picture_Data,
}

impl TagLibPicture {
    /// `c_properties` should be a valid pointer.
    pub(crate) fn from_taglib_complex_properties(
        c_properties: *mut *mut *mut symphony_taglib_sys::TagLib_Complex_Property_Attribute,
    ) -> TagLibPicture {
        let mut picture = symphony_taglib_sys::TagLib_Complex_Property_Picture_Data {
            mimeType: std::ptr::null::<c_char>() as *mut c_char,
            description: std::ptr::null::<c_char>() as *mut c_char,
            pictureType: std::ptr::null::<c_char>() as *mut c_char,
            data: std::ptr::null::<c_char>() as *mut c_char,
            size: 0,
        };
        unsafe {
            symphony_taglib_sys::taglib_picture_from_complex_property(c_properties, &mut picture);
        };
        TagLibPicture {
            properties: c_properties,
            picture,
        }
    }

    pub fn mime_type(&self) -> Option<String> {
        if self.picture.mimeType.is_null() {
            return None;
        }
        let c_value = unsafe { CStr::from_ptr(self.picture.mimeType) };
        Some(c_value.to_string_lossy().into_owned())
    }

    pub fn description(&self) -> Option<String> {
        if self.picture.description.is_null() {
            return None;
        }
        let c_value = unsafe { CStr::from_ptr(self.picture.description) };
        Some(c_value.to_string_lossy().into_owned())
    }

    pub fn picture_type(&self) -> Option<String> {
        if self.picture.pictureType.is_null() {
            return None;
        }
        let c_value = unsafe { CStr::from_ptr(self.picture.pictureType) };
        Some(c_value.to_string_lossy().into_owned())
    }

    pub fn size(&self) -> u32 {
        self.picture.size
    }

    pub fn data(&self) -> Option<&[u8]> {
        if self.picture.data.is_null() || self.picture.size == 0 {
            return None;
        }
        let slice = unsafe {
            std::slice::from_raw_parts(self.picture.data as *const u8, self.picture.size as usize)
        };
        Some(slice)
    }
}

impl Drop for TagLibPicture {
    fn drop(&mut self) {
        unsafe {
            if !self.properties.is_null() {
                symphony_taglib_sys::taglib_complex_property_free(self.properties);
            }
        }
    }
}
