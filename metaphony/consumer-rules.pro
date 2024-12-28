-keep class me.zyrouge.symphony.metaphony.AudioMetadataParser {
    void putTag(java.lang.String, java.lang.String);
    void putPicture(java.lang.String, java.lang.String, byte[]);
    void putAudioProperty(java.lang.String, int);
    boolean readMetadata(java.lang.String, int);
}