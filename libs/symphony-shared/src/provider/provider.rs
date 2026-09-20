use crate::entity::album::Album;
use crate::entity::artist::Artist;
use crate::entity::track::Track;
use std::future::Future;

pub trait Provider {
    fn id(&self) -> &str;

    fn name(&self) -> &str;

    fn get_track(
        &self,
        id: &str,
    ) -> impl Future<Output = Result<Box<dyn Track>, Box<dyn std::error::Error>>> + Send;

    fn all_tracks(
        &self,
        offset: usize,
        limit: usize,
    ) -> impl Future<Output = Result<Vec<Box<dyn Track>>, Box<dyn std::error::Error>>> + Send;

    fn get_album(
        &self,
        id: &str,
    ) -> impl Future<Output = Result<Box<dyn Album>, Box<dyn std::error::Error>>> + Send;

    fn all_albums(
        &self,
        offset: usize,
        limit: usize,
    ) -> impl Future<Output = Result<Vec<Box<dyn Album>>, Box<dyn std::error::Error>>> + Send;

    fn get_artist(
        &self,
        id: &str,
    ) -> impl Future<Output = Result<Box<dyn Artist>, Box<dyn std::error::Error>>> + Send;

    fn all_artists(
        &self,
        offset: usize,
        limit: usize,
    ) -> impl Future<Output = Result<Vec<Box<dyn Artist>>, Box<dyn std::error::Error>>> + Send;
}
