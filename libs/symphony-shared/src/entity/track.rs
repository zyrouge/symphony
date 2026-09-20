pub trait Track {
    fn provider(&self) -> &str;
    fn id(&self) -> &str;
    fn title(&self) -> &str;
}
