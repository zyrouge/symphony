package me.zyrouge.symphony.metaphony

class Metaphony {

    /**
     * A native method that is implemented by the 'metaphony' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'metaphony' library on application startup.
        init {
            System.loadLibrary("metaphony")
        }
    }
}