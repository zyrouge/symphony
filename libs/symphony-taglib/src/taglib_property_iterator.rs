use std::{ffi::CStr, os::raw::c_char};

#[derive(Debug)]
pub struct TagLibPropertyIterator {
    keys: *mut *mut c_char,
    current: *mut *mut c_char,
}

impl TagLibPropertyIterator {
    pub(crate) fn from_taglib_keys(keys: *mut *mut c_char) -> TagLibPropertyIterator {
        TagLibPropertyIterator {
            keys,
            current: keys,
        }
    }
}

impl Iterator for TagLibPropertyIterator {
    type Item = String;

    fn next(&mut self) -> Option<Self::Item> {
        if self.current.is_null() || unsafe { *self.current }.is_null() {
            return None;
        }
        let c_key = unsafe { CStr::from_ptr(*self.current) };
        let key = c_key.to_string_lossy().into_owned();
        self.current = unsafe { self.current.add(1) };
        Some(key)
    }
}

impl Drop for TagLibPropertyIterator {
    fn drop(&mut self) {
        unsafe {
            symphony_taglib_sys::taglib_property_free(self.keys);
        }
    }
}
