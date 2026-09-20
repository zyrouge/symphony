pub trait Album {
    fn provider(&self) -> &str;
    fn id(&self) -> &str;
    fn name(&self) -> &str;
}
