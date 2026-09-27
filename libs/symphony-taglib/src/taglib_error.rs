use thiserror::Error;

#[derive(Error, Debug, Clone, PartialEq, Eq)]
pub enum TagLibError {
    #[error("unsupported operation")]
    UnsupportedOperation,
    #[error("invalid file")]
    InvalidFile,
    #[error("stream creation failed")]
    StreamCreationFailed,
    #[error("file creation failed")]
    FileCreationFailed,
}
