package com.school;

/**
 * Entry point for the packaged jar / .exe. Routing through a plain main()
 * avoids the "JavaFX runtime components are missing" error. Maven runs App directly.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}