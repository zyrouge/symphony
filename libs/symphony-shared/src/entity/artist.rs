pub trait Artist {
    fn provider(&self) -> &str;
    fn id(&self) -> &str;
    fn name(&self) -> &str;
}
